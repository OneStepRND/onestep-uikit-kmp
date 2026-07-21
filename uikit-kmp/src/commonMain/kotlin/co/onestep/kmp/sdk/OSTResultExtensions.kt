package co.onestep.kmp.sdk

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Flat-map a function over the _value_ of a successful OSTResult.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T, Tʹ> OSTResult<T>.flatMap(transform: (T) -> OSTResult<Tʹ>): OSTResult<Tʹ> {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }
    return when (this) {
        is OSTResult.Success<T> -> transform(data)
        is OSTResult.Error -> this
    }
}

/**
 * Map a function over the _value_ of a successful OSTResult.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T, Tʹ> OSTResult<T>.map(transform: (T) -> Tʹ): OSTResult<Tʹ> {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }
    return flatMap { value -> OSTResult.Success(transform(value)) }
}

/**
 * Replace the value of a successful OSTResult with a constant.
 *
 * If this result is [OSTResult.Success], ignores the original value and returns
 * success with [default]. If this result is [OSTResult.Error], returns it unchanged.
 *
 * Useful for chaining operations that don't care about intermediate results.
 *
 * @param default The constant value to return on success
 * @return A new [OSTResult] with the constant value, or the original error
 *
 * ## Example
 * ```kotlin
 * startRecording().map(Unit)  // Discard the returned recording ID
 * ```
 */
fun <T, Tʹ> OSTResult<T>.map(default: Tʹ): OSTResult<Tʹ> = flatMap { OSTResult.Success(default) }

/**
 * Flat-map a function over the _cause_ of a failed OSTResult.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> OSTResult<T>.flatMapError(transform: (OSTError) -> OSTResult<T>): OSTResult<T> {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }
    return when (this) {
        is OSTResult.Success<T> -> this
        is OSTResult.Error -> transform(cause)
    }
}

/**
 * Map a function over the _cause_ of a failed OSTResult.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> OSTResult<T>.mapError(transform: (OSTError) -> OSTError): OSTResult<T> {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }
    return flatMapError { cause -> OSTResult.Error(transform(cause)) }
}

/**
 * Run a side-effect block on success, returning the result unchanged.
 *
 * If this result is [OSTResult.Success], executes [block] with the value,
 * then returns the success unchanged. If this result is [OSTResult.Error],
 * returns it unchanged without executing the block.
 *
 * Useful for logging, analytics, or state updates that don't transform the result.
 *
 * @param block Side-effect function to execute on success
 * @return This result unchanged
 *
 * ## Example
 * ```kotlin
 * getUserAttributes()
 *     .onSuccess { attrs -> analytics.trackProfileLoaded(attrs.email) }
 *     .map { it.email }
 * ```
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> OSTResult<T>.onSuccess(block: (T) -> Unit): OSTResult<T> {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return when (this) {
        is OSTResult.Success<T> -> {
            block(data)
            this
        }
        is OSTResult.Error -> this
    }
}

/**
 * Run a side-effect block on error, returning the result unchanged.
 *
 * If this result is [OSTResult.Error], executes [block] with the error,
 * then returns the error unchanged. If this result is [OSTResult.Success],
 * returns it unchanged without executing the block.
 *
 * Useful for logging, analytics, or error-specific state updates that don't
 * transform the result.
 *
 * @param block Side-effect function to execute on error
 * @return This result unchanged
 *
 * ## Example
 * ```kotlin
 * setPatient(apiKey, customerId, verification)
 *     .onError { err -> log.e("Auth failed: ${err.cause.message}") }
 *     .onSuccess { patientId -> navigateToHome(patientId) }
 * ```
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> OSTResult<T>.onError(block: (OSTResult.Error) -> Unit): OSTResult<T> {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return when (this) {
        is OSTResult.Success -> this
        is OSTResult.Error -> {
            block(this)
            this
        }
    }
}

/**
 * Extract the value on success, or compute a fallback on error.
 *
 * If this result is [OSTResult.Success], returns the value. If this result is
 * [OSTResult.Error], invokes [fallback] with the error and returns its result.
 *
 * Useful for providing sensible defaults when operations fail.
 *
 * @param fallback Function invoked on error to produce the fallback value
 * @return The success value, or the result of [fallback]
 *
 * ## Example
 * ```kotlin
 * getUserAttributes().getOr { error -> OSTUserAttributes.DEFAULT }
 * ```
 */
inline infix fun <T> OSTResult<T>.getOr(fallback: (OSTResult.Error) -> T): T = when (this) {
    is OSTResult.Success -> data
    is OSTResult.Error -> fallback(this)
}

/**
 * Extract the value on success, or return a constant fallback on error.
 *
 * If this result is [OSTResult.Success], returns the value. If this result is
 * [OSTResult.Error], returns [fallbackParam].
 *
 * Useful for providing a fixed default when operations fail.
 *
 * @param fallbackParam The constant fallback value to return on error
 * @return The success value, or the fallback value
 *
 * ## Example
 * ```kotlin
 * getUserAttributes().getOr(emptyMap())
 * ```
 */
infix fun <T> OSTResult<T>.getOr(fallbackParam: T): T = when (this) {
    is OSTResult.Success -> data
    is OSTResult.Error -> fallbackParam
}

/**
 * Extract the value on success, or throw an exception on error.
 *
 * If this result is [OSTResult.Success], returns the value. If this result is
 * [OSTResult.Error], invokes [throwable] with the error and throws its result.
 *
 * Useful for converting SDK errors to exceptions when the caller prefers
 * exception-based error handling.
 *
 * @param throwable Function that produces the exception to throw
 * @return The success value
 * @throws Throwable The exception produced by [throwable]
 *
 * ## Example
 * ```kotlin
 * setPatient(apiKey, customerId, verification)
 *     .getOrThrow { error -> IllegalStateException("Auth failed: ${error.message}") }
 * ```
 */
infix fun <T> OSTResult<T>.getOrThrow(throwable: (OSTError) -> Throwable): T = when (this) {
    is OSTResult.Success -> data
    is OSTResult.Error -> throw throwable(cause)
}

/**
 * Extract the value on success, or invoke a never-returning function on error.
 *
 * If this result is [OSTResult.Success], returns the value. If this result is
 * [OSTResult.Error], invokes [block] with the error. The block must never return
 * (e.g. it always throws or calls `error(...)`).
 *
 * Useful for converting errors to exceptions or fatal states when the caller
 * prefers to guarantee success or terminate.
 *
 * @param block Function that handles the error and never returns
 * @return The success value
 * @throws Throwable Whatever the block throws
 *
 * ## Example
 * ```kotlin
 * initialize().guard { error -> error("SDK init failed: ${error.message}") }
 * ```
 */
infix fun <T> OSTResult<T>.guard(block: (OSTError) -> Nothing): T = when (this) {
    is OSTResult.Success -> data
    is OSTResult.Error -> block(cause)
}

/**
 * Wrap any value in [OSTResult.Success].
 */
fun <T> T.asSuccess(): OSTResult.Success<T> = OSTResult.Success(this)

/**
 * Build an [OSTResult.Error] from a message (and optional numeric code / details).
 */
fun String.asErrorResult(code: Int = 0, details: String? = null): OSTResult.Error =
    OSTResult.Error(OSTError(code = code, message = this, details = details))

/**
 * The error message of a failed result, or null on success.
 */
val OSTResult<*>.errorMessage: String?
    get() =
        when (this) {
            is OSTResult.Error -> cause.message
            is OSTResult.Success<*> -> null
        }
