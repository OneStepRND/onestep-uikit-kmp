package co.onestep.kmp.uikit.features.summary.analytics

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * Formats an epoch-millisecond instant as the analytics `activity_date` value: a
 * millisecond-precision UTC timestamp with a literal `Z`, e.g. `2026-05-20T11:22:09.815Z`.
 *
 * Built on `kotlinx-datetime` so the output is platform-independent and always ASCII (`0-9`),
 * matching uikit's `Date.toDeviceTimestamp()` (`yyyy-MM-dd'T'HH:mm:ss.SSS'Z'`, UTC) byte-for-byte.
 */
internal fun Long.toAnalyticsDeviceTimestamp(): String {
    val dt = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC)
    val year = dt.year.toString().padStart(4, '0')
    val month = (dt.month.ordinal + 1).toString().padStart(2, '0')
    val day = dt.day.toString().padStart(2, '0')
    val hour = dt.hour.toString().padStart(2, '0')
    val minute = dt.minute.toString().padStart(2, '0')
    val second = dt.second.toString().padStart(2, '0')
    val millis = (dt.nanosecond / 1_000_000).toString().padStart(3, '0')
    return "$year-$month-${day}T$hour:$minute:$second.${millis}Z"
}
