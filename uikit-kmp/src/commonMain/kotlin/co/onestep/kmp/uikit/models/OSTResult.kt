package co.onestep.kmp.uikit.models

sealed class OSTResult<out T> {

    data class Success<out T>(
        val data: T,
    ) : OSTResult<T>()

    data class Error(
        val exception: Throwable,
        val code: Int? = null,
    ) : OSTResult<Nothing>()
}
