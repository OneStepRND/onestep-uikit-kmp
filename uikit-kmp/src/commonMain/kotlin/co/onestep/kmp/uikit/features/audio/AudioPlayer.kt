package co.onestep.kmp.uikit.features.audio

/**
 * Platform-agnostic audio player interface.
 * Android: MediaPlayer, iOS: AVAudioPlayer
 */
internal interface AudioPlayer {
    fun enable(enable: Boolean)

    fun playAudio(resourceKey: String)

    fun stopCurrentAudio()

    fun isPlaying(): Boolean
}
