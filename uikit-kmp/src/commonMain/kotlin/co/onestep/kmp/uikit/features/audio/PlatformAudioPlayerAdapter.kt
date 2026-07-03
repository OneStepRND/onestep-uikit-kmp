package co.onestep.kmp.uikit.features.audio

import co.onestep.kmp.uikit.bridge.PlatformAudioPlayer

/**
 * Adapts [PlatformAudioPlayer] (expect class exposed via UIKitServiceLocator)
 * to the internal [AudioPlayer] interface used by ViewModels.
 */
internal class PlatformAudioPlayerAdapter(
    private val platform: PlatformAudioPlayer,
) : AudioPlayer {
    private var enabled = true

    override fun enable(enable: Boolean) {
        enabled = enable
    }

    override fun playAudio(resourceKey: String) {
        if (enabled) platform.play(resourceKey)
    }

    override fun stopCurrentAudio() {
        platform.stop()
    }

    override fun isPlaying(): Boolean = platform.isPlaying()
}
