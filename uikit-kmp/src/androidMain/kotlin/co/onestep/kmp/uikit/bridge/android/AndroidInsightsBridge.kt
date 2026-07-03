package co.onestep.kmp.uikit.bridge.android

import co.onestep.android.core.OSTResult
import co.onestep.android.core.OneStep
import co.onestep.android.core.getOr
import co.onestep.android.core.insights.getInsights
import co.onestep.kmp.uikit.bridge.InsightsBridge
import co.onestep.kmp.uikit.models.OSTInsights
import co.onestep.kmp.uikit.models.OSTMotionMeasurement

class AndroidInsightsBridge(private val oneStep: OneStep) : InsightsBridge {

    override suspend fun getInsights(measurement: OSTMotionMeasurement): OSTInsights? =
        getInsightsByUuid(measurement.id)

    override suspend fun getInsightsByUuid(uuid: String): OSTInsights? {
        val insights = oneStep.getInsights().getOr(null) ?: return null
        return when (val result = insights.getMeasurementInsights(uuid)) {
            is OSTResult.Success -> result.data.toKmp()
            is OSTResult.Error -> null
        }
    }
}
