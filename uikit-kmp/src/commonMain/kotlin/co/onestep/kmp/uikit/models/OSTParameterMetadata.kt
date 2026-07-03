package co.onestep.kmp.uikit.models

data class OSTParameterMetadata(
    val activity: OSTActivityType,
    val displayName: String,
    val units: String?,
    val imperialUnits: String? = null,
    val category: String,
    val lowRange: Float?,
    val sortKey: Float? = null,
    val isMainParam: Boolean? = null,
    val highRange: Float?,
    val roundDigits: Float?,
    val imperialRoundDigits: Float? = null,
)
