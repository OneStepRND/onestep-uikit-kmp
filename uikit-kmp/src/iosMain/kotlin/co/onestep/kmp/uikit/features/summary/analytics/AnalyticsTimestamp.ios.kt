package co.onestep.kmp.uikit.features.summary.analytics

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.localeWithLocaleIdentifier
import platform.Foundation.timeZoneWithAbbreviation

/**
 * Millisecond-precision UTC instant with a literal `Z`. Pins the `en_US_POSIX` locale so
 * digits are always ASCII and the calendar is proleptic Gregorian, and forces the UTC time
 * zone — matching uikit's `Date.toDeviceTimestamp()` byte-for-byte.
 */
internal actual fun Long.toAnalyticsDeviceTimestamp(): String {
    val date = NSDate.dateWithTimeIntervalSince1970(this / 1000.0)
    val formatter = NSDateFormatter().apply {
        dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        locale = NSLocale.localeWithLocaleIdentifier("en_US_POSIX")
        timeZone = NSTimeZone.timeZoneWithAbbreviation("UTC")!!
    }
    return formatter.stringFromDate(date)
}
