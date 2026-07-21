package co.onestep.kmp.uikit.utils

import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale

internal actual fun uses24HourClock(): Boolean {
    // Resolve the locale's short time pattern; a 12-hour locale includes the AM/PM designator 'a'.
    val formatter = NSDateFormatter().apply {
        dateStyle = NSDateFormatterNoStyle
        timeStyle = NSDateFormatterShortStyle
        locale = NSLocale.currentLocale
    }
    return formatter.dateFormat?.contains("a") != true
}
