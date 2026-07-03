package co.onestep.kmp.uikit.bridge.android

import co.onestep.android.core.OneStep
import co.onestep.android.core.getOrThrow
import co.onestep.android.core.insights.OSTMotionDataService
import co.onestep.android.core.insights.getInsights
import co.onestep.kmp.uikit.bridge.MotionDataBridge
import co.onestep.kmp.uikit.models.OSTDiscreteColor
import co.onestep.kmp.uikit.models.OSTMotionMeasurement
import co.onestep.kmp.uikit.models.OSTNorm
import co.onestep.kmp.uikit.models.OSTParamName
import co.onestep.kmp.uikit.models.OSTParameterMetadata
import kotlinx.coroutines.runBlocking

class AndroidMotionDataBridge(private val oneStep: OneStep) : MotionDataBridge {

    private val service: OSTMotionDataService by lazy {
        runBlocking {
            oneStep.getInsights()
                .getOrThrow { IllegalStateException("Insights unavailable: ${it.message}") }
                .getMotionDataService()
                .getOrThrow { IllegalStateException("MotionDataService unavailable: ${it.message}") }
        }
    }

    override fun mainParam(motionMeasurement: OSTMotionMeasurement): Pair<OSTParamName, Float>? {
        val entry = service.mainParam(motionMeasurement.toCore()) ?: return null
        val kmpParam = entry.key.toKmp() ?: return null
        return kmpParam to entry.value
    }

    override fun getAllParametersMetadata(): Map<OSTParamName, OSTParameterMetadata> =
        service.getAllParametersMetadata().mapNotNull { (key, value) ->
            key.toKmp()?.let { it to value.toKmp() }
        }.toMap()

    override fun getNormByName(name: OSTParamName?): OSTNorm? {
        val coreName = name?.toCore() ?: return null
        return service.getNormByName(coreName)?.toKmp()
    }

    override fun getParameterMetadata(paramName: OSTParamName): OSTParameterMetadata {
        val coreName = paramName.toCore()
            ?: error("Unknown param name: $paramName")
        return service.getParameterMetadata(coreName).toKmp()
    }

    override fun isWithinNorms(param: OSTParamName, value: Float): Boolean? {
        val coreName = param.toCore() ?: return null
        return service.isWithinNorms(coreName, value)
    }

    override fun discreteScore(motionMeasurement: OSTMotionMeasurement, value: Float): OSTDiscreteColor? =
        service.discreteScore(motionMeasurement.toCore(), value)?.toKmp()

    override fun discreteScore(param: OSTParamName, value: Float): OSTDiscreteColor? {
        val coreName = param.toCore() ?: return null
        return service.discreteScore(coreName, value)?.toKmp()
    }
}
