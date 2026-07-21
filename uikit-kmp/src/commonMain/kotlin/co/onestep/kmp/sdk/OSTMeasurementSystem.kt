package co.onestep.kmp.sdk

/**
 * Display unit system for a patient's measurements.
 *
 * Mirror of the native `co.onestep.android.core.motionLab.OSTMeasurementSystem`, applied via
 * [OSTMotionLab.setMeasurementUnits]. Only the two explicit systems are surfaced; the SDK's
 * device-default behaviour is the implicit state before a system is set.
 */
enum class OSTMeasurementSystem {
    METRIC,
    IMPERIAL,
}
