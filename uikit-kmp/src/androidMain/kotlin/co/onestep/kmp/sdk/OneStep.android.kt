package co.onestep.kmp.sdk

import android.app.Application
import co.onestep.android.core.OneStep as CoreOneStep
import co.onestep.android.core.OneStepInternalApi
import co.onestep.android.core.OSTError as CoreError
import co.onestep.android.core.OSTIdentificationState as CoreState
import co.onestep.android.core.OSTPatientId as CorePatientId
import co.onestep.android.core.OSTPatientScope as CorePatientScope
import co.onestep.android.core.OSTResult as CoreResult
import co.onestep.android.core.OSTUserAttributes as CoreUserAttributes
import co.onestep.android.core.OSTUserAttributesScope as CoreUserAttributesScope
import co.onestep.android.core.motionLab.OSTMeasurementSystem as CoreMeasurementSystem
import co.onestep.android.core.motionLab.OSTMotionLab as CoreMotionLab
import co.onestep.android.core.motionLab.getMotionLab
import co.onestep.kmp.uikit.bridge.android.configureWithAndroidSDK
import co.onestep.kmp.uikit.bridge.android.toKmp
import co.onestep.kmp.uikit.di.UIKitServiceLocator
import co.onestep.kmp.uikit.models.OSTMotionMeasurement
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import java.util.Date
import kotlin.time.Instant

/**
 * Initializes the OneStep SDK.
 *
 * Android-only entry point: the underlying Android SDK `OneStep.initialize` needs an
 * [Application], which commonMain cannot reference — so `initialize` lives here as an extension
 * rather than in the shared [OneStep] companion. Everything it returns is mapped to the common
 * `co.onestep.kmp.sdk` types so KMP callers never see the Android SDK surface.
 *
 * Should be called once during app startup, typically in `Application.onCreate()`.
 *
 * @param application The Android Application instance
 * @param onAuthLost Callback invoked when the session is lost mid-app (401/403, revocation, etc.)
 * @param configuration Optional [OSTConfigScope] builder for SDK configuration
 */
@Suppress("unused")
fun OneStep.Companion.initialize(
    application: Application,
    onAuthLost: (OSTError) -> Unit,
    configuration: OSTConfigScope.() -> Unit = {},
): OSTResult<OneStep> {
    val configPairs = OSTConfigScope().apply(configuration)
        .toOSTConfiguration().additionalConfig
        .map { (key, value) -> key to value }
        .toTypedArray()
    val coreResult = CoreOneStep.initialize(
        application = application,
        onAuthLost = { onAuthLost(it.toKmpError()) },
    ) {
        if (configPairs.isNotEmpty()) additionalConfiguration(*configPairs)
    }
    return when (coreResult) {
        is CoreResult.Success -> {
            // Wire the UIKit service locator with SDK-backed Android bridges so the UIKit flows
            // (OSTRecordingFlow / OSTMeasurementSummary) resolve their dependencies without the host
            // touching co.onestep.android.core. Uses the default AndroidPatientScopedBridgesFactory,
            // which is only exercised for patientId-scoped (clinician-mode) launches and inert for
            // single-patient hosts. Safe to re-run: configure() just re-sets the singletons.
            UIKitServiceLocator.configureWithAndroidSDK(application, coreResult.data)
            OSTResult.Success(AndroidOneStep(coreResult.data))
        }
        is CoreResult.Error -> OSTResult.Error(coreResult.cause.toKmpError())
    }
}

internal actual fun oneStepGetInstance(): OSTResult<OneStep> =
    CoreOneStep.getInstance().toKmpResult { AndroidOneStep(it) }

@OptIn(OneStepInternalApi::class)
internal actual fun oneStepWithPatient(patientId: OSTPatientId, patientScope: OSTPatientScope.() -> Unit) {
    CoreOneStep.withPatient(patientId.toCorePatientId()) {
        // `this` is the Android core OSTPatientScope; expose it to callers as the common scope.
        AndroidPatientScope(this).patientScope()
    }
}

/**
 * Adapts the Android SDK [co.onestep.android.core.OneStep] handle to the common [OneStep] facade,
 * mapping every value at the boundary so callers only ever touch `co.onestep.kmp.sdk` types.
 */
internal class AndroidOneStep(private val delegate: CoreOneStep) : OneStep {

    @OptIn(ExperimentalForInheritanceCoroutinesApi::class)
    override val identificationState: StateFlow<OSTIdentificationState>
        get() = object : StateFlow<OSTIdentificationState> {
            private val source = delegate.identificationState
            override val replayCache: List<OSTIdentificationState>
                get() = source.replayCache.map { it.toKmpState() }
            override val value: OSTIdentificationState get() = source.value.toKmpState()
            override suspend fun collect(collector: FlowCollector<OSTIdentificationState>): Nothing {
                source.collect { collector.emit(it.toKmpState()) }
            }
        }

    override val events: Flow<OSTEvent>
        get() = delegate.events.map { event ->
            OSTEvent(
                name = event.name,
                properties = event.properties.mapValues { it.value.toString() },
                timestamp = event.timestamp,
            )
        }

    override suspend fun setPatient(
        apiKey: String,
        customerPatientId: String,
        identityVerification: String?,
        userAttributes: OSTUserAttributesScope.() -> Unit,
    ): OSTResult<OSTPatientId?> {
        val attributes = OSTUserAttributesScope().apply(userAttributes).toOSTUserAttributes()
        return delegate.setPatient(apiKey, customerPatientId, identityVerification) {
            applyKmpAttributes(attributes)
        }.toKmpResult { it?.toKmpPatientId() }
    }

    @OptIn(OneStepInternalApi::class)
    override suspend fun setPatient(
        authPatientUuid: OSTPatientId,
        userAttributes: OSTUserAttributesScope.() -> Unit,
    ): OSTResult<Unit> {
        val attributes = OSTUserAttributesScope().apply(userAttributes).toOSTUserAttributes()
        return delegate.setPatient(authPatientUuid.toCorePatientId()) {
            applyKmpAttributes(attributes)
        }.toKmpResult()
    }

    override fun clearPatient(): OSTResult<Unit> = delegate.clearPatient().toKmpResult()

    override fun updatePushToken(token: String): OSTResult<Unit> =
        delegate.updatePushToken(token).toKmpResult()

    override fun handleNotification(payload: Map<String, String>): OSTResult<DidHandleNotification> =
        delegate.handleNotification(payload).toKmpResult()
}

/** Adapts the Android core [co.onestep.android.core.OSTPatientScope] to the common [OSTPatientScope]. */
internal class AndroidPatientScope(private val delegate: CorePatientScope) : OSTPatientScope {

    override suspend fun sync(): OSTResult<Unit> = delegate.sync().toKmpResult()

    override suspend fun getUserAttributes(): OSTResult<OSTUserAttributes> =
        delegate.getUserAttributes().toKmpResult { it.toKmpAttributes() }

    override suspend fun updateCustomMetadata(metadata: Map<String, Any>): OSTResult<Map<String, Any>> =
        delegate.updateCustomMetadata(metadata).toKmpResult()

    override suspend fun flush(remoteTroubleshooting: Boolean?): OSTResult<Unit> =
        delegate.flush(remoteTroubleshooting).toKmpResult()

    override fun getMotionLab(): OSTMotionLab = AndroidMotionLab(delegate.getMotionLab())
}

/** Adapts the Android core [CoreMotionLab] (patient-scoped) to the common [OSTMotionLab]. */
internal class AndroidMotionLab(private val delegate: CoreMotionLab) : OSTMotionLab {

    override suspend fun setMeasurementUnits(system: OSTMeasurementSystem): OSTResult<Unit> =
        delegate.setMeasurementUnits(system.toCore()).toKmpResult()

    override suspend fun readSingleMotionMeasurement(measurementId: String): OSTResult<OSTMotionMeasurement> =
        delegate.readSingleMotionMeasurement(measurementId).toKmpResult { it.toKmp() }
}

// ── Type mappers (Android core → common) ─────────────────────────────────────

private inline fun <A, K> CoreResult<A>.toKmpResult(transform: (A) -> K): OSTResult<K> = when (this) {
    is CoreResult.Success -> OSTResult.Success(transform(data))
    is CoreResult.Error -> OSTResult.Error(cause.toKmpError())
}

private fun <A> CoreResult<A>.toKmpResult(): OSTResult<A> = when (this) {
    is CoreResult.Success -> OSTResult.Success(data)
    is CoreResult.Error -> OSTResult.Error(cause.toKmpError())
}

private fun CoreState.toKmpState(): OSTIdentificationState = when (this) {
    is CoreState.Unidentified -> OSTIdentificationState.Unidentified
    is CoreState.Identified -> OSTIdentificationState.Identified(patientId.toKmpPatientId())
    is CoreState.Lost -> OSTIdentificationState.Lost(cause.toKmpError())
}

// shortcut: OSTError.Type carries a numeric code only for ServerError/OSTMeasurement; others map to null.
private fun CoreError.toKmpCode(): Int? = when (val t = type) {
    is CoreError.Type.ServerError -> t.code
    is CoreError.Type.OSTMeasurement -> t.code
    else -> null
}

// shortcut: common OSTSDKError needs a non-null code; 0 is the sentinel for type-only errors,
// with the original category preserved in `details` (matches the OSTState mapper convention).
private fun CoreError.toKmpError(): OSTError = OSTSDKError(
    code = toKmpCode() ?: 0,
    message = message,
    details = type.name,
)

private fun OSTMeasurementSystem.toCore(): CoreMeasurementSystem = when (this) {
    OSTMeasurementSystem.METRIC -> CoreMeasurementSystem.METRIC
    OSTMeasurementSystem.IMPERIAL -> CoreMeasurementSystem.IMPERIAL
}

private fun CorePatientId.toKmpPatientId(): OSTPatientId = OSTPatientId.fromString(value)

private fun OSTPatientId.toCorePatientId(): CorePatientId = CorePatientId.fromString(value)

private fun CoreUserAttributes.toKmpAttributes(): OSTUserAttributes {
    val mapped = attributes.mapValues { (key, value) ->
        if (key == DOB_KEY && value is Date) value.toKotlinLocalDate() else value
    }
    return OSTUserAttributes(attributes = mapped, customAttributes = customAttributes)
}

// ── User-attribute scope translation (common → Android core) ─────────────────

private fun CoreUserAttributesScope.applyKmpAttributes(attributes: OSTUserAttributes) {
    attributes.firstName?.let { withFirstName(it) }
    attributes.lastName?.let { withLastName(it) }
    attributes.email?.let { withEmail(it) }
    attributes.phone?.let { withPhone(it) }
    attributes.profileImageUrl?.let { withProfileImage(it) }
    attributes.emrId?.let { withEmrId(it) }
    attributes.dateOfBirth?.toJavaUtcDate()?.let { withDateOfBirth(it) }
    attributes.sex?.let { sex ->
        CoreUserAttributes.Sex.entries.firstOrNull { it.description == sex }?.let { withSex(it) }
    }
    attributes.heightCm?.let { withHeightCm(it) }
    attributes.weightKg?.let { withWeightKg(it) }
    attributes.age?.let { withAge(it) }
    attributes.customAttributes.forEach { (key, value) -> withCustomAttribute(key, value) }
}

// ── Date-of-birth conversion ─────────────────────────────────────────────────
// The core SDK's `withDateOfBirth` takes a `java.util.Date` but treats `date_of_birth` as a
// UTC calendar date (yyyy-MM-dd on the wire). Both directions pin UTC midnight via
// kotlinx-datetime so the birthdate never shifts across a timezone boundary. The only
// java type here is `java.util.Date`, required by the core SDK signature.

private const val DOB_KEY = "date_of_birth"

private fun LocalDate.toJavaUtcDate(): Date =
    Date(atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds())

private fun Date.toKotlinLocalDate(): LocalDate =
    Instant.fromEpochMilliseconds(time).toLocalDateTime(TimeZone.UTC).date
