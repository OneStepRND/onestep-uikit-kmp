package co.onestep.kmp.uikit.features.recordFlow.configurations

import co.onestep.kmp.uikit.features.summary.models.OSTSummaryOptions
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Covers [collectsPostRecordingNote] — the flag the Static Balance "Recording saved" screen reads to
 * decide whether to offer a note (OS-16914).
 *
 * The defect this guards: `staticBalance()` has always declared `note = true` *and* explained that
 * the note is collected on that screen rather than the generic tagging screen — but the screen never
 * read the flag, so a host switching it off still got the field. A blinded research workspace is the
 * case that matters, because there the clinician can never see the note again.
 */
class PostRecordingNoteTest {

    @Test
    fun staticBalanceCollectsANoteByDefault() {
        // An ordinary clinic: the note reaches the web summary and the care log, so it is wanted.
        assertTrue(OSTRecordingConfiguration.staticBalance().collectsPostRecordingNote())
    }

    @Test
    fun aBlindedHostCollectsNoNote() {
        // What the Clinician app sends for a research workspace: no analysis, and nothing asked
        // afterwards. Both halves have to travel, which is why the host copies postTaggingData too.
        val blinded = OSTRecordingConfiguration.staticBalance().copy(
            showSummaryScreen = OSTSummaryOptions.None,
            postTaggingData = OSTPostTaggingData.None(emptyList()),
        )

        assertFalse(blinded.collectsPostRecordingNote())
    }

    @Test
    fun noteFalseOnTheTaggingScreenIsHonoured() {
        // The per-row switch, distinct from None: the screen exists, the note row does not.
        val noNote = OSTRecordingConfiguration.staticBalance().copy(
            postTaggingData = OSTPostTaggingData.OSTPostTaggingScreen(
                questions = null,
                assistiveDeviceTag = false,
                levelOfAssistanceTag = false,
                footwearTag = false,
                note = false,
            ),
        )

        assertFalse(noNote.collectsPostRecordingNote())
    }

    @Test
    fun genericRecordingKeepsItsOwnNoteScreenRegardless() {
        // Generic Recording ships `note = false` on a postTaggingData its own notes screen was never
        // meant to read: the recording is never analysed, so the note is the only description of
        // what was captured. This asserts the flag reads false — i.e. that anyone wiring
        // [collectsPostRecordingNote] into that screen would be removing its reason to exist.
        assertFalse(OSTRecordingConfiguration.genericRecording().collectsPostRecordingNote())
    }
}
