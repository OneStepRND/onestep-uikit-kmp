package co.onestep.kmp.uikit.bridge.android

import co.onestep.android.core.OneStep
import co.onestep.android.core.getOrThrow
import co.onestep.android.core.insights.OSTMotionDataService
import co.onestep.android.core.insights.getInsights
import co.onestep.kmp.uikit.bridge.MotionDataBridge
import co.onestep.kmp.uikit.models.OSTDiscreteColor
import co.onestep.kmp.uikit.models.OSTMotionMeasurement
import co.onestep.kmp.uikit.models.OSTNorm
import co.onestep.kmp.uikit.models.OSTParamName
import co.onestep.kmp.uikit.models.OSTParameterMetadata
import kotlinx.coroutines.runBlocking

class AndroidMotionDataBridge private constructor(
    private val serviceProvider: () -> OSTMotionDataService,
) : MotionDataBridge {

    /**
     * Current-user path: resolve the auth-bound [OSTMotionDataService] lazily from the [OneStep]
     * singleton on first use (the resolution is `suspend`, so it is bridged with `runBlocking` off
     * the caller thread, exactly as before).
     */
    constructor(oneStep: OneStep) : this(
        serviceProvider = {
            runBlocking {
                oneStep.getInsights()
                    .getOrThrow { IllegalStateException("Insights unavailable: ${it.message}") }
                    .getMotionDataService()
                    .getOrThrow { IllegalStateException("MotionDataService unavailable: ${it.message}") }
            }
        },
    )

    /**
     * Clinician-mode path: bound to a patient-scoped [OSTMotionDataService] already resolved inside
     * a `OneStep.withPatient(patientId) { … }` block.
     *
     * Prefer [deferred] where the caller can hand over a provider instead: resolving the service
     * costs two awaited network calls, and this constructor forces the caller to have paid them.
     */
    constructor(service: OSTMotionDataService) : this(serviceProvider = { service })

    private val service: OSTMotionDataService by lazy { serviceProvider() }

    override fun mainParam(motionMeasurement: OSTMotionMeasurement): Pair<OSTParamName, Float>? {
        val entry = service.mainParam(motionMeasurement.toCore()) ?: return null
        val kmpParam = entry.key.toKmp() ?: return null
        return kmpParam to entry.value
    }

    override fun getAllParametersMetadata(): Map<OSTParamName, OSTParameterMetadata> =
        service.getAllParametersMetadata().mapNotNull { (key, value) ->
            key.toKmp()?.let { it to value.toKmp() }
        }.toMap()

    override fun getNormByName(name: OSTParamName?): OSTNorm? {
        val coreName = name?.toCore() ?: return null
        return service.getNormByName(coreName)?.toKmp()
    }

    override fun getParameterMetadata(paramName: OSTParamName): OSTParameterMetadata {
        val coreName = paramName.toCore()
            ?: error("Unknown param name: $paramName")
        return service.getParameterMetadata(coreName).toKmp()
    }

    override fun isWithinNorms(param: OSTParamName, value: Float): Boolean? {
        val coreName = param.toCore() ?: return null
        return service.isWithinNorms(coreName, value)
    }

    override fun discreteScore(motionMeasurement: OSTMotionMeasurement, value: Float): OSTDiscreteColor? =
        service.discreteScore(motionMeasurement.toCore(), value)?.toKmp()

    override fun discreteScore(param: OSTParamName, value: Float): OSTDiscreteColor? {
        val coreName = param.toCore() ?: return null
        return service.discreteScore(coreName, value)?.toKmp()
    }

    companion object {
        /**
         * Clinician-mode path with the resolution **deferred to first use** — the counterpart of the
         * `OneStep` constructor above, for a caller that has a patient scope but no service yet.
         *
         * [serviceProvider] is invoked at most once, on the first bridge method call, and must
         * resolve a *patient-scoped* service — capture the `OSTInsights` obtained inside
         * `OneStep.withPatient(patientId) { … }` rather than re-entering the scope, so the service
         * cannot end up bound to a different patient than the bundle it belongs to.
         *
         * Why this exists: resolving an [OSTMotionDataService] runs
         * `MotionDataServiceImpl.initialize()`, which fires the norms and parameter-metadata
         * requests and awaits both — ~670ms against production from an emulator, measured
         * 2026-09-03. It is also per-resolve, not once per process: each `withPatient` scope vends a
         * fresh service whose `isInitialized` starts false. A caller that resolves eagerly pays that
         * on its own thread, so the recording flow — which never reads this bridge — was paying two
         * HTTP round trips on the main thread before its first screen could compose.
         */
        fun deferred(serviceProvider: () -> OSTMotionDataService): AndroidMotionDataBridge =
            AndroidMotionDataBridge(serviceProvider)
    }
}
