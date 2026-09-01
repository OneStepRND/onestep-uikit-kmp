package co.onestep.kmp.uikit.bridge

import co.onestep.kmp.uikit.models.OSTActivityType
import co.onestep.kmp.uikit.models.OSTAnalyserState
import co.onestep.kmp.uikit.models.OSTMotionMeasurement
import co.onestep.kmp.uikit.models.OSTRecorderState
import co.onestep.kmp.uikit.models.OSTRecordingWindow
import co.onestep.kmp.uikit.models.OSTTimeRangedDataRequest
import co.onestep.kmp.uikit.models.OSTUserInputMetaData
import co.onestep.kmp.uikit.models.OSTWalkCourseLength
import kotlinx.coroutines.flow.StateFlow

/**
 * Bridge interface abstracting OSTRecorder.
 * Android implementation delegates to the real OSTRecorder from core.
 * iOS implementation will delegate to the iOS recorder.
 */
interface RecorderBridge {
    val recorderState: StateFlow<OSTRecorderState>
    val stepsCount: StateFlow<Int>
    val analyserState: StateFlow<OSTAnalyserState>

    /**
     * Timing of the current recording — when capture began and when the recorder will auto-stop —
     * or `null` when no recording is in flight or the platform SDK does not publish it.
     *
     * **This is the source of truth for recording time.** The recording clock belongs to the SDK:
     * capture runs to its deadline even when the recording screen is gone, so a UI that counts its
     * own seconds inevitably disagrees with the recorder. See [OSTRecordingWindow].
     *
     * Populated before [recorderState] reaches [OSTRecorderState.RECORDING] and cleared on
     * [reset], so an observer that sees RECORDING reads a non-null window.
     */
    val currentRecordingWindow: StateFlow<OSTRecordingWindow?>

    suspend fun prepareForRecording(activityType: OSTActivityType): Boolean

    suspend fun start(
        activityType: OSTActivityType,
        duration: Long? = null,
        sensorEnhancedMode: Boolean = false,
        userInputMetadata: OSTUserInputMetaData? = OSTUserInputMetaData(),
        customMetadata: Map<String, Any>? = emptyMap(),
    )

    /**
     * Moves the auto-stop deadline of the recording already in flight to [durationMillis] from
     * now, without interrupting capture, and republishes [currentRecordingWindow] with the new
     * deadline (both `startedAt` values are left alone — capture really did begin when they say).
     *
     * Exists because a recording that starts early ([start] on the Get Ready screen) can only be
     * given a *predicted* deadline: the countdown has a "Start now" button, so how long the
     * preamble lasts is not known until the user reaches the end of it. Re-anchoring at the real
     * "go" is what makes the measurement come out at its configured length on both paths.
     *
     * Re-anchoring only ever **shortens** an over-generous deadline — it cannot revive a recording
     * that already auto-stopped, so the provisional deadline must still outlast the preamble.
     *
     * A no-op when the recorder is not RECORDING, and on any platform whose SDK does not expose
     * the API yet — the UI-side clock in `RecordingSessionController` is the fallback there.
     *
     * @param durationMillis Milliseconds from now until the recorder auto-stops. Must be positive.
     */
    fun rescheduleAutoStop(durationMillis: Long)

    suspend fun stop()

    fun reset()

    suspend fun analyze(timeout: Long = 60000): OSTMotionMeasurement?

    suspend fun analyze(
        uuid: String,
        timeout: Long = 60000,
        interval: Long = 500,
    ): OSTMotionMeasurement?

    /**
     * Generic Recording (OS-16861): uploads the completed recording and banks it **without**
     * requesting an analysis, returning the stored measurement (or `null` if the upload failed).
     *
     * A stored recording is the success case here — there is no analysis pipeline to poll, so the
     * platform analyser never advances to [OSTAnalyserState.Analyzed] and the flow's completion has
     * to be driven from this call's return rather than from the analyser-state collector. Calling
     * [analyze] instead would wait on a result that never arrives.
     *
     * The returned measurement carries no score, insight or `summaryUrl`; the only post-recording
     * input is the free-text note, attached afterwards via [updateMotionMeasurement].
     */
    suspend fun uploadWithoutAnalysis(): OSTMotionMeasurement?

    suspend fun updateSixMinuteWalkCourseLength(
        uuid: String,
        requestBody: OSTWalkCourseLength,
    )

    fun currentRecordingLimit(): Long

    fun addMarker(marker: String)

    suspend fun readSingleMotionMeasurement(uuid: String): OSTMotionMeasurement?

    suspend fun readMotionMeasurements(
        request: OSTTimeRangedDataRequest,
    ): List<OSTMotionMeasurement>

    suspend fun deleteMotionMeasurement(uuid: String)

    suspend fun updateMotionMeasurement(uuid: String, metadata: OSTUserInputMetaData)

    /**
     * Static Balance (OS-15960): attaches the nested per-condition object
     * ([OSTBalanceCondition.KEY_BALANCE_CONDITIONS]) to a completed measurement's custom
     * metadata. Separate from [updateMotionMeasurement] because that path carries only
     * [OSTUserInputMetaData] fields, not arbitrary nested custom metadata.
     *
     * @param conditions The full nested object (selections + optional note), sent whole so
     *        it stays complete regardless of the server's per-key merge behavior.
     */
    suspend fun updateBalanceConditionMetadata(uuid: String, conditions: Map<String, String>)

    /**
     * STS manual self-report (OS-15960 sibling): submits a clinician-entered repetition count as
     * an override for a completed STS measurement. Mirrors the Android uikit call to
     * `OSTMotionLab.selfReportMotionMeasurement(uuid, stsRepetitions)`.
     *
     * The clinical value ([stsRepetitions]) is written to the measurement only — it is a count,
     * never PII/PHI free text. Returns a [SelfReportResult] so callers can distinguish a
     * retryable transport failure from a non-retryable server rejection, matching uikit's
     * `StsFailureType`.
     */
    suspend fun selfReportMotionMeasurement(uuid: String, stsRepetitions: Int): SelfReportResult

    companion object {
        const val MIN_STEPS_FOR_ANALYSIS = 20
        const val READY_FOR_ANALYSIS_KEY = "ready_for_analysis"
    }
}

/**
 * Outcome of [RecorderBridge.selfReportMotionMeasurement].
 *
 * Mirrors uikit's `StsManualReportViewModel.StsFailureType`: [NetworkFailure] is a retryable
 * transport-level failure (offer "Reload"); [ServerFailure] is a non-retryable server rejection
 * or uninitialized-SDK case (dismiss only).
 */
sealed interface SelfReportResult {
    data object Success : SelfReportResult
    data object NetworkFailure : SelfReportResult
    data object ServerFailure : SelfReportResult
}
