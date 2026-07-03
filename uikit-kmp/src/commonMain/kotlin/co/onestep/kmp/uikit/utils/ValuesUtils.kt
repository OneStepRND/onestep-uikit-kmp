package co.onestep.kmp.uikit.utils

import co.onestep.kmp.uikit.bridge.PreferencesBridge
import co.onestep.kmp.uikit.models.OSTParameterMetadata

fun Float.roundValue(
    metadata: OSTParameterMetadata,
    preferenceManager: PreferencesBridge,
): Float {
    val expendRounding = (useImperialSystem(preferenceManager) && metadata.units == CM_UNITS)
    val roundValue =
        when {
            expendRounding -> metadata.roundDigits?.plus(1)
            else -> metadata.roundDigits
        }
    return when (roundValue) {
        0f -> toInt().toFloat()
        else -> round(roundValue?.toInt() ?: 0)
    }
}

fun Float.cleanString(): String =
    if (this % 1f == 0f) {
        toInt().toString()
    } else {
        toString()
    }

internal fun Float.round(decimals: Int): Float {
    var multiplier = 1f
    repeat(decimals) { multiplier *= 10 }
    return kotlin.math.round(this * multiplier) / multiplier
}

fun Float.toText(): String =
    if ((this % 1.0).toFloat() == 0f) {
        toInt().toString()
    } else {
        this.toString()
    }

fun Float?.toStringOrDefault(defaultValue: String): String = this?.toInt()?.toString() ?: defaultValue

fun Float?.toRoundedImperialString(
    defaultValue: String,
    preferenceManager: PreferencesBridge,
    metadata: OSTParameterMetadata,
): String {
    val raw = this ?: return defaultValue
    val maybeConverted =
        if (useImperialSystem(preferenceManager)) {
            raw.toImperial(metadata.units)
        } else {
            raw
        }
    return maybeConverted.roundValue(metadata, preferenceManager).cleanString()
}
