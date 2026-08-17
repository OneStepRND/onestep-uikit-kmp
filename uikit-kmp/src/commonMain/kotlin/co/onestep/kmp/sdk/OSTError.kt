package co.onestep.kmp.sdk

/**
 * The failure carried by [OSTResult.Error].
 *
 * This is an interface, not a concrete type: consumers are free to supply their own error
 * model (sealed hierarchies, domain-specific types, wrapped exceptions) and still use
 * [OSTResult] and the extensions in `OSTResultExtensions.kt`. The only contract is a
 * human-readable [message].
 *
 * The SDK's own errors are produced through `String.asErrorResult(...)`.
 */
interface OSTError {

    /** Human-readable description of the failure. Never PII/PHI (HIPAA). */
    val message: String
}
