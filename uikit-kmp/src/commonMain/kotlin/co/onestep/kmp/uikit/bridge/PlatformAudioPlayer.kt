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
