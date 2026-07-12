package co.onestep.kmp.uikit.bridge.android

import co.onestep.android.core.OSTPatientId
import co.onestep.android.core.OneStep
import co.onestep.android.core.getOrThrow
import co.onestep.android.core.insights.getInsights
import co.onestep.android.core.motionLab.getMotionLab
import co.onestep.kmp.uikit.bridge.PatientScopedBridges
import co.onestep.kmp.uikit.bridge.PatientScopedBridgesFactory
import kotlinx.coroutines.runBlocking

/**
 * Android clinician-mode bridge factory. Ships out of the box in `androidMain` because it depends
 * only on the shipped, public SDK surface:
 * - [OSTPatientId.fromString] (public factory over a `String`),
 * - [OneStep.withPatient] (public), and
 * - the `OSTPatientScope.getMotionLab()` / `getInsights()` extensions (public), which vend
 *   patient-bound products distinct from the auth-bound singleton's.
 *
 * The SDK's identification state is untouched — no SDK-wide identity is written. Because the
 * MotionLab is resolved *inside* the `withPatient { }` block, its recorder is already owner-bound to
 * the patient, so recordings are attributed correctly without any per-call pinning.
 *
 * Register once at startup via `UIKitServiceLocator.configure(..., patientScopedBridgesFactory =
 * AndroidPatientScopedBridgesFactory())`.
 */
class AndroidPatientScopedBridgesFactory : PatientScopedBridgesFactory {

    override fun create(patientId: String): PatientScopedBridges {
        // withPatient returns Unit (the block is `OSTPatientScope.() -> Unit`), so capture the
        // bundle built inside the scope. The scope caches its products for its lifetime; here we
        // only need them long enough to construct the resolved-product bridges.
        lateinit var bridges: PatientScopedBridges
        OneStep.withPatient(OSTPatientId.fromString(patientId)) {
            val insights = getInsights()
            val motionDataService = runBlocking {
                insights.getMotionDataService()
                    .getOrThrow { IllegalStateException("MotionDataService unavailable: ${it.message}") }
            }
            bridges = PatientScopedBridges(
                recorderBridge = AndroidRecorderBridge(motionLab = getMotionLab()),
                insightsBridge = AndroidInsightsBridge(insights = insights),
                motionDataBridge = AndroidMotionDataBridge(service = motionDataService),
            )
        }
        return bridges
    }
}
