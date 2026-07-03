package co.onestep.kmp.uikit.models

sealed class OSTState {
    data object Uninitialized : OSTState()

    data object Ready : OSTState()

    data class Identified(
        val userId: String,
    ) : OSTState()

    data class Error(
        val code: Int,
        val message: String,
    ) : OSTState()
}
