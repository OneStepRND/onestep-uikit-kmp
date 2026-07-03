package co.onestep.kmp.uikit.features.audio

/**
 * Platform-agnostic text-to-speech player interface.
 * Android: Android TTS, iOS: AVSpeechSynthesizer
 */
internal interface TTSPlayer {
    fun enable(enable: Boolean)

    fun speak(text: String)

    fun stopCurrentSpeech()

    fun isSpeaking(): Boolean

    fun setOnDoneListener(callback: (() -> Unit)?)
}
