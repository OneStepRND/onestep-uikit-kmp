package co.onestep.kmp.uikit.bridge

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation
import platform.AVFAudio.AVSpeechBoundary
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechSynthesizerDelegateProtocol
import platform.AVFAudio.AVSpeechUtterance
import platform.AVFAudio.setActive
import platform.Foundation.NSData
import platform.Foundation.create
import platform.darwin.NSObject

/** Copies the array into an `NSData` — `dataWithBytes:length:` copies, so the pin can be released. */
@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
    NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
}

@OptIn(ExperimentalForeignApi::class)
actual class PlatformAudioPlayer {
    private var player: AVAudioPlayer? = null

    actual fun play(bytes: ByteArray) {
        if (bytes.isEmpty()) return
        try {
            val session = AVAudioSession.sharedInstance()
            session.setCategory(AVAudioSessionCategoryPlayback, error = null)
            // Without activating the session the first clip of a flow is dropped whenever the
            // app is not already the active audio source.
            session.setActive(true, error = null)

            player = AVAudioPlayer(data = bytes.toNSData(), error = null)
            player?.prepareToPlay()
            player?.play()
        } catch (e: Exception) {
            // Audio is non-critical, but the failure is logged: silence used to be the only
            // symptom of the voice files never being found at all (OS-17410).
            println("PlatformAudioPlayer: cannot play audio: ${e.message}")
            player = null
        }
    }

    actual fun stop() {
        val wasPlaying = player != null
        player?.stop()
        player = null
        // Hand the session back. The playback category suppresses whatever the host or another
        // app was playing, so staying active past the last clip would leave a patient's music
        // silenced for the rest of the session.
        if (wasPlaying) {
            AVAudioSession.sharedInstance().setActive(
                false,
                withOptions = AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation,
                error = null,
            )
        }
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
