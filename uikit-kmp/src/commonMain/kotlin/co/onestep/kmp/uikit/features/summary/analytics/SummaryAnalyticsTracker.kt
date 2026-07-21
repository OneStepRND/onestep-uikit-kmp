package co.onestep.kmp.uikit.features.summary.analytics

import co.onestep.kmp.uikit.OSTUIKitAnalyticsHandler
import co.onestep.kmp.uikit.features.recordFlow.analytics.AnalyticsProps
import co.onestep.kmp.uikit.features.recordFlow.analytics.analyticsName
import co.onestep.kmp.uikit.features.summary.analytics.SummaryAnalyticsEvents.ScreenNames
import co.onestep.kmp.uikit.models.OSTActivityType
import co.onestep.kmp.sdk.OSTEvent
import co.onestep.kmp.uikit.models.OSTMotionMeasurement
import co.onestep.kmp.uikit.models.OSTParamName
import kotlin.math.roundToInt

/**
 * Central analytics tracker for the summary flow, ported from the Android `uikit`
 * `SummaryAnalyticsTracker`. Event names, property keys/values, unit conversions and
 * per-metric omission rules match uikit exactly so both SDKs emit an identical contract.
 *
 * KMP note: [OSTMotionMeasurement.params] is keyed by the raw `columnName` String, so
 * metrics are read via [OSTMotionMeasurement.getParam] (uikit read a `Map<OSTParamName,
 * Float>` directly). [OSTEvent.properties] is `Map<String, String>`, so all values are
 * stringified. `activity_date` uses [toAnalyticsDeviceTimestamp] to reproduce uikit's
 * `Date.toDeviceTimestamp()` ISO millis-UTC string.
 *
 * @param analytics The analytics handler that emits events.
 */
internal class SummaryAnalyticsTracker(
    private val analytics: OSTUIKitAnalyticsHandler,
) {
    fun trackEditHallway(
        activity: OSTActivityType,
        original: Int,
        updated: Int,
    ) {
        analytics.onEvent(
            OSTEvent(
                name = SummaryAnalyticsEvents.SUMMARY_EDIT_HALLWAY,
                properties = mapOf(
                    AnalyticsProps.ACTIVITY_NAME to activity.analyticsName,
                    AnalyticsProps.ORIGINAL_HALLWAY_VALUE to original.toString(),
                    AnalyticsProps.UPDATED_HALLWAY_VALUE to updated.toString(),
                ),
            ),
        )
    }

    /**
     * `screen: activity_summary`. [appSection] distinguishes arriving from the end of the
     * recording flow ("activities") vs the host history/care-log ("history"). Every metric
     * is read from the analyzed [measurement] and omitted when its param is absent.
     *
     * `trend`, `fall_risk_indicators` and `asymmetry_score` are intentionally NOT sent
     * (same rationale as uikit).
     */
    fun trackActivitySummaryScreen(
        measurement: OSTMotionMeasurement,
        appSection: String,
    ) {
        track(SummaryAnalyticsEvents.SCREEN_ACTIVITY_SUMMARY, measurement) {
            put(AnalyticsProps.APP_SECTION, appSection)
            measurement.intOf(OSTParamName.WALKING_WALK_SCORE)?.let { put(AnalyticsProps.SCORE, it.toString()) }
            measurement.metadata.steps?.let { put(AnalyticsProps.STEPS, it.toString()) }
            measurement.metadata.seconds?.let { put(AnalyticsProps.SECONDS, it.toString()) }
            measurement.ratioOf(OSTParamName.WALKING_DOUBLE_SUPPORT)?.let { put(AnalyticsProps.DOUBLE_SUPPORT, it.toString()) }
            measurement.ratioOf(OSTParamName.WALKING_STEP_LENGTH_ASYMMETRY)?.let { put(AnalyticsProps.STEP_LENGTH_ASYMMETRY, it.toString()) }
            measurement.cmOf(OSTParamName.WALKING_STRIDE_LENGTH)?.let { put(AnalyticsProps.STRIDE_LENGTH, it.toString()) }
            measurement.intOf(OSTParamName.WALKING_CADENCE)?.let { put(AnalyticsProps.CADENCE, it.toString()) }
            measurement.intOf(OSTParamName.WALKING_CONSISTENCY)?.let { put(AnalyticsProps.CONSISTENCY, it.toString()) }
        }
    }

    /**
     * `screen: gait_data`. All gait metrics come from the analyzed [measurement]; length
     * params (stride/step length, base width) are converted from the stored metres to the
     * spec's centimetres, percentage/ratio params (0–1) and velocity (m/s) pass through.
     */
    fun trackGaitDataScreen(measurement: OSTMotionMeasurement) {
        track(SummaryAnalyticsEvents.SCREEN_GAIT_DATA, measurement) {
            measurement.intOf(OSTParamName.WALKING_WALK_SCORE)?.let { put(AnalyticsProps.SCORE, it.toString()) }
            measurement.metadata.steps?.let { put(AnalyticsProps.STEPS, it.toString()) }
            measurement.metadata.seconds?.let { put(AnalyticsProps.SECONDS, it.toString()) }
            measurement.intOf(OSTParamName.WALKING_CADENCE)?.let { put(AnalyticsProps.CADENCE, it.toString()) }
            measurement.ratioOf(OSTParamName.WALKING_CADENCE_VARIABILITY)?.let { put(AnalyticsProps.CADENCE_VARIABILITY, it.toString()) }
            measurement.floatOf(OSTParamName.WALKING_VELOCITY)?.let { put(AnalyticsProps.VELOCITY, it.toString()) }
            measurement.ratioOf(OSTParamName.WALKING_VELOCITY_VARIABILITY)?.let { put(AnalyticsProps.VELOCITY_VARIABILITY, it.toString()) }
            measurement.intOf(OSTParamName.WALKING_CONSISTENCY)?.let { put(AnalyticsProps.CONSISTENCY, it.toString()) }
            measurement.cmOf(OSTParamName.WALKING_STRIDE_LENGTH)?.let { put(AnalyticsProps.STRIDE_LENGTH, it.toString()) }
            measurement.cmOf(OSTParamName.WALKING_STEP_LENGTH_RIGHT)?.let { put(AnalyticsProps.STEP_LENGTH_RIGHT, it.toString()) }
            measurement.cmOf(OSTParamName.WALKING_STEP_LENGTH_LEFT)?.let { put(AnalyticsProps.STEP_LENGTH_LEFT, it.toString()) }
            measurement.ratioOf(OSTParamName.WALKING_STEP_LENGTH_ASYMMETRY)?.let { put(AnalyticsProps.STEP_LENGTH_ASYMMETRY, it.toString()) }
            measurement.cmOf(OSTParamName.WALKING_BASE_WIDTH)?.let { put(AnalyticsProps.BASE_WIDTH, it.toString()) }
            measurement.ratioOf(OSTParamName.WALKING_DOUBLE_SUPPORT)?.let { put(AnalyticsProps.DOUBLE_SUPPORT, it.toString()) }
            measurement.ratioOf(OSTParamName.WALKING_DOUBLE_SUPPORT_ASYMMETRY)?.let { put(AnalyticsProps.DOUBLE_SUPPORT_ASYMMETRY, it.toString()) }
            measurement.ratioOf(OSTParamName.WALKING_SINGLE_SUPPORT_LEFT)?.let { put(AnalyticsProps.SINGLE_SUPPORT_LEFT, it.toString()) }
            measurement.ratioOf(OSTParamName.WALKING_SINGLE_SUPPORT_RIGHT)?.let { put(AnalyticsProps.SINGLE_SUPPORT_RIGHT, it.toString()) }
            measurement.ratioOf(OSTParamName.WALKING_STANCE_LEFT)?.let { put(AnalyticsProps.STANCE_LEFT, it.toString()) }
            measurement.ratioOf(OSTParamName.WALKING_STANCE_RIGHT)?.let { put(AnalyticsProps.STANCE_RIGHT, it.toString()) }
            measurement.ratioOf(OSTParamName.WALKING_STANCE_ASYMMETRY)?.let { put(AnalyticsProps.STANCE_ASYMMETRY, it.toString()) }
            measurement.intOf(OSTParamName.WALKING_HIP_RANGE)?.let { put(AnalyticsProps.HIP_RANGE, it.toString()) }
        }
    }

    /**
     * `Clicked: activity_summary_tab` — [tabName] is "highlights" or "gait_data" (the spec
     * key is `tab_name`, not `tab`).
     */
    fun trackActivitySummaryTabClicked(measurement: OSTMotionMeasurement, tabName: String) {
        track(SummaryAnalyticsEvents.CLICKED_ACTIVITY_SUMMARY_TAB, measurement) {
            put(AnalyticsProps.TAB_NAME, tabName)
        }
    }

    /**
     * `Clicked: discard_measurement` — emitted from the trash icon on the summary toolbar.
     * `reps` is STS-only; `score`/`steps` are walk-only — each is omitted when absent.
     */
    fun trackDiscardMeasurementClicked(measurement: OSTMotionMeasurement) {
        track(SummaryAnalyticsEvents.CLICKED_DISCARD_MEASUREMENT, measurement) {
            measurement.intOf(OSTParamName.WALKING_WALK_SCORE)?.let { put(AnalyticsProps.SCORE, it.toString()) }
            measurement.metadata.steps?.let { put(AnalyticsProps.STEPS, it.toString()) }
            measurement.intOf(OSTParamName.STS_REPETITION_COUNT)?.let { put(AnalyticsProps.REPS, it.toString()) }
            measurement.metadata.seconds?.let { put(AnalyticsProps.SECONDS, it.toString()) }
        }
    }

    /**
     * `screen: measurement_deleted`. The SDK has no dedicated full-screen "Measurement
     * deleted" view; this fires at the moment the user confirms deletion, the SDK-owned
     * point closest to the spec screen. `screen_name` is reported as "measurement".
     */
    fun trackMeasurementDeleted(measurement: OSTMotionMeasurement) {
        analytics.onEvent(
            OSTEvent(
                name = SummaryAnalyticsEvents.SCREEN_MEASUREMENT_DELETED,
                properties = buildMap {
                    put(AnalyticsProps.SCREEN_NAME, ScreenNames.MEASUREMENT)
                    put(AnalyticsProps.ACTIVITY_NAME, measurement.type.analyticsName)
                    put(AnalyticsProps.PERCEPTION_UUID, measurement.id)
                    put(AnalyticsProps.ACTIVITY_DATE, measurement.timestamp.toAnalyticsDeviceTimestamp())
                },
            ),
        )
    }

    // --- helpers ---

    private fun OSTMotionMeasurement.intOf(param: OSTParamName): Int? = getParam(param)?.toInt()

    private fun OSTMotionMeasurement.floatOf(param: OSTParamName): Float? = getParam(param)

    /**
     * Length params are stored by the analysis pipeline in centimetres. The spec reports
     * them as whole cm, so just round.
     */
    private fun OSTMotionMeasurement.cmOf(param: OSTParamName): Int? = getParam(param)?.roundToInt()

    /**
     * Ratio/percentage params are stored as a 0–100 percentage, but the spec reports them
     * as a 0–1 ratio.
     */
    private fun OSTMotionMeasurement.ratioOf(param: OSTParamName): Float? = getParam(param)?.let { it / 100f }

    private fun track(
        event: String,
        measurement: OSTMotionMeasurement,
        extra: (MutableMap<String, String>.() -> Unit)? = null,
    ) {
        val props = buildMap {
            put(AnalyticsProps.SCREEN_NAME, ScreenNames.ACTIVITY_SUMMARY)
            put(AnalyticsProps.ACTIVITY_NAME, measurement.type.analyticsName)
            put(AnalyticsProps.PERCEPTION_UUID, measurement.id)
            put(AnalyticsProps.ACTIVITY_DATE, measurement.timestamp.toAnalyticsDeviceTimestamp())
            if (extra != null) extra()
        }
        analytics.onEvent(OSTEvent(name = event, properties = props))
    }
}
