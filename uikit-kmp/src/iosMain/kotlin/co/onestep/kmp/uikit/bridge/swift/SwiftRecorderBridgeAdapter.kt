package co.onestep.kmp.uikit.bridge.swift

import co.onestep.kmp.uikit.bridge.RecorderBridge
import co.onestep.kmp.uikit.bridge.SelfReportResult
import co.onestep.kmp.uikit.mapper.createKmpAnalyserError
import co.onestep.kmp.uikit.mapper.createKmpAnalyserState
import co.onestep.kmp.uikit.mapper.toIosString
import co.onestep.kmp.uikit.mapper.toKmpRecorderState
import co.onestep.kmp.uikit.models.OSTActivityType
import co.onestep.kmp.uikit.models.OSTAnalyserState
import co.onestep.kmp.uikit.models.OSTMotionMeasurement
import co.onestep.kmp.uikit.models.OSTOrder
import co.onestep.kmp.uikit.models.OSTRecorderState
import co.onestep.kmp.uikit.models.OSTRecordingWindow
import co.onestep.kmp.uikit.models.OSTTimeRangedDataRequest
import co.onestep.kmp.uikit.models.OSTUserInputMetaData
import co.onestep.kmp.uikit.models.OSTWalkCourseLength
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Swift-facing delegate for [SwiftRecorderBridgeAdapter]. A Swift class implements this by
 * delegating to the native OneStep recorder. Suspend functions on [RecorderBridge] are exposed here
 * as ObjC-friendly completion-handler methods that hand back already-mapped KMP models (built with
 * the iosMain mapper factories). [StateFlow]s never cross the boundary — the adapter owns them and
 * Swift pushes updates via the adapter's `on...Changed` functions.
 *
 * Metadata is passed as exported KMP types ([OSTUserInputMetaData]) rather than JSON so Swift can
 * build them directly; free-form custom metadata uses a `Map<String, String>` (ObjC-representable).
 */
interface IosRecorderDelegate {
    fun prepareForRecording(activityType: String, completion: (Boolean) -> Unit)

    fun start(
        activityType: String,
        durationMs: Long,
        sensorEnhancedMode: Boolean,
        userInputMetadata: OSTUserInputMetaData?,
        customMetadata: Map<String, String>?,
        completion: () -> Unit,
    )

    fun stop(completion: () -> Unit)

    fun reset()

    fun analyze(uuid: String?, timeoutMs: Long, completion: (OSTMotionMeasurement?) -> Unit)

    fun updateSixMinuteWalkCourseLength(
        uuid: String,
        walkCourseLength: OSTWalkCourseLength,
        completion: () -> Unit,
    )

    fun currentRecordingLimit(): Long

    fun addMarker(marker: String)

    fun readSingleMotionMeasurement(uuid: String, completion: (OSTMotionMeasurement?) -> Unit)

    fun readMotionMeasurements(
        limit: Int?,
        order: String?,
        activityType: String?,
        startTimeMs: Long?,
        endTimeMs: Long?,
        completion: (List<OSTMotionMeasurement>) -> Unit,
    )

    fun deleteMotionMeasurement(uuid: String, completion: () -> Unit)

    fun updateMotionMeasurement(
        uuid: String,
        metadata: OSTUserInputMetaData,
        completion: () -> Unit,
    )

    fun updateBalanceConditionMetadata(
        uuid: String,
        conditions: Map<String, String>,
        completion: () -> Unit,
    )

    /**
     * Submit a clinician-entered STS repetition count. [completion] receives a
     * [SwiftRecorderBridgeAdapter.SELF_REPORT_SUCCESS] / [SwiftRecorderBridgeAdapter.SELF_REPORT_NETWORK_FAILURE] /
     * [SwiftRecorderBridgeAdapter.SELF_REPORT_SERVER_FAILURE] code.
     */
    fun selfReportMotionMeasurement(uuid: String, stsRepetitions: Int, completion: (Int) -> Unit)
}

/**
 * [RecorderBridge] implementation that owns the coroutine/flow machinery in Kotlin and delegates
 * ObjC-friendly work to an [IosRecorderDelegate] Swift implementation.
 *
 * Swift pushes recorder/analyser updates via [onRecorderStateChanged], [onStepsChanged], and
 * [onAnalyserStateChanged]; the adapter exposes those as the [recorderState] (initial
 * [OSTRecorderState.INITIALIZED]), [stepsCount] (initial `0`), and [analyserState] (initial
 * [OSTAnalyserState.Idle]) flows.
 */
class SwiftRecorderBridgeAdapter(private val delegate: IosRecorderDelegate) : RecorderBridge {

    private val _recorderState = MutableStateFlow(OSTRecorderState.INITIALIZED)
    override val recorderState: StateFlow<OSTRecorderState> = _recorderState.asStateFlow()

    private val _stepsCount = MutableStateFlow(0)
    override val stepsCount: StateFlow<Int> = _stepsCount.asStateFlow()

    private val _analyserState = MutableStateFlow<OSTAnalyserState>(OSTAnalyserState.Idle)
    override val analyserState: StateFlow<OSTAnalyserState> = _analyserState.asStateFlow()

    private val _currentRecordingWindow = MutableStateFlow<OSTRecordingWindow?>(null)

    /**
     * TODO(OS-16749): stays `null` until the iOS SDK publishes a recording window. The Kotlin side
     *  is ready — Swift only has to call [onRecordingWindowChanged] / [onRecordingWindowCleared]
     *  once `OSTMotionLab` exposes `startedAt` / `willEndAt`. Until then the recording clock falls
     *  back to a UI-side wall clock (see `RecordingSessionController`), i.e. iOS keeps today's
     *  behaviour and does not yet get the drift/backgrounding fixes this window enables.
     */
    override val currentRecordingWindow: StateFlow<OSTRecordingWindow?> =
        _currentRecordingWindow.asStateFlow()

    // --- Swift push functions ---

    /**
     * Push the current recording window from Swift, before reporting the RECORDING state.
     *
     * [startedAtMonotonicMs] and [willEndAtMonotonicMs] must be
     * `ProcessInfo.processInfo.systemUptime * 1000` readings — the same clock
     * [co.onestep.kmp.uikit.utils.monotonicNowMillis] uses on iOS. Wall-clock timestamps here
     * would make every duration wrong the moment the device clock moves.
     *
     * @param startedAtEpochMs Wall-clock start, for display and logging only.
     */
    fun onRecordingWindowChanged(
        startedAtMonotonicMs: Long,
        willEndAtMonotonicMs: Long,
        startedAtEpochMs: Long,
    ) {
        _currentRecordingWindow.value = OSTRecordingWindow(
            startedAtMonotonicMillis = startedAtMonotonicMs,
            willEndAtMonotonicMillis = willEndAtMonotonicMs,
            startedAtEpochMillis = startedAtEpochMs,
        )
    }

    /** Clear the recording window from Swift when the recorder is reset. */
    fun onRecordingWindowCleared() {
        _currentRecordingWindow.value = null
    }

    /** Push a new recorder state from Swift. Uses the existing [toKmpRecorderState] mapper. */
    fun onRecorderStateChanged(stateName: String) {
        _recorderState.value = stateName.toKmpRecorderState()
    }

    /** Push the current step count from Swift. */
    fun onStepsChanged(steps: Int) {
        _stepsCount.value = steps
    }

    /** Push a new analyser state from Swift. Uses the existing [createKmpAnalyserState] mapper. */
    fun onAnalyserStateChanged(
        stateName: String,
        errorName: String? = null,
        errorMessage: String? = null,
    ) {
        _analyserState.value = if (stateName.uppercase() == "FAILED") {
            OSTAnalyserState.Failed(
                error = createKmpAnalyserError(
                    type = errorName ?: "GENERAL",
                    message = errorMessage,
                ),
            )
        } else {
            createKmpAnalyserState(stateName = stateName, errorMessage = errorMessage)
        }
    }

    // --- RecorderBridge ---

    override suspend fun prepareForRecording(activityType: OSTActivityType): Boolean =
        suspendCancellableCoroutine { continuation ->
            delegate.prepareForRecording(activityType.toIosString()) { success ->
                continuation.resume(success)
            }
        }

    override suspend fun start(
        activityType: OSTActivityType,
        duration: Long?,
        sensorEnhancedMode: Boolean,
        userInputMetadata: OSTUserInputMetaData?,
        customMetadata: Map<String, Any>?,
    ) {
        // customMetadata carries String values in practice; expose it to Swift as Map<String,String>.
        val stringMetadata = customMetadata
            ?.mapNotNull { (key, value) -> (value as? String)?.let { key to it } }
            ?.toMap()
        suspendCancellableCoroutine { continuation ->
            delegate.start(
                activityType = activityType.toIosString(),
                durationMs = duration ?: 0L,
                sensorEnhancedMode = sensorEnhancedMode,
                userInputMetadata = userInputMetadata,
                customMetadata = stringMetadata,
            ) { continuation.resume(Unit) }
        }
    }

    override suspend fun stop() {
        suspendCancellableCoroutine { continuation ->
            delegate.stop { continuation.resume(Unit) }
        }
    }

    override fun reset() {
        delegate.reset()
    }

    override suspend fun analyze(timeout: Long): OSTMotionMeasurement? =
        suspendCancellableCoroutine { continuation ->
            delegate.analyze(uuid = null, timeoutMs = timeout) { measurement ->
                continuation.resume(measurement)
            }
        }

    override suspend fun analyze(uuid: String, timeout: Long, interval: Long): OSTMotionMeasurement? =
        suspendCancellableCoroutine { continuation ->
            delegate.analyze(uuid = uuid, timeoutMs = timeout) { measurement ->
                continuation.resume(measurement)
            }
        }

    override suspend fun updateSixMinuteWalkCourseLength(uuid: String, requestBody: OSTWalkCourseLength) {
        suspendCancellableCoroutine { continuation ->
            delegate.updateSixMinuteWalkCourseLength(uuid, requestBody) { continuation.resume(Unit) }
        }
    }

    override fun currentRecordingLimit(): Long = delegate.currentRecordingLimit()

    override fun addMarker(marker: String) {
        delegate.addMarker(marker)
    }

    override suspend fun readSingleMotionMeasurement(uuid: String): OSTMotionMeasurement? =
        suspendCancellableCoroutine { continuation ->
            delegate.readSingleMotionMeasurement(uuid) { measurement ->
                continuation.resume(measurement)
            }
        }

    override suspend fun readMotionMeasurements(
        request: OSTTimeRangedDataRequest,
    ): List<OSTMotionMeasurement> =
        suspendCancellableCoroutine { continuation ->
            val orderName = when (request.order) {
                OSTOrder.ASCENDING -> "ASCENDING"
                OSTOrder.DESCENDING -> "DESCENDING"
                null -> null
            }
            delegate.readMotionMeasurements(
                limit = request.limit,
                order = orderName,
                activityType = request.activityType,
                startTimeMs = request.timeRangeFilter?.getStartTime(),
                endTimeMs = request.timeRangeFilter?.getEndTime(),
            ) { measurements -> continuation.resume(measurements) }
        }

    override suspend fun deleteMotionMeasurement(uuid: String) {
        suspendCancellableCoroutine { continuation ->
            delegate.deleteMotionMeasurement(uuid) { continuation.resume(Unit) }
        }
    }

    override suspend fun updateMotionMeasurement(uuid: String, metadata: OSTUserInputMetaData) {
        suspendCancellableCoroutine { continuation ->
            delegate.updateMotionMeasurement(uuid, metadata) { continuation.resume(Unit) }
        }
    }

    override suspend fun updateBalanceConditionMetadata(uuid: String, conditions: Map<String, String>) {
        suspendCancellableCoroutine { continuation ->
            delegate.updateBalanceConditionMetadata(uuid, conditions) { continuation.resume(Unit) }
        }
    }

    override suspend fun selfReportMotionMeasurement(uuid: String, stsRepetitions: Int): SelfReportResult =
        suspendCancellableCoroutine { continuation ->
            delegate.selfReportMotionMeasurement(uuid, stsRepetitions) { code ->
                val result = when (code) {
                    SELF_REPORT_SUCCESS -> SelfReportResult.Success
                    SELF_REPORT_NETWORK_FAILURE -> SelfReportResult.NetworkFailure
                    else -> SelfReportResult.ServerFailure
                }
                continuation.resume(result)
            }
        }

    companion object {
        /** [selfReportMotionMeasurement] outcome codes exchanged with Swift. */
        const val SELF_REPORT_SUCCESS = 0
        const val SELF_REPORT_NETWORK_FAILURE = 1
        const val SELF_REPORT_SERVER_FAILURE = 2
    }
}
