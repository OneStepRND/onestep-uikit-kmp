package co.onestep.kmp.uikit.utils

import kotlinx.datetime.TimeZone

/**
 * Platform-independent date/time formatting built on `kotlinx-datetime`.
 *
 * Clock format (12h/24h) follows the device preference via [uses24HourClock]; month names are
 * fixed English abbreviations. See [TimeFormatting] for the shared building blocks.
 */

/**
 * Formats a timestamp (epoch millis) as `MMM dd, yyyy | h:mm a` or `MMM dd, yyyy | HH:mm`
 * depending on the device clock preference, in the device time zone.
 */
fun Long.toLocalizedTimeString(): String {
    val dateTime = toLocalDateTimeIn(TimeZone.currentSystemDefault())
    return "${dateTime.toDatePart()} | ${dateTime.toClockTime()}"
}

/**
 * Formats a timestamp (epoch millis) as `h:mm a` or `HH:mm` depending on the device clock
 * preference, in the device time zone.
 */
fun Long.toDeviceTimeString(): String =
    toLocalDateTimeIn(TimeZone.currentSystemDefault()).toClockTime()
