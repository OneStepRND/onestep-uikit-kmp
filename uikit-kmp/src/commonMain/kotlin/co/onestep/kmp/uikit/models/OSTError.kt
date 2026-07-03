package co.onestep.kmp.uikit.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OSTError(
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String,
    @SerialName("details") val details: String? = null,
)
