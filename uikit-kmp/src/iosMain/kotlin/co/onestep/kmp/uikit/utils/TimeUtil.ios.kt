package co.onestep.kmp.uikit.utils

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.localTimeZone

internal actual fun Long.toMainParamTitle(showDuration: Boolean): String {
    val date = NSDate.dateWithTimeIntervalSince1970(this / 1000.0)
    val locale = NSLocale.currentLocale
    val tz = NSTimeZone.localTimeZone

    val dateFormatter = NSDateFormatter().apply {
        this.dateFormat = "LLL dd, yyyy"
        this.locale = locale
        this.timeZone = tz
    }
    val datePart = dateFormatter.stringFromDate(date)

    var result = datePart
    if (showDuration) {
        val timeFormatter = NSDateFormatter().apply {
            this.dateFormat = "h:mm a"
            this.locale = locale
            this.timeZone = tz
        }
        val timePart = timeFormatter.stringFromDate(date)
        result = "$result| $timePart"
    }
    return result
}

internal actual fun Int.toDisplayTime(): String {
    val minutes = this / 60
    val seconds = this % 60
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}
