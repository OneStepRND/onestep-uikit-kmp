package co.onestep.kmp.uikit.models

data class OSTNorm(
    var units: String? = null,
    val parts: List<OSTNormPart>? = null,
)

data class OSTNormPart(
    var start: Float,
    var end: Float,
    val color: String,
    val includeStart: Boolean,
    val includeEnd: Boolean,
)
