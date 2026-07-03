package co.onestep.kmp.uikit.features.audio

import co.onestep.kmp.uikit.bridge.PlatformTTSPlayer

/**
 * Adapts [PlatformTTSPlayer] (expect class exposed via UIKitServiceLocator)
 * to the internal [TTSPlayer] interface used by ViewModels.
 */
internal class PlatformTTSPlayerAdapter(
    private val platform: PlatformTTSPlayer,
) : TTSPlayer {
    private var enabled = true

    override fun enable(enable: Boolean) {
        enabled = enable
    }

    override fun speak(text: String) {
        if (enabled) platform.speak(text)
    }

    override fun stopCurrentSpeech() {
        platform.stop()
    }

    override fun isSpeaking(): Boolean = platform.isSpeaking()

    override fun setOnDoneListener(callback: (() -> Unit)?) {
        platform.setOnDoneListener(callback)
    }
}
