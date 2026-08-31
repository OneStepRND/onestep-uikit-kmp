package co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.recording

import co.onestep.kmp.uikit.bridge.RecorderBridge
import co.onestep.kmp.uikit.bridge.SelfReportResult
import co.onestep.kmp.uikit.models.OSTActivityType
import co.onestep.kmp.uikit.models.OSTAnalyserState
import co.onestep.kmp.uikit.models.OSTMotionMeasurement
import co.onestep.kmp.uikit.models.OSTRecorderState
import co.onestep.kmp.uikit.models.OSTRecordingWindow
import co.onestep.kmp.uikit.models.OSTTimeRangedDataRequest
import co.onestep.kmp.uikit.models.OSTUserInputMetaData
import co.onestep.kmp.uikit.models.OSTWalkCourseLength
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Records the recorder interactions ([calls], [startCalls], [markers]) and mimics the real
 * bridge's state transitions: `start()` → RECORDING, `stop()` → DONE, `reset()` → INITIALIZED.
 * Set [startGate] to make `start()` suspend until released (in-flight-start ordering tests).
 */
internal class FakeRecorderBridge : RecorderBridge {
    override val recorderState = MutableStateFlow(OSTRecorderState.INITIALIZED)
    override val stepsCount = MutableStateFlow(0)
    override val analyserState: StateFlow<OSTAnalyserState> =
        MutableStateFlow(OSTAnalyserState.Idle)

    /**
     * Mimics an SDK that publishes a recording window: `start()` opens one on the test clock
     * ([monotonicNow]) for the duration it was given, `reset()` clears it. Leave [monotonicNow]
     * null to mimic a platform that publishes no window (iOS today).
     */
    override val currentRecordingWindow = MutableStateFlow<OSTRecordingWindow?>(null)

    var monotonicNow: (() -> Long)? = null

    data class StartCall(val activityType: OSTActivityType, val durationMs: Long?)

    /** Coarse call order across start/stop/reset/marker — for ordering assertions. */
    val calls = mutableListOf<String>()
    val startCalls = mutableListOf<StartCall>()
    val markers = mutableListOf<String>()

    /** Durations passed to [rescheduleAutoStop] while RECORDING, in order. */
    val rescheduleCalls = mutableListOf<Long>()
    var stopCount = 0
        private set
    var resetCount = 0
        private set

    var startGate: CompletableDeferred<Unit>? = null

    override suspend fun prepareForRecording(activityType: OSTActivityType): Boolean = true

    override suspend fun start(
        activityType: OSTActivityType,
        duration: Long?,
        sensorEnhancedMode: Boolean,
        userInputMetadata: OSTUserInputMetaData?,
        customMetadata: Map<String, Any>?,
    ) {
        startGate?.await()
        calls += "start"
        startCalls += StartCall(activityType, duration)
        monotonicNow?.let { clock ->
            val startedAt = clock()
            currentRecordingWindow.value = OSTRecordingWindow(
                startedAtMonotonicMillis = startedAt,
                willEndAtMonotonicMillis = startedAt + (duration ?: currentRecordingLimit()),
                startedAtEpochMillis = startedAt,
            )
        }
        recorderState.value = OSTRecorderState.RECORDING
    }

    /**
     * Mimics core: ignored unless RECORDING, otherwise republishes the window with the new
     * deadline, leaving `startedAt` where capture really began.
     */
    override fun rescheduleAutoStop(durationMillis: Long) {
        if (recorderState.value != OSTRecorderState.RECORDING) return
        calls += "reschedule"
        rescheduleCalls += durationMillis
        val clock = monotonicNow ?: return
        currentRecordingWindow.value = currentRecordingWindow.value?.copy(
            willEndAtMonotonicMillis = clock() + durationMillis,
        )
    }

    override suspend fun stop() {
        calls += "stop"
        stopCount++
        recorderState.value = OSTRecorderState.DONE
    }

    override fun reset() {
        calls += "reset"
        resetCount++
        currentRecordingWindow.value = null
        recorderState.value = OSTRecorderState.INITIALIZED
    }

    override fun addMarker(marker: String) {
        calls += "marker:$marker"
        markers += marker
    }

    override suspend fun analyze(timeout: Long): OSTMotionMeasurement? = null

    override suspend fun analyze(uuid: String, timeout: Long, interval: Long): OSTMotionMeasurement? = null

    /** Generic Recording upload. Counted so a test can assert the analysis was skipped. */
    var uploadWithoutAnalysisCount = 0
        private set

    override suspend fun uploadWithoutAnalysis(): OSTMotionMeasurement? {
        calls += "uploadWithoutAnalysis"
        uploadWithoutAnalysisCount++
        return null
    }

    override suspend fun updateSixMinuteWalkCourseLength(uuid: String, requestBody: OSTWalkCourseLength) = Unit

    override fun currentRecordingLimit(): Long = 6 * 60 * 1000L

    override suspend fun readSingleMotionMeasurement(uuid: String): OSTMotionMeasurement? = null

    override suspend fun readMotionMeasurements(request: OSTTimeRangedDataRequest): List<OSTMotionMeasurement> =
        emptyList()

    override suspend fun deleteMotionMeasurement(uuid: String) = Unit

    override suspend fun updateMotionMeasurement(uuid: String, metadata: OSTUserInputMetaData) = Unit

    override suspend fun updateBalanceConditionMetadata(uuid: String, conditions: Map<String, String>) = Unit

    override suspend fun selfReportMotionMeasurement(uuid: String, stsRepetitions: Int): SelfReportResult =
        SelfReportResult.Success
}
