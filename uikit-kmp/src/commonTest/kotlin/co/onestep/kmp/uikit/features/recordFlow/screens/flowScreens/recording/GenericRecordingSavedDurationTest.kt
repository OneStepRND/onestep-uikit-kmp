package co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.recording

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The Generic Recording "Recording saved" card must read what the timer showed at the stop. The
 * SDK rounds its captured span (20.6s -> 21) while the count-up timer floors (00:20), so the card
 * used to claim a second the participant never saw (iOS UI kit `6690111`).
 */
class GenericRecordingSavedDurationTest {

    @Test
    fun aStopMidSecond_readsWhatTheTimerShowed() {
        val onTheTimer = floorElapsedSeconds(20_600)

        assertEquals(20, onTheTimer)
        assertEquals(20, genericRecordingSavedSeconds(stoppedAtElapsedSeconds = onTheTimer, measuredSeconds = 21))
    }

    @Test
    fun theFirstSecond_readsZero_andAnAutoStopReadsTheFullWindow() {
        assertEquals(0, floorElapsedSeconds(999))
        assertEquals(1_800, floorElapsedSeconds(30 * 60 * 1_000L))
    }

    @Test
    fun withoutACapturedStop_fallsBackToTheMeasuredSpan_thenZero() {
        assertEquals(21, genericRecordingSavedSeconds(stoppedAtElapsedSeconds = null, measuredSeconds = 21))
        assertEquals(0, genericRecordingSavedSeconds(stoppedAtElapsedSeconds = null, measuredSeconds = null))
    }
}
