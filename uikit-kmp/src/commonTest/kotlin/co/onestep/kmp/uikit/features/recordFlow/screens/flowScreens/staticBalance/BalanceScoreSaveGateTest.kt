package co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.staticBalance

import co.onestep.kmp.uikit.bridge.SelfReportResult
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.recording.FakeRecorderBridge
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * OS-17571: the "score wasn't saved" state machine, and when the score is self-reported at all.
 * The tapped "Recording saved" button must stay in flight until the clinician settles the dialog.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BalanceScoreSaveGateTest {

    /** A save whose outcomes are queued up front; true = the score was saved. */
    private class ScriptedSave(vararg results: Boolean) {
        private val results = ArrayDeque(results.toList())
        var calls = 0
            private set
        var hold: CompletableDeferred<Unit>? = null

        suspend fun invoke(): Boolean {
            calls++
            hold?.await()
            return results.removeFirstOrNull() ?: true
        }
    }

    @Test
    fun aSavedScoreReturnsAtOnceWithNoDialog() = runTest {
        val gate = BalanceScoreSaveGate()
        val save = ScriptedSave(true)

        gate.saveThen { save.invoke() }

        assertNull(gate.pending)
        assertEquals(1, save.calls)
    }

    @Test
    fun aFailedScoreHoldsTheButtonUntilContinue() = runTest {
        val gate = BalanceScoreSaveGate()
        val save = ScriptedSave(false)
        val button = launch { gate.saveThen { save.invoke() } }
        runCurrent()

        assertNotNull(gate.pending)
        assertTrue(button.isActive, "the button must stay in flight while the dialog is up")

        gate.continueWithoutScore()
        runCurrent()

        assertNull(gate.pending)
        assertTrue(button.isCompleted)
        assertEquals(1, save.calls)
    }

    @Test
    fun aSuccessfulRetryResendsTheSaveAndReleasesTheButton() = runTest {
        val gate = BalanceScoreSaveGate()
        val save = ScriptedSave(false, true)
        val button = launch { gate.saveThen { save.invoke() } }
        runCurrent()

        gate.tryAgain(this)
        runCurrent()

        assertEquals(2, save.calls)
        assertNull(gate.pending)
        assertFalse(gate.retrying)
        assertTrue(button.isCompleted)
    }

    @Test
    fun aFailedRetryKeepsTheDialogUp() = runTest {
        val gate = BalanceScoreSaveGate()
        val save = ScriptedSave(false, false)
        val button = launch { gate.saveThen { save.invoke() } }
        runCurrent()

        gate.tryAgain(this)
        runCurrent()

        assertNotNull(gate.pending)
        assertFalse(gate.retrying)
        assertTrue(button.isActive)

        gate.continueWithoutScore()
        runCurrent()
        assertTrue(button.isCompleted)
    }

    @Test
    fun aRetryInFlightIgnoresASecondTryAgainAndContinue() = runTest {
        val gate = BalanceScoreSaveGate()
        val save = ScriptedSave(false, true)
        val button = launch { gate.saveThen { save.invoke() } }
        runCurrent()

        val held = CompletableDeferred<Unit>()
        save.hold = held
        assertNotNull(gate.tryAgain(this))
        runCurrent()
        assertTrue(gate.retrying)

        assertNull(gate.tryAgain(this), "a second tap must not start a second retry")
        gate.continueWithoutScore()
        runCurrent()
        assertNotNull(gate.pending, "Continue must not leave mid-retry")

        held.complete(Unit)
        runCurrent()

        assertEquals(2, save.calls)
        assertNull(gate.pending)
        assertTrue(button.isCompleted)
    }

    @Test
    fun aCancelledButtonTakesTheDialogDown() = runTest {
        val gate = BalanceScoreSaveGate()
        val button = launch { gate.saveThen { false } }
        runCurrent()
        assertNotNull(gate.pending)

        button.cancel()
        runCurrent()

        assertNull(gate.pending)
    }

    // --- selfReportBalanceScore ------------------------------------------------------------

    @Test
    fun noScoreReportsNothing() = runTest {
        val bridge = FakeRecorderBridge().apply { supportsBalanceScoreSelfReport = true }

        assertTrue(bridge.selfReportBalanceScore("m-1", score = null))
        assertTrue(bridge.selfReportCalls.isEmpty())
    }

    @Test
    fun aPlatformThatCannotReportAScoreSkipsItWithoutFailing() = runTest {
        val bridge = FakeRecorderBridge().apply { supportsBalanceScoreSelfReport = false }

        assertTrue(bridge.selfReportBalanceScore("m-1", score = 0))
        assertTrue(bridge.selfReportCalls.isEmpty())
    }

    @Test
    fun theScoreIsReportedAloneAsTheBalanceScore() = runTest {
        val bridge = FakeRecorderBridge().apply { supportsBalanceScoreSelfReport = true }

        assertTrue(bridge.selfReportBalanceScore("m-1", score = 0))
        assertEquals(
            listOf(FakeRecorderBridge.SelfReportCall("m-1", stsRepetitions = null, balanceScore = 0)),
            bridge.selfReportCalls,
        )
    }

    @Test
    fun aRejectedReportOrNoMeasurementFails() = runTest {
        val bridge = FakeRecorderBridge().apply {
            supportsBalanceScoreSelfReport = true
            selfReportResults += SelfReportResult.NetworkFailure
        }

        assertFalse(bridge.selfReportBalanceScore("m-1", score = 0))
        assertFalse(bridge.selfReportBalanceScore(measurementId = null, score = 0))
        assertEquals(1, bridge.selfReportCalls.size)
    }
}
