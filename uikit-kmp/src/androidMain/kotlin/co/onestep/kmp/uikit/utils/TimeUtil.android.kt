package co.onestep.kmp.uikit.utils

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal actual fun Long.toMainParamTitle(showDuration: Boolean): String {
    val date = Date(this)
    val tz = TimeZone.getDefault()
    val datePart =
        SimpleDateFormat("LLL dd, yyyy", Locale.getDefault())
            .apply { timeZone = tz }
            .format(date)
    val timePart =
        DateFormat
            .getTimeInstance(DateFormat.SHORT, Locale.getDefault())
            .apply { timeZone = tz }
            .format(date)

    var result = datePart
    if (showDuration) {
        result = "$result| $timePart"
    }
    return result
}

internal actual fun Int.toDisplayTime(): String {
    val minutes = this / 60
    val seconds = this % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
