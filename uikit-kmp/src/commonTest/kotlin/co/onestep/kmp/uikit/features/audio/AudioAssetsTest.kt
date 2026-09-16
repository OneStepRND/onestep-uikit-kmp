package co.onestep.kmp.uikit.features.audio

import co.onestep.kmp.uikit_kmp.generated.resources.Res
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Every voice-over key the flow can ask for must resolve to a file that is actually packaged.
 *
 * This is the check that was missing when OS-17410 shipped: the keys were fine, the files were
 * present, and nothing connected the two — both platforms looked the clips up in a bundle they
 * are not packaged into, so every voice-over was silence with no error anywhere. Reading the
 * bytes is the only assertion that fails when that link breaks again, whether because an asset is
 * renamed, dropped, or re-encoded to a new extension.
 */
class AudioAssetsTest {

    /**
     * Keyed exactly as [MotionRecorderViewModel.localizedAudioKey] and `startRecordAudioKey`
     * build them, across every language the flow is localized in.
     */
    private val allKeys = listOf(
        // Countdown — Get Ready. No Russian variant exists for the 5-second countdown, so it
        // deliberately falls back to the English clip.
        "countdown_from_5", "countdown_from_5_iw",
        "countdown_from_10", "countdown_from_10_ru", "countdown_from_10_iw",
        // Recording stopped.
        "recording_stopped", "recording_stopped_ru", "recording_stopped_iw",
        // Analysis ready — the Russian asset really is spelled "read", and Hebrew uses "_heb".
        "data_is_ready_for_analysis", "data_is_read_for_analysis_ru", "data_is_ready_for_analysis_heb",
        // Start screen voice-over.
        "tap_the_start_button_vo", "tap_the_start_button_ru_vo", "tap_the_start_button_iw_vo",
    )

    @Test
    fun everyVoiceOverKeyResolvesToAPackagedFile() = runTest {
        allKeys.forEach { key ->
            val path = AudioAssets.pathFor(key)
            val bytes = Res.readBytes(path)
            assertTrue(bytes.isNotEmpty(), "Audio asset '$path' (key '$key') is packaged but empty")
        }
    }
}
