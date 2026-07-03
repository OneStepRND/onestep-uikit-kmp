package co.onestep.kmp.uikit.models

import kotlinx.serialization.Serializable

@Serializable
enum class OSTResultState(
    val value: Int,
    val displayName: String,
) {
    FULL_ANALYSIS(2, "Full Analysis"),
    PARTIAL_ANALYSIS(1, "Partial Analysis"),
    EMPTY_ANALYSIS(0, "Empty Analysis"),
}

fun Int.toResultState() =
    when (this) {
        2 -> OSTResultState.FULL_ANALYSIS
        1 -> OSTResultState.PARTIAL_ANALYSIS
        0 -> OSTResultState.EMPTY_ANALYSIS
        else -> null
    }
