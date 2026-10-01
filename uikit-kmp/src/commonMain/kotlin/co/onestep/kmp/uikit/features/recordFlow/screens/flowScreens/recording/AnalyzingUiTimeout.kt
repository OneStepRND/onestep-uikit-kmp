package co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.recording

import co.onestep.kmp.uikit.features.summary.models.OSTSummaryOptions
import co.onestep.kmp.uikit.models.OSTAnalyserError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * The analyzing screen's give-up timer, extracted from [MotionRecorderViewModel] so the timeout
 * outcome can be tested without the ViewModel's platform dependencies.
 *
 * Started when the participant first sees the analyzing UI. If no outcome arrives within
 * [timeoutMillis], the attempt ends on the timeout:
 *
 * - With a summary configured ([OSTSummaryOptions.Full], [OSTSummaryOptions.WEB],
 *   [OSTSummaryOptions.MINIMAL]) it is the Timeout error screen, reached through `onError`.
 * - With [OSTSummaryOptions.None] the host asked for no in-flow result screens, so the flow ends
 *   instead: [exit] fires and the NavGraph dismisses with the `ui_timeout` exit reason, as uikit's
 *   RecordFlowFragment finishes `RESULT_CANCELED` + `EXIT_REASON_UI_TIMEOUT` on the same signal.
 *
 * Both branches claim the attempt's one-shot outcome first: the timeout is this attempt's terminal
 * outcome like any other, so it must not cover a result already delivered and must not be covered
 * by one that arrives after it.
 */
internal class AnalyzingUiTimeout(
    private val scope: CoroutineScope,
    private val timeoutMillis: Long = UI_TIMEOUT_MILLIS,
) {
    private var job: Job? = null

    private val _exit = Channel<Unit>(Channel.CONFLATED)

    /** Fires once when a [OSTSummaryOptions.None] attempt times out; the host must end the flow. */
    val exit: Flow<Unit> = _exit.receiveAsFlow()

    /**
     * Arms the timer unless it was already armed.
     *
     * @param summaryOption Read when the timer expires, not when it is armed.
     * @param onExpired Runs on expiry before the outcome is claimed — cancels in-flight analysis
     *   and resets the recorder, whichever outcome wins.
     * @param claimOutcome The attempt's one-shot latch; false means an outcome already resolved it.
     * @param onError Routes the Timeout error for the summary-showing options. Only called after a
     *   successful [claimOutcome].
     */
    fun start(
        summaryOption: () -> OSTSummaryOptions,
        onExpired: () -> Unit,
        claimOutcome: () -> Boolean,
        onError: (OSTAnalyserError) -> Unit,
    ) {
        if (job != null) return // Already running
        job = scope.launch {
            delay(timeoutMillis)
            onExpired()
            if (!claimOutcome()) return@launch
            when (summaryOption()) {
                OSTSummaryOptions.None -> _exit.trySend(Unit)

                OSTSummaryOptions.Full,
                OSTSummaryOptions.WEB,
                OSTSummaryOptions.MINIMAL -> onError(OSTAnalyserError.Timeout(null, "UI timeout"))
            }
        }
    }

    /**
     * Stops the timer because an outcome arrived. As before the extraction, a cancelled timer is
     * not re-armed by [start] until [reset].
     */
    fun cancel() {
        job?.cancel()
    }

    /** Stops the timer and lets the next [start] arm it again. */
    fun reset() {
        job?.cancel()
        job = null
    }

    companion object {
        const val UI_TIMEOUT_MILLIS = 60_000L
    }
}
