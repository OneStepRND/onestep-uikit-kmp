package co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.recording

import co.onestep.kmp.uikit.models.OSTActivityType
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Acceptance tests for the recording clock and the stop.
 *
 * The recorder is started with the exact deadline the measurement should end at — including the
 * Get Ready countdown when capture starts early — so the SDK owns recording time. The controller
 * renders from absolute anchors and only stops the recording itself when "go" arrived earlier than
 * the predicted countdown, or when the platform SDK publishes no recording window.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RecordingSessionControllerTest {

    /** A bridge whose window tracks the test clock — i.e. a platform SDK that publishes one. */
    private fun TestScope.bridgeWithWindow() = FakeRecorderBridge().apply {
        monotonicNow = { testScheduler.currentTime }
    }

    private fun TestScope.controller(bridge: FakeRecorderBridge) =
        RecordingSessionController(
            recorderBridge = bridge,
            scope = backgroundScope,
            now = { testScheduler.currentTime },
            monotonicNow = { testScheduler.currentTime },
        )

    private fun request(
        activityType: OSTActivityType = OSTActivityType.WALK,
        activityMillis: Long? = 30_000L,
        prepareOffsetMillis: Long = 0L,
    ) = RecordingSessionController.StartRequest(
        activityType = activityType,
        activityDurationMillis = activityMillis,
        prepareOffsetMillis = prepareOffsetMillis,
    )

    // --- The deadline handed to the recorder ------------------------------------------------

    @Test
    fun startedAtGoPassesExactlyTheMeasurementDuration() = runTest {
        val bridge = bridgeWithWindow()
        val session = controller(bridge)

        session.onGo { request(activityMillis = 30_000L) }

        assertEquals(
            listOf(FakeRecorderBridge.StartCall(OSTActivityType.WALK, 30_000L)),
            bridge.startCalls,
        )
        assertTrue(session.isRecording)
    }

    @Test
    fun earlyStartAddsTheCountdownSoTheDeadlineLandsAtGoPlusDuration() = runTest {
        val bridge = bridgeWithWindow()
        val session = controller(bridge)

        // TUG: 3 min measurement behind a 10 s countdown whose "GO" frame runs 1.5 s.
        session.startEarly(
            request(OSTActivityType.TUG, activityMillis = 180_000L, prepareOffsetMillis = 11_500L),
        )
        runCurrent()

        assertEquals(191_500L, bridge.startCalls.single().durationMs)
        assertTrue(bridge.markers.isEmpty(), "no marker until the activity actually begins")
        assertFalse(session.isRecording, "the countdown is not the activity")

        advanceTimeBy(11_500) // the full Get Ready countdown
        session.onGo { error("recorder already started early — request must not be rebuilt") }

        assertEquals(listOf(RecordingSessionController.GO_MARKER), bridge.markers)
        assertTrue(session.isRecording)

        // The recorder's own deadline now falls 3 min after "go" — so the measurement is its full
        // length even if this controller never runs again (app suspended, screen closed).
        val window = bridge.currentRecordingWindow.value!!
        assertEquals(191_500L, window.totalMillis)
        assertEquals(180_000L, window.remainingMillisAt(testScheduler.currentTime))

        // Nothing ends the recording early.
        advanceTimeBy(179_999)
        runCurrent()
        assertEquals(0, bridge.stopCount)
    }

    @Test
    fun durationIsClampedToTheRecorderLimit() = runTest {
        val bridge = bridgeWithWindow() // limit is 6 min
        val session = controller(bridge)

        session.startEarly(
            request(OSTActivityType.TUG, activityMillis = 6 * 60_000L, prepareOffsetMillis = 11_500L),
        )
        runCurrent()

        assertEquals(6 * 60_000L, bridge.startCalls.single().durationMs)
    }

    @Test
    fun unrestrictedRecordingStartsWithNullDurationAndNeverAutoStops() = runTest {
        val bridge = bridgeWithWindow()
        val session = controller(bridge)

        session.onGo { request(activityMillis = null) }

        assertEquals(null, bridge.startCalls.single().durationMs)

        advanceTimeBy(30 * 60 * 1000L)
        runCurrent()
        assertEquals(0, bridge.stopCount)
        assertTrue(session.isRecording)
        assertEquals(30 * 60 * 1000L, session.elapsedMillis.value)
    }

    // --- The clock --------------------------------------------------------------------------

    @Test
    fun clockMeasuresFromGoNotFromSensorStart() = runTest {
        val bridge = bridgeWithWindow()
        val session = controller(bridge)

        session.startEarly(
            request(OSTActivityType.TUG, activityMillis = 180_000L, prepareOffsetMillis = 11_500L),
        )
        runCurrent()
        advanceTimeBy(11_500)

        session.onGo { error("already started") }
        runCurrent()
        assertEquals(0L, session.elapsedMillis.value, "TUG must read 00:00 at 'go', not 00:11")

        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(60_000L, session.elapsedMillis.value)
    }

    @Test
    fun clockSelfCorrectsAcrossASuspendedTick() = runTest {
        val bridge = bridgeWithWindow()
        val session = controller(bridge)

        session.onGo { request(activityMillis = 120_000L) }
        // One long jump — a backgrounded app, a screen-off, a stalled dispatcher. A clock that
        // incremented per tick would be seconds behind; this one recomputes.
        advanceTimeBy(45_000)
        runCurrent()

        assertEquals(45_000L, session.elapsedMillis.value)
    }

    // --- Stopping ---------------------------------------------------------------------------

    @Test
    fun startNowTrimsTheSurplusTheSdkDeadlineCannotKnowAbout() = runTest {
        val bridge = bridgeWithWindow()
        val session = controller(bridge)

        session.startEarly(
            request(OSTActivityType.STS, activityMillis = 30_000L, prepareOffsetMillis = 11_500L),
        )
        runCurrent()

        // The user taps "Start now" 3 s in, so the SDK's deadline is 8.5 s later than intended.
        advanceTimeBy(3_000)
        session.onGo { error("recorder already started early") }

        advanceTimeBy(29_999)
        runCurrent()
        assertEquals(0, bridge.stopCount)

        advanceTimeBy(RecordingSessionController.CLOCK_TICK_MILLIS)
        runCurrent()
        assertEquals(1, bridge.stopCount, "the measurement must still be 30 s after 'go'")
        assertFalse(session.isRecording)
    }

    // --- Re-anchoring the SDK's auto-stop at "go" (OS-16818/OS-16814) -------------------------

    @Test
    fun startNowReAnchorsTheRecorderDeadlineToGo() = runTest {
        val bridge = bridgeWithWindow()
        val session = controller(bridge)

        // TUG behind a 10 s countdown: capture opens against the provisional 191 500 ms deadline.
        session.startEarly(
            request(OSTActivityType.TUG, activityMillis = 180_000L, prepareOffsetMillis = 11_500L),
        )
        runCurrent()
        assertEquals(191_500L, bridge.startCalls.single().durationMs)

        // The user cuts the countdown short 3 s in.
        advanceTimeBy(3_000)
        session.onGo { error("recorder already started early") }
        runCurrent()

        // The recorder — not just this controller — now stops 3 min after "go", so the recording
        // is its intended length even if the app is suspended and the clock below never runs.
        assertEquals(listOf(180_000L), bridge.rescheduleCalls)
        val window = bridge.currentRecordingWindow.value!!
        assertEquals(180_000L, window.remainingMillisAt(testScheduler.currentTime))
        assertEquals(
            0L,
            window.startedAtMonotonicMillis,
            "capture really began at the countdown — only the deadline moves",
        )

        // OS-16814: the count-up display must move immediately, not sit frozen on 00:00 for the
        // unused remainder of the countdown.
        assertEquals(0L, session.elapsedMillis.value)
        advanceTimeBy(5_000)
        runCurrent()
        assertEquals(5_000L, session.elapsedMillis.value)
    }

    @Test
    fun theGoMarkerAndTheReAnchorBothWaitForTheRecorderToBeRecording() = runTest {
        // "Start now" tapped while start() is still in flight — the SDK drops both the marker and
        // the re-anchor unless it has reached RECORDING.
        val bridge = bridgeWithWindow()
        val gate = CompletableDeferred<Unit>()
        bridge.startGate = gate
        val session = controller(bridge)

        session.startEarly(
            request(OSTActivityType.TUG, activityMillis = 180_000L, prepareOffsetMillis = 11_500L),
        )
        runCurrent()

        val go = launch { session.onGo { error("recorder already started early") } }
        runCurrent()
        assertTrue(bridge.calls.isEmpty(), "nothing may reach a recorder that has not started")

        gate.complete(Unit)
        go.join()

        assertEquals(listOf("start", "marker:go", "reschedule"), bridge.calls)
    }

    @Test
    fun aCountdownRunToCompletionLandsOnTheSameDeadlineItAlreadyHad() = runTest {
        val bridge = bridgeWithWindow()
        val session = controller(bridge)

        session.startEarly(
            request(OSTActivityType.TUG, activityMillis = 180_000L, prepareOffsetMillis = 11_500L),
        )
        runCurrent()
        advanceTimeBy(11_500) // the countdown runs out on its own

        session.onGo { error("recorder already started early") }
        runCurrent()

        // Re-anchoring is arithmetically a no-op on this path: the prediction was exact.
        assertEquals(listOf(180_000L), bridge.rescheduleCalls)
        assertEquals(191_500L, bridge.currentRecordingWindow.value!!.willEndAtMonotonicMillis)
    }

    @Test
    fun aRecorderStartedAtGoIsNeverReAnchored() = runTest {
        val bridge = bridgeWithWindow()
        val session = controller(bridge)

        // No early start, so the recorder was handed exactly the measurement duration already.
        session.onGo { request(activityMillis = 30_000L) }
        runCurrent()

        assertTrue(bridge.rescheduleCalls.isEmpty())
    }

    @Test
    fun anUnrestrictedEarlyStartHasNoDeadlineToReAnchor() = runTest {
        val bridge = bridgeWithWindow()
        val session = controller(bridge)

        session.startEarly(
            request(OSTActivityType.TUG, activityMillis = null, prepareOffsetMillis = 11_500L),
        )
        runCurrent()
        advanceTimeBy(3_000)

        session.onGo { error("recorder already started early") }
        runCurrent()

        assertEquals(null, bridge.startCalls.single().durationMs)
        assertTrue(bridge.rescheduleCalls.isEmpty())
    }

    @Test
    fun withoutAnSdkWindowTheControllerStopsAtGoPlusDuration() = runTest {
        // iOS today: the SDK publishes no recording window (OS-16749).
        val bridge = FakeRecorderBridge()
        val session = controller(bridge)

        session.onGo { request(activityMillis = 30_000L) }

        advanceTimeBy(29_999)
        runCurrent()
        assertEquals(0, bridge.stopCount, "must not stop before the measurement duration elapsed")

        advanceTimeBy(RecordingSessionController.CLOCK_TICK_MILLIS)
        runCurrent()
        assertEquals(1, bridge.stopCount)
        assertFalse(session.isRecording)
        assertEquals(30_000L, session.elapsedMillis.value, "elapsed never exceeds the duration")
    }

    @Test
    fun manualStopStopsOnceAndSuppressesTheLaterDeadlineStop() = runTest {
        val bridge = FakeRecorderBridge()
        val session = controller(bridge)

        session.onGo { request(activityMillis = 30_000L) }
        advanceTimeBy(5_000)
        session.stopAndAwaitDone()

        assertEquals(1, bridge.stopCount)
        assertFalse(session.isRecording)

        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(1, bridge.stopCount, "the dead clock must not fire a second stop")
    }

    @Test
    fun teardownDuringEarlyStartStopsAndResetsTheRecorder() = runTest {
        val bridge = bridgeWithWindow()
        val session = controller(bridge)

        session.startEarly(
            request(OSTActivityType.TUG, activityMillis = 180_000L, prepareOffsetMillis = 11_500L),
        )
        runCurrent()
        advanceTimeBy(4_000) // user exits mid-countdown

        session.stopAndAwaitDone(reset = true)

        assertEquals(1, bridge.stopCount)
        assertEquals(1, bridge.resetCount)
        assertTrue(bridge.markers.isEmpty())
    }

    @Test
    fun stopNeverOvertakesAnInFlightStart() = runTest {
        val bridge = FakeRecorderBridge()
        val gate = CompletableDeferred<Unit>()
        bridge.startGate = gate
        val session = controller(bridge)

        session.startEarly(
            request(OSTActivityType.STS, activityMillis = 30_000L, prepareOffsetMillis = 11_500L),
        )
        runCurrent() // start() is now suspended on the gate

        val teardown = launch { session.stopAndAwaitDone(reset = true) }
        runCurrent()
        assertEquals(0, bridge.stopCount, "stop must wait for the in-flight start")

        gate.complete(Unit)
        teardown.join()
        assertEquals(listOf("start", "stop", "reset"), bridge.calls)
    }

    @Test
    fun stopAfterRecordingFinishedIsANoOp() = runTest {
        val bridge = bridgeWithWindow()
        val session = controller(bridge)

        session.onGo { request(activityMillis = 30_000L) }
        // The SDK reported the recording over on its own — the ViewModel's recorder-state
        // collector calls onRecordingFinished().
        session.onRecordingFinished()

        session.stopAndAwaitDone(reset = true)

        assertEquals(0, bridge.stopCount)
        assertEquals(0, bridge.resetCount)
        assertFalse(session.recorderStartInitiated)
    }
}
