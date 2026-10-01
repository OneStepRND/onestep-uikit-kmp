package co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.staticBalance

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import co.onestep.kmp.uikit.bridge.RecorderBridge
import co.onestep.kmp.uikit.bridge.SelfReportResult
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTBalance
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTBalanceCondition
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTTagField
import co.onestep.kmp.uikit.features.recordFlow.configurations.balanceSelfReportScore
import co.onestep.kmp.uikit.features.recordFlow.configurations.resultStatesFor
import co.onestep.kmp.uikit.features.recordFlow.configurations.resultStatesMetadata
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.recording.MotionRecorderViewModel
import co.onestep.kmp.uikit.models.OSTTagValue
import co.onestep.kmp.uikit.models.codes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * The legacy (no tag catalog) "Recording saved" outcome chips: the host's [OSTBalance.resultStates]
 * that fit [condition] ([resultStatesFor]), shaped as the checkbox field the screen renders, or null
 * when none fit. The field is keyed by [OSTBalance.resultStatesKey], so the screen's answer map
 * carries the codes under it; they are saved as custom metadata, not `tag_map` (see
 * [saveBalanceConditionResult]).
 */
internal fun OSTBalance.legacyOutcomesField(condition: OSTBalanceCondition?): OSTTagField? =
    resultStatesFor(condition)
        .takeIf { it.isNotEmpty() }
        ?.let { offered ->
            OSTTagField(
                name = resultStatesKey,
                type = OSTTagField.TYPE_CHECKBOX,
                stage = OSTTagField.STAGE_POST_RECORD,
                options = offered.map { OSTTagField.Option(label = it.displayName, value = it.code) },
            )
        }

/**
 * The balance score to self-report for the outcomes chosen on "Recording saved" ([outcomes], the
 * screen's answer map on either path): [balanceSelfReportScore] of every chosen code.
 */
internal fun outcomesSelfReportScore(outcomes: Map<String, OSTTagValue>): Int? =
    balanceSelfReportScore(outcomes.codes())

/**
 * Saves what the clinician entered on "Recording saved", then self-reports the balance score. Returns
 * false only when the score self-report failed, so the caller can offer to try again.
 *
 * - **Catalog** ([legacyBalance] null): the note and the chosen `$balance_result_states` go up
 *   together in one awaited update, the outcomes in `tag_map` (unchanged since OS-17546).
 * - **Legacy** ([legacyBalance] set): the note goes into `onestep_balance_conditions`, and the
 *   chosen outcomes, when any, under [OSTBalance.resultStatesKey] as a list of codes, both in one
 *   awaited update (OS-17571, as the Android SDK saves them).
 *
 * The score goes after the update: the backend rescores on a self-report, and its pass/fail verdict
 * reads the result states that update stores. Resending both on a retry is harmless — the update
 * merges into the stored metadata and the self-report overwrites.
 */
internal suspend fun saveBalanceConditionResult(
    viewModel: MotionRecorderViewModel,
    recorderBridge: RecorderBridge,
    legacyBalance: OSTBalance?,
    note: String?,
    outcomes: Map<String, OSTTagValue>,
): Boolean {
    if (legacyBalance == null) {
        viewModel.updateBalanceConditionAnswers(note, outcomes)
    } else {
        val offered = legacyBalance.resultStatesFor(viewModel.currentBalanceCondition)
        viewModel.updateBalanceConditionNote(
            newNote = note,
            resultStatesMetadata = legacyBalance.resultStatesMetadata(offered, outcomes.codes()),
        )
    }
    return recorderBridge.selfReportBalanceScore(
        measurementId = viewModel.motionMeasurement.value?.id,
        score = outcomesSelfReportScore(outcomes),
    )
}

/**
 * Self-reports [score] as the Static Balance score of [measurementId]. True when it was saved or
 * there is nothing to report: no score, or a platform that cannot report one.
 */
internal suspend fun RecorderBridge.selfReportBalanceScore(measurementId: String?, score: Int?): Boolean {
    if (score == null) return true
    // shortcut: iOS cannot self-report a balance score until the native SDK does (OS-17547); it
    // skips the report, and with it the "score wasn't saved" dialog every save would otherwise show.
    if (!supportsBalanceScoreSelfReport) return true
    if (measurementId == null) return false
    return try {
        selfReportMotionMeasurement(uuid = measurementId, balanceScore = score) == SelfReportResult.Success
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (@Suppress("TooGenericExceptionCaught") failure: Throwable) {
        println(
            "BalanceConditionResult: Failed to self-report the static balance score for " +
                "$measurementId: ${failure::class.simpleName}",
        )
        false
    }
}

/**
 * The "score wasn't saved" state machine of the "Recording saved" screen (OS-17571), the
 * counterpart of the Android SDK's `PendingBalanceScore`.
 *
 * [saveThen] runs a save; when its score self-report fails, it publishes [pending] (which shows
 * [BalanceScoreNotSavedDialog]) and **suspends until the clinician settles it**, so the tapped button
 * stays in flight — taps ignored — the whole time. Returning early would re-arm the button for the
 * moment before the dialog takes input, and a fast second tap would save twice. [tryAgain] resends
 * the same save and settles on success; [continueWithoutScore] settles at once.
 */
internal class BalanceScoreSaveGate {

    /** A failed score self-report: what to resend, and what releases the waiting button. */
    internal class Pending(
        val save: suspend () -> Boolean,
        val settled: CompletableDeferred<Unit> = CompletableDeferred(),
    )

    /** The failed save awaiting the clinician's choice, or null when the dialog is not shown. */
    var pending: Pending? by mutableStateOf(null)
        private set

    /** True while a [tryAgain] is in flight: both dialog buttons ignore taps. */
    var retrying: Boolean by mutableStateOf(false)
        private set

    /**
     * Runs [save] (true = the score was saved, or there was none to save) and returns once it
     * succeeded or the clinician settled the dialog. A cancelled caller takes the dialog down.
     */
    suspend fun saveThen(save: suspend () -> Boolean) {
        if (save()) return
        val failed = Pending(save)
        pending = failed
        try {
            failed.settled.await()
        } finally {
            if (pending === failed) pending = null
        }
    }

    /**
     * Resends the pending save in [scope], unless one is already in flight. The guard is set before
     * anything launches, so two taps in the same frame start one retry.
     */
    fun tryAgain(scope: CoroutineScope): Job? {
        val current = pending ?: return null
        if (retrying) return null
        retrying = true
        return scope.launch {
            try {
                if (current.save()) settle(current)
            } finally {
                retrying = false
            }
        }
    }

    /** Proceeds without the score. Ignored while a retry is in flight. */
    fun continueWithoutScore() {
        if (retrying) return
        pending?.let { settle(it) }
    }

    private fun settle(settling: Pending) {
        if (pending === settling) pending = null
        settling.settled.complete(Unit)
    }
}
