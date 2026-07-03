package co.onestep.kmp.uikit.utils

import android.content.Context
import android.text.format.DateFormat.is24HourFormat
import co.onestep.kmp.uikit.di.UIKitServiceLocator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// --- Legacy Context-based functions (used by VMs still in androidMain) ---

internal fun Date.toLocalizedTimeString(
    context: Context,
    timeZone: TimeZone? = null,
): String {
    val tz = timeZone ?: TimeZone.getDefault()
    val locale = Locale.getDefault()

    val datePart =
        SimpleDateFormat("MMM dd, yyyy", locale)
            .apply { this.timeZone = tz }
            .format(this)

    val timePattern = if (is24HourFormat(context)) "HH:mm" else "h:mm a"
    val timePart =
        SimpleDateFormat(timePattern, locale)
            .apply { this.timeZone = tz }
            .format(this)

    return "$datePart | $timePart"
}

fun Date.toDeviceTimeString(
    context: Context,
    timeZone: TimeZone? = null,
    locale: Locale = Locale.getDefault(),
): String {
    val pattern = if (is24HourFormat(context)) "HH:mm" else "h:mm a"

    return SimpleDateFormat(pattern, locale)
        .apply {
            this.timeZone = timeZone ?: TimeZone.getDefault()
        }.format(this)
}

// --- KMP expect/actual implementations (Context-free, use ServiceLocator) ---

actual fun Long.toLocalizedTimeString(): String {
    val context = (UIKitServiceLocator.resourceProvider as ResourceProvider).context
    return Date(this).toLocalizedTimeString(context)
}

actual fun Long.toDeviceTimeString(): String {
    val context = (UIKitServiceLocator.resourceProvider as ResourceProvider).context
    return Date(this).toDeviceTimeString(context)
}
