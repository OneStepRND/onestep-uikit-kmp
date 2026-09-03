package co.onestep.kmp.uikit.bridge.android

import co.onestep.android.core.OSTParamName as CoreParamName
import co.onestep.android.core.insights.OSTDiscreteColor as CoreDiscreteColor
import co.onestep.android.core.insights.OSTMotionDataService
import co.onestep.android.core.insights.OSTNorm as CoreNorm
import co.onestep.android.core.insights.OSTParameterMetadata as CoreParameterMetadata
import co.onestep.android.core.motionLab.OSTMotionMeasurement as CoreMotionMeasurement
import co.onestep.kmp.uikit.models.OSTParamName
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Pins the property the recording flow's start-up time depends on: a bridge built with
 * [AndroidMotionDataBridge.deferred] does **no** work until one of its methods is called.
 *
 * Resolving an `OSTMotionDataService` awaits the norms and parameter-metadata requests
 * (`MotionDataServiceImpl.initialize()`), so an eager resolve puts two HTTP round trips on the
 * caller's thread. `AndroidPatientScopedBridgesFactory.create()` is called from composition on the
 * main thread, and `OSTRecordingFlow` reads only the recorder bridge — so an eager resolve there
 * cost the recording flow ~670ms of network it never uses (measured 2026-09-03). If this test goes
 * red, that stall is back.
 */
class AndroidMotionDataBridgeLazinessTest {

    @Test
    fun deferredBridgeDoesNotResolveItsServiceUntilAMethodIsCalled() {
        var resolveCount = 0
        val bridge = AndroidMotionDataBridge.deferred {
            resolveCount++
            FakeMotionDataService()
        }

        // Construction alone must not touch the service — this is the whole point.
        assertEquals(0, resolveCount, "constructing the bridge resolved the service")

        bridge.getNormByName(OSTParamName.WALKING_WALK_SCORE)

        assertEquals(1, resolveCount, "first use did not resolve the service exactly once")
    }

    @Test
    fun deferredBridgeResolvesItsServiceAtMostOnceAcrossManyCalls() {
        var resolveCount = 0
        val bridge = AndroidMotionDataBridge.deferred {
            resolveCount++
            FakeMotionDataService()
        }

        bridge.getNormByName(OSTParamName.WALKING_WALK_SCORE)
        bridge.getAllParametersMetadata()
        bridge.isWithinNorms(OSTParamName.WALKING_WALK_SCORE, 1f)

        // `by lazy` caches; a provider called per method would re-run initialize() — and so re-run
        // its two network requests — on every read.
        assertEquals(1, resolveCount)
    }

    private class FakeMotionDataService : OSTMotionDataService {
        override fun mainParam(motionMeasurement: CoreMotionMeasurement): Map.Entry<CoreParamName, Float>? = null
        override fun getAllParametersMetadata(): Map<CoreParamName, CoreParameterMetadata> = emptyMap()
        override fun getNormByName(name: CoreParamName?): CoreNorm? = null
        override fun getParameterMetadata(paramName: CoreParamName): CoreParameterMetadata =
            throw UnsupportedOperationException("not needed for these tests")
        override fun isWithinNorms(param: CoreParamName, value: Float): Boolean? = null
        override fun discreteScore(motionMeasurement: CoreMotionMeasurement, value: Float): CoreDiscreteColor? = null
        override fun discreteScore(param: CoreParamName, value: Float): CoreDiscreteColor? = null
    }
}
