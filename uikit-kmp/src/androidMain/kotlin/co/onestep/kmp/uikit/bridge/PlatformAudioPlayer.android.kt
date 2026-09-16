package co.onestep.kmp.uikit.bridge

import android.content.Context
import co.onestep.kmp.uikit.features.audio.MediaPlayerAudioPlayer
import co.onestep.kmp.uikit.features.audio.TTSPlayerImpl

// The unused `context` is kept so hosts that build this directly still compile; MediaPlayer needs
// no Context now that the clip arrives as bytes rather than a res/raw id.
actual class PlatformAudioPlayer(@Suppress("UNUSED_PARAMETER") context: Context? = null) {
    private val delegate = MediaPlayerAudioPlayer()

    actual fun play(bytes: ByteArray) { delegate.play(bytes) }
    actual fun stop() { delegate.stop() }
    actual fun isPlaying(): Boolean = delegate.isPlaying()
}

actual class PlatformTTSPlayer(context: Context? = null) {
    private val delegate: TTSPlayerImpl? = context?.let { TTSPlayerImpl(it) }

    actual fun speak(text: String, languageTag: String) { delegate?.speak(text, languageTag) }
    actual fun stop() { delegate?.stopCurrentSpeech() }
    actual fun isSpeaking(): Boolean = delegate?.isSpeaking() ?: false
    actual fun setOnDoneListener(callback: (() -> Unit)?) { delegate?.setOnDoneListener(callback) }
}
