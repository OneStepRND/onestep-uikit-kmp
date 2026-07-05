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

/**
 * Formats an Int? count-of-seconds as "M:SS" or "MM:SS".
 * @param padMinutes true → zero-pad the minutes field to 2 digits ("01:30"); false → no padding ("1:30").
 */
fun Int?.toFormattedDuration(padMinutes: Boolean = true): String {
    val m = (this ?: 0) / 60
    val s = (this ?: 0) % 60
    val minStr = if (padMinutes) m.toString().padStart(2, '0') else m.toString()
    return "$minStr:${s.toString().padStart(2, '0')}"
}

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
