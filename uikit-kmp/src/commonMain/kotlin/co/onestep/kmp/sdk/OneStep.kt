package co.onestep.kmp.sdk

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlin.jvm.JvmInline

/**
 * Result of notification handling.
 *
 * `true` if the SDK handled this notification (e.g., measurement analysis ready,
 * monitoring alert, system message). `false` if it's unrelated and should be
 * handled by the app's own notification logic.
 */
typealias DidHandleNotification = Boolean

@JvmInline
value class OSTPatientId internal constructor(val value: String) {
    companion object {
        fun fromString(string: String): OSTPatientId {
            return OSTPatientId(string)
        }
    }
}

/**
 * The OneStep SDK handle.
 *
 * Singleton entry point for the SDK. Obtained via [OneStep.initialize], which
 * must be called once per process before any other SDK operations. The instance
 * is stable for the lifetime of the app; authentication state changes are
 * observed through [identificationState] flow, not by replacing the reference.
 *
 * All product surfaces ([co.onestep.android.core.motionLab.OSTMotionLab], [OSTInsightsData], [co.onestep.android.core.monitoring.OSTMonitoring], [OSTPatientAdmin])
 * and patient-scoped operations require a patient to be [identified][setPatient]
 * first. Until then, they return [OSTResult.Error] with [OSTError.Type.NotIdentified].
 */
interface OneStep {

    /**
     * Observable SDK authentication state.
     *
     * Single source of truth for identification status. Emits:
     * - [OSTIdentificationState.Identified] when a patient has been successfully bound
     * - [OSTIdentificationState.Unidentified] when the SDK boots or after [clearPatient] is called
     * - [OSTIdentificationState.Lost] when an authenticated session expires (e.g. 401/403)
     *
     * Use [setPatient] to transition to [OSTIdentificationState.Identified]. Observe this flow to react
     * to auth loss at any time.
     */
    val identificationState: StateFlow<OSTIdentificationState>

    /**
     * Observable SDK events for analytics and observability.
     *
     * Emits [OSTEvent] instances for events like recording_started,
     * measurement_analyzed, and other SDK lifecycle events.
     * Use this to track SDK activity and implement custom analytics.
     *
     * Safe to observe **before** [initialize] is called — no events will
     * be emitted until the SDK is initialized and active.
     */
    val events: Flow<OSTEvent>

    /**
     * Identifies the current user.
     *
     * Links the device to a specific user profile. Required for most SDK functionality
     * including Monitoring and MotionLab. Safe to call multiple times (e.g., after login,
     * after fetching user config from your backend).
     *
     * Identity is persisted in the SDK and will be reused until [clearPatient] is called.
     *
     * ## Prerequisites
     *
     * The SDK must be initialized before calling this method:
     * ```kotlin
     * OneStep.initialize(application, onAuthLost) { /* configuration */ }
     * ```
     *
     * ## Identity Verification
     *
     * The [identityVerification] parameter is **strongly recommended in production**.
     * It proves that your backend authorized access to this user's data, preventing
     * one user from impersonating another by guessing their userId.
     *
     * Generate it server-side using HMAC-SHA256:
     * ```
     * HMAC-SHA256(userId, oneStepSecret)
     * ```
     *
     * For development/testing, you can pass `null` to skip verification, but this
     * **must not** be used in production environments.
     *
     * @param customerPatientId Your unique identifier for this user (e.g., database primary key)
     * @param identityVerification Optional HMAC-SHA256 signature from your backend.
     *        Required for production to enforce authorization.
     * @param userAttributes Optional user attributes to set atomically with identification.
     *        When provided, attributes are sent to the backend immediately after successful
     *        authentication, avoiding race conditions that can occur when calling
     *        [OSTPatientAdmin.updateUserAttributes] (reached via [getPatientAdmin]) separately.
     * @param apiKey OneStep client token. Use this when the token is obtained
     *        from your backend **after** SDK initialization (late-arriving token). If provided,
     *        it is stored and used for authentication, avoiding the need to re-call [initialize].
     * @return [co.onestep.android.core.OSTResult.Success] if authenticated, [co.onestep.android.core.OSTResult.Error] otherwise
     */
    suspend fun setPatient(
        apiKey: String,
        customerPatientId: String,
        identityVerification: String?,
        userAttributes: OSTUserAttributesScope.() -> Unit = {},
    ): OSTResult<OSTPatientId?>

    /** Consumer-issued auth: the customer's networking layer has already established
     *  a OneStep session (cookies present in the cookie jar). The customer declares
     *  the auth-bound patient via `patientUuid` — a UUID for patient sessions, null
     *  for clinician sessions. In this revision the call does no synchronous network
     *  work; it stores the declared binding, returns it back inside `OSTResult`, and
     *  may fire `PATCH /agent` in the background as a best-effort liveness ping
     *  (401/403 from that ping surface through `state`, like any other authenticated
     *  call). A future revision may add an optional `verify` flag that turns the
     *  agent ping into a synchronous verification of the declared UUID. */
    suspend fun setPatient(
        authPatientUuid: OSTPatientId,
        userAttributes: OSTUserAttributesScope.() -> Unit = {},
    ): OSTResult<Unit>

    /**
     * Clears the current user and reverts to unauthenticated state.
     *
     * Stops all background monitoring, clears cached credentials, and transitions
     * [identificationState] to [OSTIdentificationState.Unidentified]. The SDK remains
     * initialized and ready for a new user via [setPatient].
     *
     * Call this when the user logs out of your app.
     */
    fun clearPatient(): OSTResult<Unit>

    // ---------------- Device & Push Notifications ----------------

    /**
     * Registers or updates the FCM/APNs push token for this device.
     *
     * Call this when you receive a new token from Firebase Cloud Messaging
     * or Apple Push Notification service. The SDK uses this to send
     * notifications for monitoring alerts, measurement analysis completion, etc.
     *
     * Safe to call multiple times; the SDK will update the token if it changed.
     *
     * @param token The FCM registration token (Android) or APNs device token (iOS)
     */
    fun updatePushToken(token: String): OSTResult<Unit>

    /**
     * Processes a push notification payload if it belongs to OneStep.
     *
     * Call this from your FCM/APNs notification receiver to let the SDK
     * handle its own notifications (e.g., measurement analysis ready,
     * monitoring alerts, system messages).
     *
     * @param payload The notification data payload (RemoteMessage.data on Android)
     * @return [OSTResult.Success] wrapping `true` if this notification was handled by
     *         OneStep, `false` otherwise. If `false`, you should handle it with your
     *         own logic.
     */
    fun handleNotification(payload: Map<String, String>): OSTResult<DidHandleNotification>


    companion object {
        @Suppress("unused")
        fun getInstance(): OSTResult<OneStep> = oneStepGetInstance()

        fun withPatient(patientId: OSTPatientId, patientScope: OSTPatientScope.() -> Unit) = oneStepWithPatient(patientId, patientScope)
    }
}

/**
 * Patient-scoped operation context for explicit-patient access.
 *
 * Created inside [OneStep.withPatient] blocks to support multi-patient access patterns
 * (e.g. clinician apps operating on behalf of many patients). Exposes the same
 * patient-scoped operations as the auth-bound [OneStep] handle, but bound to an
 * explicitly passed patient.
 *
 * Products and operations accessed within the scope are cached for the scope's lifetime,
 * so repeated lookups return the same instance.
 */
interface OSTPatientScope {
    /**
     * Triggers an immediate data sync (foreground + background data).
     *
     * Forces the SDK to upload any pending data and pull latest results
     * from the backend. Safe to call on the main thread; heavy work
     * happens asynchronously.
     */
    suspend fun sync(): OSTResult<Unit>

    /**
     * Fetches the current user's attributes from the backend.
     *
     * Returns the well-known attributes (name, DOB, etc.) and any custom
     * attributes previously stored via [OSTPatientAdmin.updateUserAttributes] or by another
     * client. Useful when the host app wants to use the SDK as a
     * lightweight per-user metadata store.
     *
     * Requires the SDK to be initialized and a user identified. If called
     * before [initialize], returns [OSTResult.Error] with
     * [OSTError.Type.NotInitialized]. Network failures surface as
     * [OSTResult.Error] with [OSTError.Type.NetworkError].
     *
     * @return [OSTResult.Success] with [OSTUserAttributes] on success,
     *         [OSTResult.Error] otherwise.
     */
    suspend fun getUserAttributes(): OSTResult<OSTUserAttributes>

    /**
     * Partially updates the custom metadata stored against the identified user.
     *
     * Unlike [OSTPatientAdmin.updateUserAttributes] (which uses PATCH /profile and replaces the
     * `custom_metadata` field as a whole), this call targets the dedicated
     * metadata endpoint and the backend merges the supplied entries with the
     * existing map, so keys not present in [metadata] are preserved.
     *
     * Returns the merged result on success, so the host app can sync its
     * local cache without an extra round-trip. Keys with `null` semantics are
     * not supported — the API only accepts non-null values.
     *
     * Keys prefixed with `ost.` are reserved for SDK-internal use (for example
     * `ost.ui.suppress_short_hallway_6min`); hosts must not write them.
     * Reading them is fine.
     *
     * @param metadata The entries to merge into the stored map.
     * @return [OSTResult.Success] with the full merged map, or
     *         [OSTResult.Error] on failure / when not initialized.
     */
    suspend fun updateCustomMetadata(
        metadata: Map<String, Any>,
    ): OSTResult<Map<String, Any>>

    /**
     * Forces an immediate flush of the SDK's event ingest pipeline.
     *
     * Drains all pending log + analytic events from the local DB into a sealed
     * NDJSON file, uploads them to the OneStep ingest backend, then clears the
     * uploaded rows on success. Same rules as the periodic upload cycle: DEBUG
     * events are filtered out of the payload, retryable failures leave the rows
     * in place for the next cycle, non-retryable failures tombstone the rows.
     *
     * Useful when the host app wants to ship pending events on demand — for
     * example before the user logs out or right after a known incident.
     *
     * Safe to call from any thread; all DB and network I/O runs on a background
     * dispatcher.
     *
     * @param remoteTroubleshooting When `null` (default), the cycle reads the
     *   live remote-troubleshooting flag. When `true`/`false`, that value
     *   overrides the flag for this single cycle: `true` ships the full DEBUG
     *   backlog, `false` filters DEBUG out even if the flag is on.
     * @return `true` if the flush succeeded (or there was nothing to flush);
     *         `false` if the upload failed and will be retried by the periodic worker.
     */
    suspend fun flush(remoteTroubleshooting: Boolean? = null): OSTResult<Unit>

    /**
     * MotionLab product for this patient scope.
     *
     * The returned [OSTMotionLab] is bound to this scope's patient (recordings and reads are
     * attributed to that patient). Native: `co.onestep.android.core.motionLab.getMotionLab()`
     * on `OSTPatientScope`.
     */
    fun getMotionLab(): OSTMotionLab
}

/**
 * SDK authentication state — the single source of truth for whether a patient is bound.
 *
 * Emitted by [OneStep.identificationState] flow. Transitions:
 * - Boot → [Unidentified]
 * - [OneStep.setPatient] succeeds → [Identified]
 * - [OneStep.clearPatient] called → [Unidentified]
 * - 401/403 on any backend call → [Lost]
 *
 * Observe this flow to react to auth state changes, especially [Lost] transitions
 * which signal session expiry and require re-authentication.
 */
sealed interface OSTIdentificationState {
    /**
     * The SDK is initialized but no patient is bound.
     *
     * Returned by [OneStep.setPatient], auth-scoped operations ([OneStep.getMotionLab], [OneStep.getInsights],
     * [OneStep.getMonitoring], [OneStep.getPatientAdmin]) return [OSTError.Type.NotIdentified].
     * Call [OneStep.setPatient] to transition to [Identified].
     */
    data object Unidentified : OSTIdentificationState

    /**
     * A patient is authenticated and bound to the session.
     *
     * [OneStep.getMotionLab], [OneStep.getInsights], [OneStep.getMonitoring], [OneStep.getPatientAdmin] and other
     * patient-scoped operations return success results.
     *
     * @property patientId The OneStep UUID of the authenticated patient
     */
    data class Identified(val patientId: OSTPatientId) : OSTIdentificationState

    /**
     * Authentication was lost (session expired, revoked, or 401/403 received).
     *
     * Indicates the session is no longer valid and the patient is temporarily unreachable.
     * Recover by calling [OneStep.setPatient] again with fresh credentials.
     *
     * @property cause The error describing the loss (typically [OSTError.Type.SessionExpired])
     */
    data class Lost(val cause: OSTError) : OSTIdentificationState
}

internal data class PatientContext(
    // OneStep unique identifier for the patient
    val oneStepPatientId: OSTPatientId,
    // the customerPatientId used in identify() for this session, if any
    val customerExternalPatientId: String?,
)

/**
 * Configuration builder passed to [OneStep.initialize].
 *
 * Holds opt-in flags and overrides consumed by individual SDK subsystems.
 * Most integrators do not need to set additional configuration.
 */
class OSTConfigScope internal constructor() {

    private var additionalConfig = mutableMapOf<String, Any>()

    /**
     * Supply opt-in configuration flags to individual SDK subsystems.
     *
     * Configuration is documented per subsystem (e.g. logging verbosity via `OSTEnv`,
     * base-URL override). Pass zero or more key-value pairs; all pairs are merged.
     *
     * @param pairs Varargs of key-value configuration pairs
     */
    fun additionalConfiguration(vararg pairs: Pair<String, Any>) {
        additionalConfig.putAll(pairs)
    }

    internal fun toOSTConfiguration(): OSTConfiguration = OSTConfiguration(additionalConfig = additionalConfig)
}

internal expect fun oneStepGetInstance(): OSTResult<OneStep>

internal expect fun oneStepWithPatient(patientId: OSTPatientId, patientScope: OSTPatientScope.() -> Unit)
