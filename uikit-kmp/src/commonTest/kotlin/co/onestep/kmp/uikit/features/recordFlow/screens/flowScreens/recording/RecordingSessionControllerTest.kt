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
 * Acceptance tests for the ViewModel-owned stop model: the recorder always starts with a
 * grace-padded backstop duration, and the controller — not the SDK — stops the recording when
 * the activity time elapses (wall-clock from "go") or the caller asks.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RecordingSessionControllerTest {

    private fun TestScope.controller(bridge: FakeRecorderBridge) =
        RecordingSessionController(
            recorderBridge = bridge,
            scope = backgroundScope,
            now = { testScheduler.currentTime },
        )

    private fun request(
        activityType: OSTActivityType = OSTActivityType.WALK,
        activitySeconds: Int? = 30,
        prepareSeconds: Int = 0,
    ) = RecordingSessionController.StartRequest(
        activityType = activityType,
        activityDurationSeconds = activitySeconds,
        prepareDurationSeconds = prepareSeconds,
    )

    // --- Timed activity, started at "go" ---------------------------------------------------

    @Test
    fun timedActivityStartsWithGracePaddedBackstopAndAutoStopsAtActivityDuration() = runTest {
        val bridge = FakeRecorderBridge()
        val session = controller(bridge)

        session.onGo { request(activitySeconds = 30) }

        // SDK gets activity + grace as a suspension backstop only.
        assertEquals(
            listOf(FakeRecorderBridge.StartCall(OSTActivityType.WALK, 40_000L)),
            bridge.startCalls,
        )
        assertTrue(session.isRecording)

        advanceTimeBy(29_999)
        runCurrent()
        assertEquals(0, bridge.stopCount, "must not stop before the activity duration elapsed")

        advanceTimeBy(1)
        runCurrent()
        assertEquals(1, bridge.stopCount, "controller stops the recorder at go + activity")
        assertFalse(session.isRecording)
    }

    @Test
    fun clockTicksWholeSecondsFromGo() = runTest {
        val bridge = FakeRecorderBridge()
        val session = controller(bridge)
        val observed = mutableListOf<Int>()
        backgroundScope.launch { session.elapsedSeconds.collect { observed += it } }

        session.onGo { request(activitySeconds = 30) }
        advanceTimeBy(3_000)
        runCurrent()

        assertEquals(listOf(0, 1, 2, 3), observed)
    }

    // --- Unrestricted recording ------------------------------------------------------------

    @Test
    fun unrestrictedRecordingStartsWithNullDurationAndNeverAutoStops() = runTest {
        val bridge = FakeRecorderBridge()
        val session = controller(bridge)

        session.onGo { request(activitySeconds = null) }

        assertEquals(null, bridge.startCalls.single().durationMs)

        advanceTimeBy(30 * 60 * 1000L)
        runCurrent()
        assertEquals(0, bridge.stopCount)
        assertTrue(session.isRecording)
    }

    // --- TUG/STS early start ---------------------------------------------------------------

    @Test
    fun earlyStartBackstopCoversCountdownAndGoMarkerIsWrittenAtGo() = runTest {
        val bridge = FakeRecorderBridge()
        val session = controller(bridge)

        session.startEarly(request(OSTActivityType.TUG, activitySeconds = 180, prepareSeconds = 10))
        runCurrent()

        // 10 s countdown + 180 s activity + 10 s grace.
        assertEquals(200_000L, bridge.startCalls.single().durationMs)
        assertTrue(bridge.markers.isEmpty(), "no marker until the activity actually begins")
        assertFalse(session.isRecording, "countdown is not the activity")

        advanceTimeBy(10_000) // full Get Ready countdown
        session.onGo { error("recorder already started early — request must not be rebuilt") }

        assertEquals(listOf(RecordingSessionController.GO_MARKER), bridge.markers)
        assertTrue(session.isRecording)

        // Auto-stop lands activity-duration after GO, not after recorder start.
        advanceTimeBy(179_999)
        runCurrent()
        assertEquals(0, bridge.stopCount)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(1, bridge.stopCount)
    }

    @Test
    fun startNowSkippingCountdownStillStopsAtGoPlusActivity() = runTest {
        val bridge = FakeRecorderBridge()
        val session = controller(bridge)

        session.startEarly(request(OSTActivityType.STS, activitySeconds = 30, prepareSeconds = 10))
        runCurrent()

        advanceTimeBy(3_000) // user taps "Start now" 3 s into the 10 s countdown
        session.onGo { error("recorder already started early") }

        advanceTimeBy(29_999)
        runCurrent()
        assertEquals(0, bridge.stopCount)
        advanceTimeBy(1)
        runCurrent()
        // Stopped at go + 30 s (t = 33 s), well before the SDK backstop at 50 s.
        assertEquals(1, bridge.stopCount)
    }

    // --- Caller-initiated stop / teardown --------------------------------------------------

    @Test
    fun manualStopStopsOnceAndSuppressesTheLaterDeadlineStop() = runTest {
        val bridge = FakeRecorderBridge()
        val session = controller(bridge)

        session.onGo { request(activitySeconds = 30) }
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
        val bridge = FakeRecorderBridge()
        val session = controller(bridge)

        session.startEarly(request(OSTActivityType.TUG, activitySeconds = 180, prepareSeconds = 10))
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

        session.startEarly(request(OSTActivityType.STS, activitySeconds = 30, prepareSeconds = 10))
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
        val bridge = FakeRecorderBridge()
        val session = controller(bridge)

        session.onGo { request(activitySeconds = 30) }
        // The SDK reported the recording over on its own (backstop/cap) — the ViewModel's
        // recorder-state collector calls onRecordingFinished().
        session.onRecordingFinished()

        session.stopAndAwaitDone(reset = true)

        assertEquals(0, bridge.stopCount)
        assertEquals(0, bridge.resetCount)
        assertFalse(session.recorderStartInitiated)
    }
}
