package co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.recording

import co.onestep.kmp.sdk.OSTEvent
import co.onestep.kmp.uikit.features.recordFlow.OSTRecordingFlowExit
import co.onestep.kmp.uikit.features.recordFlow.screens.finishOnUiTimeout
import co.onestep.kmp.uikit.features.summary.models.OSTSummaryOptions
import co.onestep.kmp.uikit.models.OSTAnalyserError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * The analyzing screen's 60-second give-up timer.
 *
 * With `showSummaryScreen = None` there is no in-flow screen to show the timeout on, so the flow
 * must end and tell the host why — otherwise the participant sits on the analyzing screen forever
 * and the host never gets an outcome. Every branch goes through the attempt's one-shot outcome.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AnalyzingUiTimeoutTest {

    private class Harness(scope: TestScope) {
        val timeout = AnalyzingUiTimeout(scope.backgroundScope)
        var expiredCount = 0
        var claimCount = 0
        var resolved = false
        val errors = mutableListOf<OSTAnalyserError>()

        // What the NavGraph does with the exit signal.
        val hostEvents = mutableListOf<OSTEvent>()
        var dismissCount = 0

        init {
            scope.backgroundScope.launch(UnconfinedTestDispatcher(scope.testScheduler)) {
                timeout.exit.collect {
                    finishOnUiTimeout(
                        onResult = { hostEvents += it },
                        onDismiss = { dismissCount++ },
                    )
                }
            }
        }

        fun start(option: OSTSummaryOptions) = timeout.start(
            summaryOption = { option },
            onExpired = { expiredCount++ },
            // The ViewModel's latch: one attempt, one outcome.
            claimOutcome = {
                claimCount++
                if (resolved) false else true.also { resolved = true }
            },
            onError = { errors += it },
        )
    }

    private fun TestScope.expire() {
        advanceTimeBy(AnalyzingUiTimeout.UI_TIMEOUT_MILLIS)
        runCurrent()
    }

    @Test
    fun noneSummaryTimeoutEndsTheFlowWithTheUiTimeoutReason() = runTest {
        val h = Harness(this)
        h.start(OSTSummaryOptions.None)

        advanceTimeBy(AnalyzingUiTimeout.UI_TIMEOUT_MILLIS - 1)
        runCurrent()
        assertEquals(0, h.expiredCount, "must not give up before 60 s")
        assertEquals(0, h.dismissCount)

        expire()

        assertEquals(1, h.expiredCount, "in-flight analysis is cancelled and the recorder reset")
        assertTrue(h.resolved, "the timeout claims the attempt's outcome, so a late result is dropped")
        assertTrue(h.errors.isEmpty(), "None has no error screen to route to")
        assertEquals(1, h.hostEvents.size, "exactly one exit event")
        val event = h.hostEvents.single()
        assertEquals(OSTRecordingFlowExit.EVENT_NAME, event.name)
        assertEquals(
            mapOf(OSTRecordingFlowExit.KEY_EXIT_REASON to OSTRecordingFlowExit.EXIT_REASON_UI_TIMEOUT),
            event.properties,
            "the reason only — no measurement or patient identifiers (HIPAA)",
        )
        assertEquals(1, h.dismissCount, "the host gets a terminal outcome")
    }

    @Test
    fun noneSummaryTimeoutAfterAnOutcomeDoesNotExitTheFlow() = runTest {
        val h = Harness(this)
        h.resolved = true // A result or error already resolved this attempt.
        h.start(OSTSummaryOptions.None)

        expire()

        assertEquals(1, h.claimCount)
        assertTrue(h.hostEvents.isEmpty(), "must not double-report over the delivered outcome")
        assertEquals(0, h.dismissCount)
        assertTrue(h.errors.isEmpty())
    }

    @Test
    fun summaryOptionsStillRouteTheTimeoutErrorScreen() = runTest {
        for (option in listOf(OSTSummaryOptions.Full, OSTSummaryOptions.WEB, OSTSummaryOptions.MINIMAL)) {
            val h = Harness(this)
            h.start(option)

            expire()

            assertIs<OSTAnalyserError.Timeout>(h.errors.single(), "for $option")
            assertTrue(h.hostEvents.isEmpty(), "for $option the error screen, not an exit")
            assertEquals(0, h.dismissCount, "for $option")
        }
    }

    @Test
    fun cancelledTimerNeverFires() = runTest {
        val h = Harness(this)
        h.start(OSTSummaryOptions.None)
        advanceTimeBy(10_000L)

        h.timeout.cancel() // The analysis outcome arrived.
        expire()

        assertEquals(0, h.expiredCount)
        assertEquals(0, h.claimCount)
        assertEquals(0, h.dismissCount)
    }

    @Test
    fun startingTwiceArmsOneTimer() = runTest {
        // FINALIZING, DONE and analyse() all arm the timer for the same attempt.
        val h = Harness(this)
        h.start(OSTSummaryOptions.None)
        advanceTimeBy(30_000L)
        h.start(OSTSummaryOptions.None)

        advanceTimeBy(30_000L)
        runCurrent()

        assertEquals(1, h.expiredCount, "the first arm's deadline holds")
        assertEquals(1, h.dismissCount)
    }
}
