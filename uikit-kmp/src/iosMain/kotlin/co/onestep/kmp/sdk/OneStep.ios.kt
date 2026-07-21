package co.onestep.kmp.sdk

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import co.onestep.kmp.uikit.models.OSTMotionMeasurement
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// iOS integration for the common `OneStep` SDK facade.
//
// Kotlin/Native cannot call the Swift `OneStepSDK` directly, so — exactly like the existing
// `SwiftSDKBridgeAdapter`/`IosSDKDelegate` pattern — the native host implements the Swift-facing
// delegate protocols below against `OneStepSDK.OneStep` and registers an adapter via
// [OSTOneStepIos]. Everything crossing the boundary is ObjC-friendly (primitives, maps, exported
// KMP types, completion handlers); StateFlow/Flow never cross — the adapter owns them and Swift
// pushes updates through [SwiftOneStepAdapter.onAuthStateChanged] / [SwiftOneStepAdapter.emitEvent].

/**
 * Swift-facing delegate for the auth-bound [OneStep] handle. A native class implements this by
 * delegating to `OneStepSDK.OneStep.shared()`.
 *
 * The two `setPatient` overloads are `async` on the native SDK, so they are exposed as
 * completion-handler methods. [clearPatient]/[updatePushToken]/[handleNotification] are synchronous
 * on the native SDK, so they return directly. For the failable ops a `null` `errorMessage` means
 * success; `errorCode` carries the native code (`0` when none).
 */
interface IosOneStepDelegate {
    fun setPatientWithApiKey(
        apiKey: String,
        customerPatientId: String,
        identityVerification: String?,
        userAttributes: OSTUserAttributes,
        completion: (patientId: String?, errorCode: Int, errorMessage: String?) -> Unit,
    )

    fun setPatientWithAuthUuid(
        authPatientUuid: String,
        userAttributes: OSTUserAttributes,
        completion: (errorCode: Int, errorMessage: String?) -> Unit,
    )

    /** Returns `null` on success or an error message on failure (native `logout()`). */
    fun clearPatient(): String?

    /** Returns `null` on success or an error message on failure (native `updatePushToken(_:)`). */
    fun updatePushToken(token: String): String?

    /** Returns whether OneStep handled the notification (native `handleNotification(_:)`). */
    fun handleNotification(payload: Map<String, String>): Boolean
}

/**
 * Swift-facing delegate for the patient-scoped operations reached via [OneStep.withPatient]. Each
 * call carries the target `patientId` (the raw UUID string); the native host resolves the
 * patient-bound scope (`OneStepSDK.OneStep.withPatient(patientId)`) for the operation. All ops are
 * `async` on the native SDK, so they use completion handlers. A `null` `errorMessage` means success.
 */
interface IosPatientScopeDelegate {
    fun sync(patientId: String, completion: (errorCode: Int, errorMessage: String?) -> Unit)

    fun getUserAttributes(
        patientId: String,
        completion: (attributes: OSTUserAttributes?, errorCode: Int, errorMessage: String?) -> Unit,
    )

    fun updateCustomMetadata(
        patientId: String,
        metadata: Map<String, Any>,
        completion: (merged: Map<String, Any>?, errorCode: Int, errorMessage: String?) -> Unit,
    )

    fun flush(
        patientId: String,
        remoteTroubleshooting: Boolean?,
        completion: (errorCode: Int, errorMessage: String?) -> Unit,
    )

    /**
     * Sets the display unit system for [patientId]'s measurements. [measurementSystem] is the
     * [OSTMeasurementSystem] name (`"METRIC"` / `"IMPERIAL"`). A `null` `errorMessage` means success.
     * Backs [OSTMotionLab.setMeasurementUnits].
     */
    fun setMeasurementUnits(
        patientId: String,
        measurementSystem: String,
        completion: (errorCode: Int, errorMessage: String?) -> Unit,
    )

    /**
     * Reads one analyzed measurement for [patientId] by [measurementId], handing back an
     * already-mapped KMP [OSTMotionMeasurement] (built with the iosMain mapper factory, including
     * `summaryUrl`). A `null` `measurement` with a `null` `errorMessage` is treated as not-found.
     * Backs [OSTMotionLab.readSingleMotionMeasurement].
     */
    fun readSingleMotionMeasurement(
        patientId: String,
        measurementId: String,
        completion: (measurement: OSTMotionMeasurement?, errorCode: Int, errorMessage: String?) -> Unit,
    )
}

/**
 * [OneStep] implementation that owns the coroutine/flow machinery in Kotlin and delegates
 * ObjC-friendly work to an [IosOneStepDelegate]. The native host creates one instance, wires its
 * delegate to push state via [onAuthStateChanged]/[emitEvent], and registers it with [OSTOneStepIos].
 */
class SwiftOneStepAdapter(private val delegate: IosOneStepDelegate) : OneStep {

    private val _identificationState = MutableStateFlow<OSTIdentificationState>(OSTIdentificationState.Unidentified)
    override val identificationState: StateFlow<OSTIdentificationState> = _identificationState.asStateFlow()

    private val _events = MutableSharedFlow<OSTEvent>(extraBufferCapacity = 64)
    override val events: Flow<OSTEvent> = _events.asSharedFlow()

    // --- Swift push functions ---

    /**
     * Push a new identification state from Swift (mapped from `OneStepSDK.OSTIdentificationState`).
     *
     * @param stateName one of `"unidentified"`, `"identified"`, `"lost"` (case-insensitive)
     * @param patientId the OneStep patient UUID for the `identified` state, else `null`
     * @param errorCode native error code for the `lost` state (`0` when none)
     * @param errorMessage native error message for the `lost` state
     */
    fun onAuthStateChanged(
        stateName: String,
        patientId: String? = null,
        errorCode: Int = 0,
        errorMessage: String? = null,
    ) {
        _identificationState.value = createIdentificationState(stateName, patientId, errorCode, errorMessage)
    }

    /** Emit an SDK/analytics event from Swift. */
    fun emitEvent(event: OSTEvent) {
        _events.tryEmit(event)
    }

    // --- OneStep ---

    override suspend fun setPatient(
        apiKey: String,
        customerPatientId: String,
        identityVerification: String?,
        userAttributes: OSTUserAttributesScope.() -> Unit,
    ): OSTResult<OSTPatientId?> {
        val attributes = OSTUserAttributesScope().apply(userAttributes).toOSTUserAttributes()
        return suspendCancellableCoroutine { continuation ->
            delegate.setPatientWithApiKey(apiKey, customerPatientId, identityVerification, attributes) { patientId, code, message ->
                continuation.resume(
                    if (message == null) {
                        OSTResult.Success(patientId?.let { OSTPatientId.fromString(it) })
                    } else {
                        OSTResult.Error(OSTError(code = code, message = message, details = null))
                    },
                )
            }
        }
    }

    override suspend fun setPatient(
        authPatientUuid: OSTPatientId,
        userAttributes: OSTUserAttributesScope.() -> Unit,
    ): OSTResult<Unit> {
        val attributes = OSTUserAttributesScope().apply(userAttributes).toOSTUserAttributes()
        return suspendCancellableCoroutine { continuation ->
            delegate.setPatientWithAuthUuid(authPatientUuid.value, attributes) { code, message ->
                continuation.resume(voidResult(code, message))
            }
        }
    }

    override fun clearPatient(): OSTResult<Unit> = voidResult(errorMessage = delegate.clearPatient())

    override fun updatePushToken(token: String): OSTResult<Unit> =
        voidResult(errorMessage = delegate.updatePushToken(token))

    override fun handleNotification(payload: Map<String, String>): OSTResult<DidHandleNotification> =
        OSTResult.Success(delegate.handleNotification(payload))
}

/** [OSTPatientScope] backed by an [IosPatientScopeDelegate], bound to a single [patientId]. */
class SwiftPatientScopeAdapter(
    private val delegate: IosPatientScopeDelegate,
    private val patientId: OSTPatientId,
) : OSTPatientScope {

    override suspend fun sync(): OSTResult<Unit> = suspendCancellableCoroutine { continuation ->
        delegate.sync(patientId.value) { code, message -> continuation.resume(voidResult(code, message)) }
    }

    override suspend fun getUserAttributes(): OSTResult<OSTUserAttributes> =
        suspendCancellableCoroutine { continuation ->
            delegate.getUserAttributes(patientId.value) { attributes, code, message ->
                continuation.resume(
                    if (attributes != null && message == null) {
                        OSTResult.Success(attributes)
                    } else {
                        OSTResult.Error(OSTError(code = code, message = message ?: "getUserAttributes failed", details = null))
                    },
                )
            }
        }

    override suspend fun updateCustomMetadata(metadata: Map<String, Any>): OSTResult<Map<String, Any>> =
        suspendCancellableCoroutine { continuation ->
            delegate.updateCustomMetadata(patientId.value, metadata) { merged, code, message ->
                continuation.resume(
                    if (merged != null && message == null) {
                        OSTResult.Success(merged)
                    } else {
                        OSTResult.Error(OSTError(code = code, message = message ?: "updateCustomMetadata failed", details = null))
                    },
                )
            }
        }

    override suspend fun flush(remoteTroubleshooting: Boolean?): OSTResult<Unit> =
        suspendCancellableCoroutine { continuation ->
            delegate.flush(patientId.value, remoteTroubleshooting) { code, message ->
                continuation.resume(voidResult(code, message))
            }
        }

    override fun getMotionLab(): OSTMotionLab = SwiftMotionLabAdapter(delegate, patientId)
}

/** [OSTMotionLab] backed by an [IosPatientScopeDelegate], bound to a single [patientId]. */
class SwiftMotionLabAdapter(
    private val delegate: IosPatientScopeDelegate,
    private val patientId: OSTPatientId,
) : OSTMotionLab {

    override suspend fun setMeasurementUnits(system: OSTMeasurementSystem): OSTResult<Unit> =
        suspendCancellableCoroutine { continuation ->
            delegate.setMeasurementUnits(patientId.value, system.name) { code, message ->
                continuation.resume(voidResult(code, message))
            }
        }

    override suspend fun readSingleMotionMeasurement(measurementId: String): OSTResult<OSTMotionMeasurement> =
        suspendCancellableCoroutine { continuation ->
            delegate.readSingleMotionMeasurement(patientId.value, measurementId) { measurement, code, message ->
                continuation.resume(
                    if (measurement != null && message == null) {
                        OSTResult.Success(measurement)
                    } else {
                        OSTResult.Error(
                            OSTError(
                                code = code,
                                message = message ?: "readSingleMotionMeasurement failed",
                                details = null,
                            ),
                        )
                    },
                )
            }
        }
}

/**
 * iOS registration entry point for the [OneStep] facade. The native host calls [register] once,
 * after the native `OneStepSDK.OneStep` is initialized, so `OneStep.getInstance()` /
 * `OneStep.withPatient(...)` resolve from KMP (Compose Multiplatform) code.
 */
object OSTOneStepIos {

    private var instance: OneStep? = null
    private var patientScopeDelegate: IosPatientScopeDelegate? = null

    /**
     * Register the auth-bound [oneStep] handle (typically a [SwiftOneStepAdapter]) and the
     * [patientScopeDelegate] backing [OneStep.withPatient]. Call once during native SDK setup.
     */
    fun register(oneStep: OneStep, patientScopeDelegate: IosPatientScopeDelegate) {
        this.instance = oneStep
        this.patientScopeDelegate = patientScopeDelegate
    }

    /**
     * Build an [OSTUserAttributes] carrying [customAttributes] for
     * [IosPatientScopeDelegate.getUserAttributes] to return. Exposed because the [OSTUserAttributes]
     * constructor is internal and the native host has only the flattened custom-attribute map (the
     * native SDK's well-known fields are not part of its public custom-metadata surface).
     */
    fun createUserAttributes(customAttributes: Map<String, Any>): OSTUserAttributes =
        OSTUserAttributesScope().apply {
            customAttributes.forEach { (key, value) -> withCustomAttribute(key, value) }
        }.toOSTUserAttributes()

    internal fun currentInstance(): OneStep? = instance

    internal fun currentPatientScopeDelegate(): IosPatientScopeDelegate? = patientScopeDelegate
}

internal actual fun oneStepGetInstance(): OSTResult<OneStep> =
    OSTOneStepIos.currentInstance()?.let { OSTResult.Success(it) }
        ?: OSTResult.Error(
            OSTError(
                code = 0,
                message = "OneStep SDK not initialized. The native host must call OSTOneStepIos.register(...) " +
                    "after initializing OneStepSDK.",
                details = null,
            ),
        )

internal actual fun oneStepWithPatient(patientId: OSTPatientId, patientScope: OSTPatientScope.() -> Unit) {
    val delegate = OSTOneStepIos.currentPatientScopeDelegate()
        ?: error(
            "OneStep SDK not initialized. The native host must call OSTOneStepIos.register(...) " +
                "before OneStep.withPatient(...).",
        )
    SwiftPatientScopeAdapter(delegate, patientId).patientScope()
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun createIdentificationState(
    stateName: String,
    patientId: String?,
    errorCode: Int,
    errorMessage: String?,
): OSTIdentificationState = when (stateName.lowercase()) {
    "identified" ->
        patientId?.let { OSTIdentificationState.Identified(OSTPatientId.fromString(it)) }
            ?: OSTIdentificationState.Unidentified
    "lost" ->
        OSTIdentificationState.Lost(
            OSTError(code = errorCode, message = errorMessage ?: "Session lost", details = null),
        )
    else -> OSTIdentificationState.Unidentified
}

private fun voidResult(errorCode: Int = 0, errorMessage: String?): OSTResult<Unit> =
    if (errorMessage == null) {
        OSTResult.Success(Unit)
    } else {
        OSTResult.Error(OSTError(code = errorCode, message = errorMessage, details = null))
    }
