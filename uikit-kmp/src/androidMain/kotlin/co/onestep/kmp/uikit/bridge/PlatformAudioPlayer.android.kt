package co.onestep.kmp.uikit.bridge

import android.content.Context
import co.onestep.kmp.uikit.features.audio.MediaPlayerAudioPlayer
import co.onestep.kmp.uikit.features.audio.TTSPlayerImpl

actual class PlatformAudioPlayer(context: Context? = null) {
    private val delegate: MediaPlayerAudioPlayer? = context?.let { MediaPlayerAudioPlayer(it) }

    actual fun play(resourceKey: String) { delegate?.playAudio(resourceKey) }
    actual fun stop() { delegate?.stopCurrentAudio() }
    actual fun isPlaying(): Boolean = delegate?.isPlaying() ?: false
}

actual class PlatformTTSPlayer(context: Context? = null) {
    private val delegate: TTSPlayerImpl? = context?.let { TTSPlayerImpl(it) }

    actual fun speak(text: String) { delegate?.speak(text) }
    actual fun stop() { delegate?.stopCurrentSpeech() }
    actual fun isSpeaking(): Boolean = delegate?.isSpeaking() ?: false
    actual fun setOnDoneListener(callback: (() -> Unit)?) { delegate?.setOnDoneListener(callback) }
}
