package co.onestep.kmp.uikit.bridge

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVSpeechBoundary
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechSynthesizerDelegateProtocol
import platform.AVFAudio.AVSpeechUtterance
import platform.Foundation.NSBundle
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
actual class PlatformAudioPlayer {
    private var player: AVAudioPlayer? = null

    actual fun play(resourceKey: String) {
        try {
            val session = AVAudioSession.sharedInstance()
            session.setCategory(AVAudioSessionCategoryPlayback, error = null)

            val url = NSBundle.mainBundle.URLForResource(resourceKey, withExtension = "mp3")
                ?: NSBundle.mainBundle.URLForResource(resourceKey, withExtension = "wav")
                ?: return

            player = AVAudioPlayer(contentsOfURL = url, error = null)
            player?.prepareToPlay()
            player?.play()
        } catch (_: Exception) {
            // Silently fail - audio is non-critical
        }
    }

    actual fun stop() {
        player?.stop()
        player = null
    }

    actual fun isPlaying(): Boolean = player?.isPlaying() == true
}

actual class PlatformTTSPlayer {
    private val synthesizer = AVSpeechSynthesizer()
    private var onDoneCallback: (() -> Unit)? = null
    private val delegate = TTSDelegate { onDoneCallback?.invoke() }

    init {
        synthesizer.delegate = delegate
    }

    actual fun speak(text: String, languageTag: String) {
        if (synthesizer.isSpeaking()) {
            // AVSpeechBoundary is an NS_ENUM, so cinterop exposes it as a Kotlin enum — the
            // raw `0 as AVSpeechBoundary` this used to do threw ClassCastException on every
            // call ("this cast can never succeed"), taking the app down whenever speech was
            // cut short (Dual Task's "Start now" while the instructions are still read).
            synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
        }
        val utterance = AVSpeechUtterance.speechUtteranceWithString(text)
        // voiceWithLanguage returns null when the device has no voice for the tag; leaving
        // `voice` null makes AVSpeechSynthesizer pick the system default rather than going
        // silent, which is the better failure than reading Russian text in an English voice.
        utterance.voice = AVSpeechSynthesisVoice.voiceWithLanguage(languageTag)
        utterance.rate = 0.5f
        synthesizer.speakUtterance(utterance)
    }

    actual fun stop() {
        if (synthesizer.isSpeaking()) {
            synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
        }
    }

    actual fun isSpeaking(): Boolean = synthesizer.isSpeaking()

    actual fun setOnDoneListener(callback: (() -> Unit)?) {
        onDoneCallback = callback
    }
}

private class TTSDelegate(
    private val onFinish: () -> Unit,
) : NSObject(), AVSpeechSynthesizerDelegateProtocol {
    override fun speechSynthesizer(
        synthesizer: AVSpeechSynthesizer,
        didFinishSpeechUtterance: AVSpeechUtterance,
    ) {
        onFinish()
    }
}
