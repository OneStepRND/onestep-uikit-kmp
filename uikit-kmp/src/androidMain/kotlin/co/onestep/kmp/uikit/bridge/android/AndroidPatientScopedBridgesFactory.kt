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
            bridges = PatientScopedBridges(
                recorderBridge = AndroidRecorderBridge(motionLab = getMotionLab()),
                insightsBridge = AndroidInsightsBridge(insights = insights),
                // Deferred, NOT resolved here: `getMotionDataService()` awaits the norms and
                // parameter-metadata requests, so resolving it in `create` put two HTTP round trips
                // on the caller's thread — and `create` is called from composition, on the main
                // thread. `OSTRecordingFlow` reads only `recorderBridge`, so it was paying ~670ms
                // of network it never uses before its first screen could appear (measured on a
                // Pixel 10 Pro emulator against production, 2026-09-03: 667ms of a 716ms frame).
                // The summary flow, which does read this bridge, resolves its own bundle and now
                // pays the cost where it is actually needed.
                //
                // `insights` is the patient-scoped product resolved above and is captured rather
                // than re-derived, so the deferred service is bound to this bundle's patient even
                // though the scope block has long since returned. `runBlocking` survives because
                // `MotionDataBridge`'s methods are not suspend; it now runs on whichever thread
                // first reads the bridge instead of on every caller of `create`.
                motionDataBridge = AndroidMotionDataBridge.deferred {
                    runBlocking {
                        insights.getMotionDataService()
                            .getOrThrow { IllegalStateException("MotionDataService unavailable: ${it.message}") }
                    }
                },
            )
        }
        return bridges
    }
}
