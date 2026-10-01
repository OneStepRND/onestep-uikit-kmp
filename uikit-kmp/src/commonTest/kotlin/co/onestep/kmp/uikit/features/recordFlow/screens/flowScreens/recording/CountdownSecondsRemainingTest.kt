package co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.recording

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

/** `Clicked: start_measurement_now` `time_remaining`, measured like Android uikit's VM. */
class CountdownSecondsRemainingTest {

    @Test
    fun keepsTheFraction_ratherThanTheTimerText() {
        assertEquals(6.75, countdownSecondsRemaining(totalSeconds = 10, elapsed = 3_250.milliseconds))
    }

    @Test
    fun anUntouchedCountdown_hasItsFullLengthLeft() {
        assertEquals(10.0, countdownSecondsRemaining(totalSeconds = 10, elapsed = 0.milliseconds))
    }

    @Test
    fun theGoFrame_isFlooredAtZero() {
        // The "GO" frame is held 1.5 s past the last counted second.
        assertEquals(0.0, countdownSecondsRemaining(totalSeconds = 10, elapsed = 11_000.milliseconds))
    }
}
