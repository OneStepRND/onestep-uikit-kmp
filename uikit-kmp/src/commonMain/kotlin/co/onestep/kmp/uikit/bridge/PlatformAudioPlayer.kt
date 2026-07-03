package co.onestep.kmp.uikit.bridge

/**
 * Platform audio player. Android: MediaPlayer, iOS: AVAudioPlayer
 */
expect class PlatformAudioPlayer {
    fun play(resourceKey: String)
    fun stop()
    fun isPlaying(): Boolean
}

/**
 * Platform TTS player. Android: Android TTS, iOS: AVSpeechSynthesizer
 */
expect class PlatformTTSPlayer {
    fun speak(text: String)
    fun stop()
    fun isSpeaking(): Boolean
    fun setOnDoneListener(callback: (() -> Unit)?)
}
