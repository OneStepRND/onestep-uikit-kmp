package co.onestep.kmp.uikit.bridge

/**
 * Platform audio player. Android: MediaPlayer, iOS: AVAudioPlayer
 */
expect class PlatformAudioPlayer {
    /**
     * Plays [bytes], the full contents of an audio file already read from compose resources.
     *
     * Bytes rather than a resource key on purpose: each platform used to resolve the key against
     * a bundle the voice files are not packaged into (`res/raw` on Android, the main `NSBundle`
     * on iOS), so both silently played nothing (OS-17410). Resolution belongs to compose
     * resources, which is the one lookup that works on both platforms — see `AudioAssets`.
     */
    fun play(bytes: ByteArray)
    fun stop()
    fun isPlaying(): Boolean
}

/**
 * Platform TTS player. Android: Android TTS, iOS: AVSpeechSynthesizer
 */
expect class PlatformTTSPlayer {
    /**
     * Speaks [text] with a voice for [languageTag] (BCP-47, e.g. "en-US", "ru", "he").
     *
     * The tag is required rather than defaulted: both engines used to hardcode US English, so
     * non-English text only spoke correctly on engines that happened to fall back to a voice
     * matching its script (OS-17028). Leaving no locale-less overload is what stops a future
     * caller reintroducing that bias.
     */
    fun speak(text: String, languageTag: String)
    fun stop()
    fun isSpeaking(): Boolean
    fun setOnDoneListener(callback: (() -> Unit)?)
}
