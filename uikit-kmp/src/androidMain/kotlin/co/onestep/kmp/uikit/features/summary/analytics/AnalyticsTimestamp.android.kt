package co.onestep.kmp.uikit.features.summary.analytics

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Millisecond-precision UTC instant with a literal `Z`, pinned to [Locale.US] so digits are
 * always ASCII `0-9` (never Arabic-Indic on `ar-*` devices) and the calendar is proleptic
 * Gregorian. Matches uikit's `Date.toDeviceTimestamp()` byte-for-byte.
 */
internal actual fun Long.toAnalyticsDeviceTimestamp(): String =
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        .apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(Date(this))
