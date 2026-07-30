package co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.recording

import co.onestep.kmp.uikit.bridge.RecorderBridge
import co.onestep.kmp.uikit.bridge.SelfReportResult
import co.onestep.kmp.uikit.models.OSTActivityType
import co.onestep.kmp.uikit.models.OSTAnalyserState
import co.onestep.kmp.uikit.models.OSTMotionMeasurement
import co.onestep.kmp.uikit.models.OSTRecorderState
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

    data class StartCall(val activityType: OSTActivityType, val durationMs: Long?)

    /** Coarse call order across start/stop/reset/marker — for ordering assertions. */
    val calls = mutableListOf<String>()
    val startCalls = mutableListOf<StartCall>()
    val markers = mutableListOf<String>()
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
        recorderState.value = OSTRecorderState.RECORDING
    }

    override suspend fun stop() {
        calls += "stop"
        stopCount++
        recorderState.value = OSTRecorderState.DONE
    }

    override fun reset() {
        calls += "reset"
        resetCount++
        recorderState.value = OSTRecorderState.INITIALIZED
    }

    override fun addMarker(marker: String) {
        calls += "marker:$marker"
        markers += marker
    }

    override suspend fun analyze(timeout: Long): OSTMotionMeasurement? = null

    override suspend fun analyze(uuid: String, timeout: Long, interval: Long): OSTMotionMeasurement? = null

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
