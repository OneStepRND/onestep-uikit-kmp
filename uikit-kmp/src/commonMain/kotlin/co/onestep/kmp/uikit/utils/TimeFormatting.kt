package co.onestep.kmp.uikit.utils

import co.onestep.kmp.uikit.di.UIKitServiceLocator
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.month_abbrev_apr
import co.onestep.kmp.uikit_kmp.generated.resources.month_abbrev_aug
import co.onestep.kmp.uikit_kmp.generated.resources.month_abbrev_dec
import co.onestep.kmp.uikit_kmp.generated.resources.month_abbrev_feb
import co.onestep.kmp.uikit_kmp.generated.resources.month_abbrev_jan
import co.onestep.kmp.uikit_kmp.generated.resources.month_abbrev_jul
import co.onestep.kmp.uikit_kmp.generated.resources.month_abbrev_jun
import co.onestep.kmp.uikit_kmp.generated.resources.month_abbrev_mar
import co.onestep.kmp.uikit_kmp.generated.resources.month_abbrev_may
import co.onestep.kmp.uikit_kmp.generated.resources.month_abbrev_nov
import co.onestep.kmp.uikit_kmp.generated.resources.month_abbrev_oct
import co.onestep.kmp.uikit_kmp.generated.resources.month_abbrev_sep
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import kotlin.time.Instant

/**
 * Shared, platform-independent time formatting built on `kotlinx-datetime`.
 *
 * All date math and string formatting lives in commonMain — the single remaining platform hook
 * is [uses24HourClock], which reads the OS 12h/24h preference (unavailable in kotlinx-datetime).
 * Month names are localized via compose resources (`month_abbrev_*`), so they follow the same
 * translation pipeline as every other string; unshipped locales fall back to English.
 */

/** Reads the device's 12h/24h clock preference. Only platform hook in the time stack. */
internal expect fun uses24HourClock(): Boolean

/** Month-abbreviation string resources indexed by [kotlinx.datetime.Month.ordinal] (Jan=0). */
private val MONTH_ABBREVIATION_RES: Array<StringResource> = arrayOf(
    Res.string.month_abbrev_jan, Res.string.month_abbrev_feb, Res.string.month_abbrev_mar,
    Res.string.month_abbrev_apr, Res.string.month_abbrev_may, Res.string.month_abbrev_jun,
    Res.string.month_abbrev_jul, Res.string.month_abbrev_aug, Res.string.month_abbrev_sep,
    Res.string.month_abbrev_oct, Res.string.month_abbrev_nov, Res.string.month_abbrev_dec,
)

/** Converts epoch milliseconds to a [LocalDateTime] in [zone]. */
internal fun Long.toLocalDateTimeIn(zone: TimeZone): LocalDateTime =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(zone)

/** Localized three-letter month abbreviation (e.g. `Jul`), resolved via compose resources. */
internal val LocalDateTime.monthAbbreviation: String
    get() = UIKitServiceLocator.resourceProvider.getString(MONTH_ABBREVIATION_RES[month.ordinal])

/** Date portion formatted as `MMM dd, yyyy` (e.g. `Jul 19, 2025`). */
internal fun LocalDateTime.toDatePart(): String =
    "$monthAbbreviation ${day.toString().padStart(2, '0')}, $year"

/** Time portion as `h:mm a` (12h) or `HH:mm` (24h), per the device [uses24HourClock] preference. */
internal fun LocalDateTime.toClockTime(): String =
    if (uses24HourClock()) {
        "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
    } else {
        val h = hour % 12
        val hour12 = if (h == 0) 12 else h
        val marker = if (hour < 12) "AM" else "PM"
        "$hour12:${minute.toString().padStart(2, '0')} $marker"
    }
