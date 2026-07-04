package co.onestep.kmp.uikit.bridge.android

import co.onestep.android.core.OSTError
import co.onestep.android.core.OSTResult
import co.onestep.android.core.OneStep
import co.onestep.android.core.getOr
import co.onestep.android.core.getOrThrow
import co.onestep.android.core.motionLab.OSTMotionLab as CoreMotionLab
import co.onestep.android.core.motionLab.OSTMotionMeasurement as CoreMeasurement
import co.onestep.android.core.motionLab.OSTOrder as CoreOrder
import co.onestep.android.core.motionLab.getMotionLab
import co.onestep.kmp.uikit.bridge.RecorderBridge
import co.onestep.kmp.uikit.bridge.SelfReportResult
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTBalanceCondition
import co.onestep.kmp.uikit.models.OSTActivityType
import co.onestep.kmp.uikit.models.OSTAnalyserState
import co.onestep.kmp.uikit.models.OSTMotionMeasurement
import co.onestep.kmp.uikit.models.OSTOrder as KmpOrder
import co.onestep.kmp.uikit.models.OSTRecorderState
import co.onestep.kmp.uikit.models.OSTTimeRangedDataRequest
import co.onestep.kmp.uikit.models.OSTUserInputMetaData
import co.onestep.kmp.uikit.models.OSTWalkCourseLength
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

class AndroidRecorderBridge(private val oneStep: OneStep) : RecorderBridge {

    private val motionLab get() = oneStep.getMotionLab().getOrThrow { IllegalStateException("MotionLab unavailable: ${it.message}") }

    override val recorderState: StateFlow<OSTRecorderState>
        get() = object : StateFlow<OSTRecorderState> {
            private val delegate = motionLab.recorderState
            override val replayCache: List<OSTRecorderState> get() = delegate.replayCache.map { it.toKmp() }
            override val value: OSTRecorderState get() = delegate.value.toKmp()
            override suspend fun collect(collector: kotlinx.coroutines.flow.FlowCollector<OSTRecorderState>): Nothing {
                delegate.collect { collector.emit(it.toKmp()) }
            }
        }

    override val stepsCount: StateFlow<Int>
        get() = motionLab.stepsCount

    override val analyserState: StateFlow<OSTAnalyserState>
        get() = object : StateFlow<OSTAnalyserState> {
            private val delegate = motionLab.analyserState
            override val replayCache: List<OSTAnalyserState> get() = delegate.replayCache.map { it.toKmp() }
            override val value: OSTAnalyserState get() = delegate.value.toKmp()
            override suspend fun collect(collector: kotlinx.coroutines.flow.FlowCollector<OSTAnalyserState>): Nothing {
                delegate.collect { collector.emit(it.toKmp()) }
            }
        }

    override suspend fun prepareForRecording(activityType: OSTActivityType): Boolean =
        motionLab.prepareForRecording(activityType.toCore()) is OSTResult.Success

    override suspend fun start(
        activityType: OSTActivityType,
        duration: Long?,
        sensorEnhancedMode: Boolean,
        userInputMetadata: OSTUserInputMetaData?,
        customMetadata: Map<String, Any>?,
    ) {
        motionLab.setSensorEnhancedMode(sensorEnhancedMode)
        val core = userInputMetadata?.toCore()
        // null duration = unrestricted walk; the recorder auto-stops when the
        // duration elapses, which is what moves the recording screen forward.
        motionLab.start(
            activityType = activityType.toCore(),
            durationMillis = duration ?: CoreMotionLab.RECORDER_LIMIT_MILLIS,
        ) {
            core?.let { from(it) }
            customMetadata?.takeIf { it.isNotEmpty() }?.let { customMetadata(it) }
        }
    }

    override suspend fun stop() {
        motionLab.stop()
    }

    override fun reset() {
        motionLab.reset()
    }

    override suspend fun analyze(timeout: Long): OSTMotionMeasurement? =
        motionLab.analyze(timeout).getOr(null as CoreMeasurement?)?.toKmp()

    override suspend fun analyze(uuid: String, timeout: Long, interval: Long): OSTMotionMeasurement? {
        motionLab.analyzeMotionMeasurement(uuid, timeout, interval)
        return motionLab.readSingleMotionMeasurement(uuid).getOr(null as CoreMeasurement?)?.toKmp()
    }

    override suspend fun updateSixMinuteWalkCourseLength(uuid: String, requestBody: OSTWalkCourseLength) {
        val core = requestBody.toCore()
        motionLab.updateMotionMeasurement(uuid) {
            walkCourseLength = core
        }
    }

    override fun currentRecordingLimit(): Long = motionLab.currentRecordingLimit().getOr(0L)

    override fun addMarker(marker: String) {
        motionLab.addMarker(marker)
    }

    override suspend fun readSingleMotionMeasurement(uuid: String): OSTMotionMeasurement? =
        motionLab.readSingleMotionMeasurement(uuid).getOr(null as CoreMeasurement?)?.toKmp()

    override suspend fun readMotionMeasurements(request: OSTTimeRangedDataRequest): List<OSTMotionMeasurement> =
        motionLab.readMotionMeasurements {
            limit = request.limit
            order = when (request.order) {
                KmpOrder.ASCENDING -> CoreOrder.ASCENDING
                KmpOrder.DESCENDING -> CoreOrder.DESCENDING
                else -> null
            }
            activityType = request.activityType
        }.getOr(emptyList()).map { it.toKmp() }

    override suspend fun deleteMotionMeasurement(uuid: String) {
        motionLab.deleteMotionMeasurement(uuid)
    }

    override suspend fun updateMotionMeasurement(uuid: String, metadata: OSTUserInputMetaData) {
        val core = metadata.toCore()
        motionLab.updateMotionMeasurement(uuid) {
            from(core)
        }
    }

    override suspend fun updateBalanceConditionMetadata(uuid: String, conditions: Map<String, String>) {
        motionLab.updateMotionMeasurement(uuid) {
            customMetadata(OSTBalanceCondition.KEY_BALANCE_CONDITIONS to conditions)
        }
    }

    override suspend fun selfReportMotionMeasurement(uuid: String, stsRepetitions: Int): SelfReportResult {
        val result = motionLab.selfReportMotionMeasurement(uuid = uuid, stsRepetitions = stsRepetitions)
        return when (result) {
            is OSTResult.Success -> SelfReportResult.Success
            // Retryable only for actual transport-level connectivity issues (NetworkError);
            // every other error (ServerError for HTTP 4xx/5xx, etc.) is non-retryable.
            is OSTResult.Error ->
                if (result.cause.type == OSTError.Type.NetworkError) {
                    SelfReportResult.NetworkFailure
                } else {
                    SelfReportResult.ServerFailure
                }
        }
    }
}
