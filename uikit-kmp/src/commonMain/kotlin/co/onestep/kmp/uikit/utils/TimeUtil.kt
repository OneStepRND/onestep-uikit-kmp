package co.onestep.kmp.uikit.utils

import kotlinx.datetime.TimeZone

/**
 * Formats a timestamp (epoch millis) as `LLL dd, yyyy`, optionally suffixed with `| h:mm a`
 * (or `| HH:mm`) when [showDuration] is true, in the device time zone.
 */
internal fun Long.toMainParamTitle(showDuration: Boolean = true): String {
    val dateTime = toLocalDateTimeIn(TimeZone.currentSystemDefault())
    val datePart = dateTime.toDatePart()
    return if (showDuration) "$datePart| ${dateTime.toClockTime()}" else datePart
}

/** Formats a duration in whole seconds as `mm:ss`. */
internal fun Int.toDisplayTime(): String {
    val minutes = this / 60
    val seconds = this % 60
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}
