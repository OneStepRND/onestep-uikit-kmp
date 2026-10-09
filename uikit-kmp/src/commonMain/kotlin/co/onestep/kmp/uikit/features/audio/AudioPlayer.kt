package co.onestep.kmp.uikit.features.audio

/**
 * Platform-agnostic audio player interface.
 * Android: MediaPlayer, iOS: AVAudioPlayer
 */
internal interface AudioPlayer {
    fun enable(enable: Boolean)

    /**
     * [onStarted] runs on the main dispatcher once playback has begun — or once it is known that
     * nothing will play (voice-over off, unreadable clip), so a caller waiting on it never stalls.
     * It does **not** run when the load is cancelled by [stopCurrentAudio] or a newer [playAudio].
     * It is always dispatched, never called inline.
     */
    fun playAudio(resourceKey: String, onStarted: () -> Unit = {})

    fun stopCurrentAudio()

    fun isPlaying(): Boolean
}
