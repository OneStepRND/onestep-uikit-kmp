package co.onestep.kmp.uikit.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OSTDailyBackgroundMeasurement(
    @SerialName("date_local")
    val dateLocal: String,
    @SerialName("timestamp")
    val timestamp: Long,
    @SerialName("parameters")
    val parameters: Map<String, Float>,
    @SerialName("last_modified")
    val lastModified: Long,
) {
    /** Helper to get parameters as OSTParamName map. */
    fun parametersByName(): Map<OSTParamName, Float> =
        parameters.toParamName()
}
