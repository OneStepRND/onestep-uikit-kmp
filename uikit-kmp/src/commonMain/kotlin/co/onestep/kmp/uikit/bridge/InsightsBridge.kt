package co.onestep.kmp.uikit.bridge

import co.onestep.kmp.uikit.models.OSTInsights
import co.onestep.kmp.uikit.models.OSTMotionMeasurement

/**
 * Bridge interface abstracting OSTInsights access.
 * Provides insight data for measurements.
 */
interface InsightsBridge {
    suspend fun getInsights(measurement: OSTMotionMeasurement): OSTInsights?
    suspend fun getInsightsByUuid(uuid: String): OSTInsights?
}
