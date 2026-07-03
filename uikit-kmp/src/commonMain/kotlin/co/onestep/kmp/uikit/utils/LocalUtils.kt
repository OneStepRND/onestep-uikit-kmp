package co.onestep.kmp.uikit.utils

import co.onestep.kmp.uikit.bridge.PreferencesBridge
import co.onestep.kmp.uikit.models.OSTMeasurementSystem
import co.onestep.kmp.uikit.models.OSTNorm
import co.onestep.kmp.uikit.models.OSTParameterMetadata

const val METERS_TO_FEET_RATIO = 3.28084f
const val CM_TO_INCHES_RATIO = 0.39370078f
const val METERS_UNITS = "meters"
const val CM_UNITS = "cm"
const val M_UNITS = "m"
const val METERS_PER_SECOND_UNITS = "m/s"
const val US_COUNTRY_CODE = "US"
const val LIBERIA_COUNTRY_CODE = "LR"
const val MYANMAR_COUNTRY_CODE = "MM"

fun OSTParameterMetadata?.units(preferenceManager: PreferencesBridge): String? =
    if (useImperialSystem(preferenceManager)) {
        this?.imperialUnits
    } else {
        this?.units
    }

fun Float.toImperial(preferenceManager: PreferencesBridge): Float? =
    if (useImperialSystem(preferenceManager)) {
        this * METERS_TO_FEET_RATIO
    } else {
        this
    }

fun Float.toMeters(preferenceManager: PreferencesBridge): Float? =
    if (useImperialSystem(preferenceManager)) {
        this / METERS_TO_FEET_RATIO
    } else {
        this
    }

fun useImperialSystem(preferenceManager: PreferencesBridge): Boolean {
    val measurementSystem = preferenceManager.measurementsSystem
    return when (measurementSystem) {
        OSTMeasurementSystem.METRIC.name -> false
        OSTMeasurementSystem.IMPERIAL.name -> true
        else -> getMeasurementPreference() == MeasurementPref.IMPERIAL_US
    }
}

enum class MeasurementPref { METRIC, IMPERIAL_US }

fun getMeasurementPreference(): MeasurementPref {
    getLocaleUnicodeType("ms")?.let { ms ->
        return when (ms) {
            "ussystem" -> MeasurementPref.IMPERIAL_US
            else -> MeasurementPref.METRIC
        }
    }

    // Fall back to a country heuristic
    return when (getDefaultCountryCode().uppercase()) {
        US_COUNTRY_CODE, LIBERIA_COUNTRY_CODE, MYANMAR_COUNTRY_CODE -> MeasurementPref.IMPERIAL_US
        else -> MeasurementPref.METRIC
    }
}

data class ConversionResult(
    val value: Float,
    val norm: OSTNorm?,
)

/**
 * Converts values given in [OSTNorm] to their corresponding values in imperial units.
 *
 * Supported conversions (from the provided table):
 *  - "m/s"    -> feet/sec
 *  - "cm"     -> inches
 *  - "meters" -> feet
 */
fun OSTNorm?.toImperial(value: Float): ConversionResult =
    when (this?.units) {
        METERS_PER_SECOND_UNITS -> {
            val factor = METERS_TO_FEET_RATIO
            ConversionResult(
                value * factor,
                this.copy(
                    parts =
                        parts?.map { part ->
                            part.copy(
                                start = part.start * factor,
                                end = part.end * factor,
                            )
                        },
                ),
            )
        }

        CM_UNITS -> {
            val factor = CM_TO_INCHES_RATIO
            ConversionResult(
                value * factor,
                this.copy(
                    parts =
                        parts?.map { part ->
                            part.copy(
                                start = part.start * factor,
                                end = part.end * factor,
                            )
                        },
                ),
            )
        }

        METERS_UNITS, M_UNITS -> {
            val factor = METERS_TO_FEET_RATIO
            ConversionResult(
                value * factor,
                this.copy(
                    parts =
                        parts?.map { part ->
                            part.copy(
                                start = part.start * factor,
                                end = part.end * factor,
                            )
                        },
                ),
            )
        }

        else -> ConversionResult(value, this)
    }

fun Float.toImperial(units: String?): Float =
    when (units) {
        METERS_PER_SECOND_UNITS -> this * METERS_TO_FEET_RATIO
        CM_UNITS -> this / CM_TO_INCHES_RATIO
        METERS_UNITS -> this * METERS_TO_FEET_RATIO
        M_UNITS -> this * METERS_TO_FEET_RATIO
        else -> this
    }
