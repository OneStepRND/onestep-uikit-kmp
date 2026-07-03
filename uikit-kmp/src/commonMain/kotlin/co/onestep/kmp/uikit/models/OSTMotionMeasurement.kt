package co.onestep.kmp.uikit.models

import kotlinx.serialization.Serializable

@Serializable
data class OSTMotionMeasurement(
    val id: String,
    val timestamp: Long,
    var type: OSTActivityType,
    val customMetadata: Map<String, String> = emptyMap(),
    val metadata: OSTMeasurementMetadata = OSTMeasurementMetadata(),
    val params: Map<String, Float> = emptyMap(),
    val parameterArrays: Map<String, List<Float>> = emptyMap(),
    val status: MotionMeasurementStatus,
    var error: OSTError? = null,
    var resultState: OSTResultState? = null,
) {
    @Serializable
    enum class MotionMeasurementStatus {
        NOT_SYNCED,
        SYNCED,
        ANALYZED,
    }

    /** Helper to get params by OSTParamName. */
    fun getParam(paramName: OSTParamName): Float? =
        params[paramName.columnName]

    /** Helper to get all params as OSTParamName map. */
    fun paramsByName(): Map<OSTParamName, Float> =
        params.toParamName()
}
