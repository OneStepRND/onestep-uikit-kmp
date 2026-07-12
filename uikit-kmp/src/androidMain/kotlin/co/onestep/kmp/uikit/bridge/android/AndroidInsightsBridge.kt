package co.onestep.kmp.uikit.bridge.android

import co.onestep.android.core.OSTResult
import co.onestep.android.core.OneStep
import co.onestep.android.core.getOr
import co.onestep.android.core.insights.OSTInsights as CoreInsights
import co.onestep.android.core.insights.getInsights
import co.onestep.kmp.uikit.bridge.InsightsBridge
import co.onestep.kmp.uikit.models.OSTInsights
import co.onestep.kmp.uikit.models.OSTMotionMeasurement

class AndroidInsightsBridge private constructor(
    private val insightsProvider: () -> CoreInsights?,
) : InsightsBridge {

    /** Current-user path: resolve the auth-bound Insights lazily from the [OneStep] singleton. */
    constructor(oneStep: OneStep) : this(insightsProvider = { oneStep.getInsights().getOr(null) })

    /**
     * Clinician-mode path: bound to a patient-scoped Insights already resolved inside a
     * `OneStep.withPatient(patientId) { … }` block.
     */
    constructor(insights: CoreInsights) : this(insightsProvider = { insights })

    override suspend fun getInsights(measurement: OSTMotionMeasurement): OSTInsights? =
        getInsightsByUuid(measurement.id)

    override suspend fun getInsightsByUuid(uuid: String): OSTInsights? {
        val insights = insightsProvider() ?: return null
        return when (val result = insights.getMeasurementInsights(uuid)) {
            is OSTResult.Success -> result.data.toKmp()
            is OSTResult.Error -> null
        }
    }
}
