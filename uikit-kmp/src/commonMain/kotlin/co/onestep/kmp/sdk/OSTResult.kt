package co.onestep.kmp.sdk


/**
 * A discriminated union representing the outcome of an SDK operation.
 *
 * Every async or failable call returns an [OSTResult]. Callers should
 * consume it through the extensions in `OSTResultExtensions.kt`
 * (`map`, `flatMap`, `mapError`, `flatMapError`, `onSuccess`, `onError`,
 * `getOr`, `getOrThrow`, `guard`) rather than branching with a `when`
 * expression or catching exceptions directly.
 *
 * @param T The type of the payload carried by a successful result.
 */
sealed class OSTResult<out T> {

    val isSuccess: Boolean = when (this) {
        is Success<*> -> true
        is Error -> false
    }

    /**
     * Indicates that the operation completed successfully.
     *
     * @property data The result payload returned by the operation.
     */
    data class Success<out T>(val data: T) : OSTResult<T>()

    /**
     * Indicates that the operation failed.
     *
     * @property cause The specific [OSTError] identifying the failure category.
     */
    data class Error(val cause: OSTError) : OSTResult<Nothing>()
}