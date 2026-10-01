package co.onestep.kmp.uikit.bridge

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryMultiRoute
import platform.AVFAudio.AVAudioSessionCategoryOptionDuckOthers
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVAudioSessionCategoryRecord
import platform.AVFAudio.AVAudioSessionModeDefault
import platform.AVFAudio.AVSpeechBoundary
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechSynthesizerDelegateProtocol
import platform.AVFAudio.AVSpeechUtterance
import platform.AVFAudio.setActive
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.create
import platform.darwin.NSObject

/** Copies the array into an `NSData` — `dataWithBytes:length:` copies, so the pin can be released. */
@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
    NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
}

/**
 * The only options installed with the playback category: `DuckOthers`, as the iOS UI kit's
 * `ostPlayback` (OS-17539). The Bluetooth options belong to the recording categories; a device
 * (not the Simulator) rejects them with `-50` and leaves the category as it was.
 */
private val VOICE_OVER_OPTIONS = AVAudioSessionCategoryOptionDuckOthers

/**
 * Makes the process-wide audio session able to play a voice cue, and activates it — the iOS UI
 * kit's `ostActivateForVoiceOver()` rule (`4838ee8`, `0ce9976`), shared by the clip and TTS players.
 *
 * - A category that owns an input route is never downgraded. The SDK holds `PlayAndRecord` from
 *   Get Ready to capture Dual Task audio (OS-17543), and a host may record under `Record` or
 *   `MultiRoute`; `Playback` has no input, so taking it silently ended their capture. Those
 *   categories already play.
 * - The category is not set again when it is already exactly `Playback`/`Default`/[VOICE_OVER_OPTIONS]:
 *   a `setCategory` on an active session is a main-thread hang risk, once per cue.
 * - The session is always activated: without it the first clip of a flow is dropped whenever the
 *   app is not already the active audio source.
 *
 * Failures are logged (domain and code only — nothing here carries patient data) rather than
 * thrown: audio is non-critical, and silence used to be the only symptom (OS-17410).
 */
@OptIn(ExperimentalForeignApi::class)
private fun activateAudioSessionForVoiceOver(caller: String) {
    val session = AVAudioSession.sharedInstance()
    val category = session.category
    val ownsInputRoute = category == AVAudioSessionCategoryPlayAndRecord ||
        category == AVAudioSessionCategoryRecord ||
        category == AVAudioSessionCategoryMultiRoute
    val alreadyConfigured = category == AVAudioSessionCategoryPlayback &&
        session.mode == AVAudioSessionModeDefault &&
        session.categoryOptions == VOICE_OVER_OPTIONS
    memScoped {
        if (!ownsInputRoute && !alreadyConfigured) {
            val categoryError = alloc<ObjCObjectVar<NSError?>>()
            val categorySet = session.setCategory(
                AVAudioSessionCategoryPlayback,
                mode = AVAudioSessionModeDefault,
                options = VOICE_OVER_OPTIONS,
                error = categoryError.ptr,
            )
            if (!categorySet) logAudioError(caller, "setCategory", categoryError.value)
        }
        val activeError = alloc<ObjCObjectVar<NSError?>>()
        if (!session.setActive(true, error = activeError.ptr)) {
            logAudioError(caller, "setActive", activeError.value)
        }
    }
}

private fun logAudioError(caller: String, operation: String, error: NSError?) {
    println("$caller: $operation failed: ${error?.domain} ${error?.code}")
}

@OptIn(ExperimentalForeignApi::class)
actual class PlatformAudioPlayer {
    private var player: AVAudioPlayer? = null

    actual fun play(bytes: ByteArray) {
        if (bytes.isEmpty()) return
        try {
            activateAudioSessionForVoiceOver("PlatformAudioPlayer")
            player = memScoped {
                val error = alloc<ObjCObjectVar<NSError?>>()
                val created = AVAudioPlayer(data = bytes.toNSData(), error = error.ptr)
                error.value?.let { logAudioError("PlatformAudioPlayer", "AVAudioPlayer", it) }
                created
            }
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
        player?.stop()
        player = null
        // The session is deliberately left active, as the iOS UI kit leaves it. It is
        // process-wide: during a Dual Task measurement it is the SDK's PlayAndRecord capture,
        // which deactivating would interrupt, and a host may be using it too. DuckOthers lowers
        // other audio while it is active rather than stopping it.
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
        // Same session rule as the clip player: speech must not downgrade a recording session
        // either — the Get Ready prompt speaks right after the SDK arms the Dual Task microphone.
        activateAudioSessionForVoiceOver("PlatformTTSPlayer")
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
