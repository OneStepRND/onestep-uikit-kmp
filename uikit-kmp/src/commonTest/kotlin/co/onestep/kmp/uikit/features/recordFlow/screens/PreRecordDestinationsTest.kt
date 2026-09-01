package co.onestep.kmp.uikit.features.recordFlow.screens

import co.onestep.kmp.uikit.bridge.PermissionStatus
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingConfiguration
import co.onestep.kmp.uikit.features.recordFlow.destinations.CustomTagsDestination
import co.onestep.kmp.uikit.features.recordFlow.destinations.SoundInstructionsDestination
import co.onestep.kmp.uikit.features.recordFlow.destinations.StartRecordDestination
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingQuestionData
import co.onestep.kmp.uikit.models.OSTActivityType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * The pre-recording screen sequence — which screens a flow shows before it records, and which screen
 * it records *from*.
 *
 * Worth pinning because the sequence is otherwise only observable by running the flow on a device:
 * the list is assembled once per configuration and every "start over" path (an analysis error's
 * "Try again", abandoning a recording) navigates back to whichever destination ends it.
 */
class PreRecordDestinationsTest {

    private fun destinationsFor(
        config: OSTRecordingConfiguration,
        micStatus: PermissionStatus = PermissionStatus.GRANTED,
        showSoundInstructions: Boolean = false,
    ) = buildPreRecordDestinations(
        config = config,
        micStatus = { micStatus },
        showSoundInstructions = { showSoundInstructions },
    )

    @Test
    fun genericRecordingRecordsWithNoStartScreenInFront() {
        // A big "Start" button beside a "View instructions" link is the one screen this activity
        // cannot honour: there are no instructions to open, and the recording screen opens on its
        // own "Get ready" countdown, so the extra tap asked for nothing.
        val destinations = destinationsFor(OSTRecordingConfiguration.genericRecording())

        assertEquals(listOf(RecordingDestination), destinations)
        assertFalse(
            StartRecordDestination in destinations,
            "Generic Recording must not be preceded by the Start screen",
        )
    }

    @Test
    fun genericRecordingStillReachesTheRecordingWhenAScreenPrecedesIt() {
        // The sound-instructions screen is the one pre-recording screen this activity can show (it
        // plays voice-over), and it advances to its successor in this list — so dropping the Start
        // screen must leave the recording as that successor, or the flow dead-ends there.
        val destinations = destinationsFor(
            OSTRecordingConfiguration.genericRecording(),
            showSoundInstructions = true,
        )

        assertEquals(listOf(SoundInstructionsDestination, RecordingDestination), destinations)
    }

    @Test
    fun everyOtherActivityStillEndsOnTheStartScreen() {
        // The Start screen is where an ordinary measurement's instructions are read, so removing it
        // for Generic Recording must not remove it for anything else.
        OSTActivityType.entries
            .filter { it != OSTActivityType.GENERIC_RECORDING }
            .forEach { activityType ->
                val destinations = destinationsFor(timedConfig(activityType))

                assertSame(
                    StartRecordDestination,
                    destinations.last(),
                    "$activityType must still record from the Start screen",
                )
            }
    }

    @Test
    fun preRecordingQuestionsStillPrecedeTheStartScreen() {
        // Guards the extraction itself: the ordering rules moved out of the composable unchanged.
        val destinations = destinationsFor(
            timedConfig(OSTActivityType.WALK).copy(
                preRecordingQuestions = listOf(
                    OSTRecordingQuestionData(
                        title = "Which shoes?",
                        tagsValues = listOf("Apos shoes", "Own shoes"),
                    ),
                ),
            ),
        )

        assertEquals(listOf(CustomTagsDestination, StartRecordDestination), destinations)
        assertTrue(destinations.indexOf(CustomTagsDestination) < destinations.lastIndex)
    }

    /** A duration, so the walk-duration picker (which a null duration inserts) stays out of the way. */
    private fun timedConfig(activityType: OSTActivityType) = OSTRecordingConfiguration(
        activityType = activityType,
        duration = 60,
        isCountingDown = true,
        playVoiceOver = false,
    )
}
