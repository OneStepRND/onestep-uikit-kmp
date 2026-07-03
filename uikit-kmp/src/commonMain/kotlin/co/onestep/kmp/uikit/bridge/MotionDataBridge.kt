package co.onestep.kmp.uikit.bridge

import co.onestep.kmp.uikit.models.OSTDiscreteColor
import co.onestep.kmp.uikit.models.OSTMotionMeasurement
import co.onestep.kmp.uikit.models.OSTNorm
import co.onestep.kmp.uikit.models.OSTParamName
import co.onestep.kmp.uikit.models.OSTParameterMetadata

/**
 * Bridge interface abstracting OSTMotionDataService.
 * Provides access to measurement data, norms, and parameter metadata.
 */
interface MotionDataBridge {

    fun mainParam(motionMeasurement: OSTMotionMeasurement): Pair<OSTParamName, Float>?

    fun getAllParametersMetadata(): Map<OSTParamName, OSTParameterMetadata>

    fun getNormByName(name: OSTParamName?): OSTNorm?

    fun getParameterMetadata(paramName: OSTParamName): OSTParameterMetadata

    fun isWithinNorms(
        param: OSTParamName,
        value: Float,
    ): Boolean?

    fun discreteScore(
        motionMeasurement: OSTMotionMeasurement,
        value: Float,
    ): OSTDiscreteColor?

    fun discreteScore(
        param: OSTParamName,
        value: Float,
    ): OSTDiscreteColor?
}
