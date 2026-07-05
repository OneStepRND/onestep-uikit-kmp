package co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.recording

import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTBalanceCondition
import co.onestep.kmp.uikit.features.recordFlow.configurations.randomUUID
import co.onestep.kmp.uikit.models.OSTMotionMeasurement

/**
 * Owns the Static Balance session state (OS-15960). Extracted from [MotionRecorderViewModel]
 * so the session's identity and its completed-condition bookkeeping live in one place.
 *
 * One session per flow launch: the ViewModel is scoped to the recording flow, so a fresh
 * launch gets a fresh session UUID. Every condition recorded in this launch (including via
 * "Record another test") shares it; it is the Engine's grouping key for the session's
 * perceptions.
 *
 * The two recorder-entangled operations — attaching the post-recording clinician note and
 * resetting for the next condition — remain on the ViewModel because they reach into the
 * recorder bridge and the recorder-driven UI state; they read/clear this manager's condition
 * through [currentBalanceCondition] and [clearCurrentCondition].
 */
internal class BalanceSessionManager {

    /** Groups all Static Balance conditions recorded in this flow launch. */
    val sessionUuid: String = randomUUID()

    /** Measurement ids of the session's completed conditions, in recording order. */
    private val sessionMeasurementIds = mutableListOf<String>()

    /**
     * The most recently completed condition's measurement. Used to resume to the web
     * summary when a later condition fails (e.g. "recording too short" → Finish) — the
     * host opens its web summary, which shows all of the day's conditions.
     */
    var lastSavedBalanceMeasurement: OSTMotionMeasurement? = null
        private set

    /** The condition configured for the upcoming/current recording, if any. */
    var currentBalanceCondition: OSTBalanceCondition? = null
        private set

    /**
     * Sets the Static Balance condition for the upcoming recording. The condition's
     * per-category selections are attached to the perception as the nested
     * `onestep_balance_conditions` custom metadata at recorder start (see
     * `MotionRecorderViewModel.startRecording`). No note is collected before recording; the
     * single per-condition note is entered afterwards on the "Recording saved" screen and
     * merged into the same nested object via `MotionRecorderViewModel.updateBalanceConditionNote`.
     */
    fun setBalanceCondition(condition: OSTBalanceCondition) {
        currentBalanceCondition = condition
    }

    /** Records a completed condition's measurement in the session. */
    fun onBalanceConditionSaved(measurement: OSTMotionMeasurement) {
        sessionMeasurementIds.add(measurement.id)
        lastSavedBalanceMeasurement = measurement
    }

    /** Number of conditions completed in this session so far. */
    fun balanceConditionCount(): Int = sessionMeasurementIds.size

    /**
     * Clears the per-condition selection ("Record another test") while keeping the session
     * alive: same [sessionUuid], accumulated measurement ids untouched.
     */
    fun clearCurrentCondition() {
        currentBalanceCondition = null
    }
}
