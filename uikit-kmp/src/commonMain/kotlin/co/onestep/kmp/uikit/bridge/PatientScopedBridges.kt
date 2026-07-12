package co.onestep.kmp.uikit.bridge

/**
 * The patient-bound subset of the bridge surface, resolved once per clinician-mode flow launch.
 *
 * The SDK models patient scope as *different product instances* (an `OSTPatientScope` vends a
 * patient-bound MotionLab / Insights distinct from the auth-bound singleton's). The KMP analogue
 * is *different bridge instances* behind the same interfaces — bridge method signatures never
 * change. This mirrors iOS UIKit's `PatientScopedSDK`, which kept the `OneStepProtocol` seam and
 * swapped in patient-bound products.
 *
 * Lifetime is the flow's composition: the bundle is dropped when the entry composable leaves. No
 * registry, TTL, or release hook. In current-user (patient-app) mode the flow uses the auth-bound
 * singletons instead (see [co.onestep.kmp.uikit.di.UIKitServiceLocator.currentUserBridges]).
 *
 * [OSTSDKBridge] is intentionally NOT part of this bundle: `sdkState`/`events` stay singleton, and
 * the only patient-touching use of the SDK bridge (hallway-length custom metadata) is suppressed in
 * clinician mode rather than patient-scoped (the hallway length belongs to the clinic, not the
 * patient — see the design doc's behavior matrix).
 */
class PatientScopedBridges(
    val recorderBridge: RecorderBridge,
    val insightsBridge: InsightsBridge,
    val motionDataBridge: MotionDataBridge,
)

/**
 * Builds a [PatientScopedBridges] bundle for an explicit patient id. Implemented per platform and
 * registered once at `configure` time via
 * [co.onestep.kmp.uikit.di.UIKitServiceLocator.configure]'s `patientScopedBridgesFactory`.
 *
 * The `patientId` is a plain `String` — no platform scope type ever crosses the KMP boundary; each
 * platform builds its own SDK scope internally (Android `OneStep.withPatient(OSTPatientId)`, iOS
 * `OneStep.withPatient(_:)`). Because the entry composable is a plain function call in the host's
 * process, no token/registry indirection is needed.
 */
interface PatientScopedBridgesFactory {
    /**
     * Called once per clinician-mode flow launch. Implementations MAY cache per `patientId`, but a
     * fresh call must never return a bundle bound to a *different* patient (stale-scope guard).
     */
    fun create(patientId: String): PatientScopedBridges
}

/**
 * Resolves the bridge bundle for one flow launch. Pure and side-effect-free so the fail-fast and
 * delegation behavior is unit-testable without Compose or a full service-locator configuration.
 *
 * - `patientId == null` → current-user mode: the auth-bound [currentUserBridges].
 * - `patientId != null` → clinician mode: delegate to [patientScopedBridgesFactory]. When no factory
 *   was configured this **throws** rather than falling back to [currentUserBridges] — a silent
 *   fallback would attribute the patient's recording to the wrong (auth-bound) identity.
 */
internal fun resolveSessionBridges(
    patientId: String?,
    currentUserBridges: () -> PatientScopedBridges,
    patientScopedBridgesFactory: PatientScopedBridgesFactory?,
): PatientScopedBridges =
    if (patientId == null) {
        currentUserBridges()
    } else {
        patientScopedBridgesFactory?.create(patientId)
            ?: error(
                "OSTRecordingFlow was launched with a patientId but no PatientScopedBridgesFactory " +
                    "was configured. Register one via UIKitServiceLocator.configure(" +
                    "patientScopedBridgesFactory = ...) (Android: AndroidPatientScopedBridgesFactory).",
            )
    }
