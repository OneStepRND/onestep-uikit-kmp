package co.onestep.kmp.uikit.features.audio

import co.onestep.kmp.uikit.bridge.PlatformAudioPlayer
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Adapts [PlatformAudioPlayer] (expect class exposed via UIKitServiceLocator)
 * to the internal [AudioPlayer] interface used by ViewModels.
 *
 * Reading a compose resource is suspending, so playback starts one dispatch after the call.
 * [isPlaying] reports that in-flight load as playing: callers use it to ask "is a voice-over
 * already running?" before starting another (see `handleAnalyzingStart`), and answering "no"
 * during the load would let two of them overlap.
 */
internal class PlatformAudioPlayerAdapter(
    private val platform: PlatformAudioPlayer,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob()),
) : AudioPlayer {
    private var enabled = true
    private var loadJob: Job? = null

    override fun enable(enable: Boolean) {
        enabled = enable
    }

    override fun playAudio(resourceKey: String) {
        if (!enabled) return
        val path = AudioAssets.pathFor(resourceKey)
        loadJob?.cancel()
        loadJob = scope.launch {
            val bytes = try {
                Res.readBytes(path)
            } catch (e: Exception) {
                // A missing/renamed asset must not take the flow down — audio is non-critical.
                // It is logged rather than swallowed because the symptom (silence) is otherwise
                // indistinguishable from voice-over being switched off.
                println("PlatformAudioPlayerAdapter: cannot read audio resource $path: ${e.message}")
                return@launch
            }
            platform.play(bytes)
        }
    }

    override fun stopCurrentAudio() {
        loadJob?.cancel()
        loadJob = null
        platform.stop()
    }

    override fun isPlaying(): Boolean = loadJob?.isActive == true || platform.isPlaying()
}
