package co.onestep.kmp.uikit.features.recordFlow.configurations

import co.onestep.kmp.uikit.features.recordFlow.RecordFlowError
import co.onestep.kmp.uikit.features.recordFlow.ResultHandler
import co.onestep.kmp.uikit.features.summary.models.OSTSummaryOptions
import co.onestep.kmp.uikit.models.OSTActivityType
import co.onestep.kmp.uikit.models.OSTAnalyserError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Covers the Generic Recording's configuration (OS-16861), ported from the native SDK's
 * `GenericRecordingConfigurationTest`.
 *
 * The properties asserted here are the ones the PRD fixes and that a well-meaning future change
 * could quietly break: the 30-minute cap, the count-up timer, the absence of any summary, and the
 * fact that nothing about this activity leaks into another one's flow.
 */
class GenericRecordingConfigurationTest {

    @Test
    fun genericRecordingIsCappedAtThirtyMinutes() {
        // 30 minutes, matching the recorder's own hard limit. The recorder rejects anything longer,
        // so a change here would produce a configuration that cannot record.
        assertEquals(30 * 60, OSTRecordingConfiguration.GENERIC_RECORDING_DURATION_SEC)
        assertEquals(
            OSTRecordingConfiguration.GENERIC_RECORDING_DURATION_SEC,
            OSTRecordingConfiguration.genericRecording().duration,
        )
    }

    @Test
    fun genericRecordingCountsUpAndShowsNoSummary() {
        val config = OSTRecordingConfiguration.genericRecording()

        assertEquals(OSTActivityType.GENERIC_RECORDING, config.activityType)
        assertFalse(config.isCountingDown, "timer must count up, not down")
        assertSame(OSTSummaryOptions.None, config.showSummaryScreen)
    }

    @Test
    fun genericRecordingCarriesNoInstructions() {
        // OneStep does not know what is being recorded, so there is nothing to instruct — and a
        // null `instructions` is what keeps the instructions screen and sheet unreachable.
        assertNull(OSTRecordingConfiguration.genericRecording().instructions)
    }

    @Test
    fun genericRecordingPreparesWithATenSecondCountdown() {
        val prepare = OSTRecordingConfiguration.genericRecording().prepareScreenData

        assertTrue(prepare is OSTPrepareData.Duration, "expected a duration prepare, got $prepare")
        assertEquals(OSTPrepareDuration.TEN_SECONDS, prepare.prepareDuration)
    }

    @Test
    fun genericRecordingRecordsNoGeoLocation() {
        assertFalse(OSTRecordingConfiguration.genericRecording().shouldRecordGeoLocation)
    }

    @Test
    fun genericRecordingDoesNotCollectANoteOnTheGenericTaggingScreen() {
        // The note is collected by the flow's own "Recording saved" screen; enabling it here too
        // would ask for the same note twice.
        val postTagging = OSTRecordingConfiguration.genericRecording().postTaggingData
        assertTrue(
            postTagging is OSTPostTaggingData.OSTPostTaggingScreen,
            "expected a post-tagging screen, got $postTagging",
        )

        assertNull(postTagging.questions)
        assertFalse(postTagging.note == true)
        assertFalse(postTagging.footwearTag == true)
        assertFalse(postTagging.assistiveDeviceTag == true)
        assertFalse(postTagging.levelOfAssistanceTag == true)
    }

    @Test
    fun genericRecordingAsksForNoPreRecordingSelections() {
        val config = OSTRecordingConfiguration.genericRecording()

        assertNull(config.preRecordingQuestions)
        assertFalse(config.showPreRecordingAssistiveDeviceSelection)
        assertFalse(config.showPreRecordingFootwearSelection)
        // Static Balance's condition schema must not leak into this flow's pre-recording sequence.
        assertNull(config.balance)
    }

    @Test
    fun genericRecordingSerializesAsGenericRecording() {
        // The wire name the server keys the activity off. Changing it silently reclassifies every
        // recording.
        assertEquals("generic_recording", OSTActivityType.GENERIC_RECORDING.serializedName)
    }

    @Test
    fun aTooShortGenericRecordingGetsItsOwnErrorAndNotTheWalkOne() {
        // The walk short-error coaches the participant to walk in a straight line — advice for a
        // protocol OneStep does not know this recording was following.
        assertEquals(
            RecordFlowError.GenericRecordingShort,
            ResultHandler.onAnalyseError(
                analyserError = OSTAnalyserError.TooShort("measurement_too_short"),
                activityType = OSTActivityType.GENERIC_RECORDING,
                networkStatus = true,
            ),
        )
    }

    @Test
    fun noOtherActivityIsAffected() {
        // Every other activity keeps its own configuration: none of them becomes a generic
        // recording, and none of them inherits the 30-minute cap or the suppressed summary.
        val builders = listOf(
            OSTRecordingConfiguration.defaultWalk(),
            OSTRecordingConfiguration.balanceTest(),
            OSTRecordingConfiguration.staticBalance(),
            OSTRecordingConfiguration.sts(),
            OSTRecordingConfiguration.tug(),
            OSTRecordingConfiguration.romExt(),
            OSTRecordingConfiguration.stairs(),
            OSTRecordingConfiguration.sixMinuteWalk(),
            OSTRecordingConfiguration.twoMinuteWalk(),
            OSTRecordingConfiguration.dualTaskSubtract(ttsSpeechText = "count back from 100"),
        )

        builders.forEach { config ->
            assertFalse(
                config.activityType == OSTActivityType.GENERIC_RECORDING,
                "${config.activityType} must not be a generic recording",
            )
            // Only the generic recording suppresses the summary outright. (Stairs shares the
            // 30-minute cap by its own definition, so the duration is not the discriminator.)
            assertFalse(
                config.showSummaryScreen == OSTSummaryOptions.None,
                "${config.activityType} must keep its own summary option",
            )
        }

        // And a too-short recording of any other activity keeps its own error screen.
        OSTActivityType.entries
            .filter { it != OSTActivityType.GENERIC_RECORDING }
            .forEach { type ->
                assertFalse(
                    ResultHandler.onAnalyseError(
                        analyserError = OSTAnalyserError.TooShort("measurement_too_short"),
                        activityType = type,
                        networkStatus = true,
                    ) == RecordFlowError.GenericRecordingShort,
                    "$type must not get the generic-recording error screen",
                )
            }
    }
}
