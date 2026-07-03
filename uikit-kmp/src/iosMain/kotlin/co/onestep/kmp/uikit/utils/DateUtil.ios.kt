package co.onestep.kmp.uikit.utils

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.localTimeZone

actual fun Long.toLocalizedTimeString(): String {
    val date = NSDate.dateWithTimeIntervalSince1970(this / 1000.0)
    val locale = NSLocale.currentLocale
    val timeZone = NSTimeZone.localTimeZone

    val dateFormatter = NSDateFormatter().apply {
        this.dateFormat = "MMM dd, yyyy"
        this.locale = locale
        this.timeZone = timeZone
    }

    val timeFormatter = NSDateFormatter().apply {
        this.dateFormat = "h:mm a"
        this.locale = locale
        this.timeZone = timeZone
    }

    val datePart = dateFormatter.stringFromDate(date)
    val timePart = timeFormatter.stringFromDate(date)
    return "$datePart | $timePart"
}

actual fun Long.toDeviceTimeString(): String {
    val date = NSDate.dateWithTimeIntervalSince1970(this / 1000.0)

    val formatter = NSDateFormatter().apply {
        this.dateFormat = "h:mm a"
        this.locale = NSLocale.currentLocale
        this.timeZone = NSTimeZone.localTimeZone
    }

    return formatter.stringFromDate(date)
}
