package co.onestep.kmp.uikit.features.audio

/**
 * Platform-agnostic text-to-speech player interface.
 * Android: Android TTS, iOS: AVSpeechSynthesizer
 */
internal interface TTSPlayer {
    fun enable(enable: Boolean)

    /** Speaks [text] with a voice for [languageTag] (BCP-47). See `PlatformTTSPlayer.speak`. */
    fun speak(text: String, languageTag: String)

    fun stopCurrentSpeech()

    fun isSpeaking(): Boolean

    fun setOnDoneListener(callback: (() -> Unit)?)
}
