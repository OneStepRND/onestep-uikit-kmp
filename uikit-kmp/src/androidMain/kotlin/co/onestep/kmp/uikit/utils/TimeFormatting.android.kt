package co.onestep.kmp.uikit.utils

import android.text.format.DateFormat
import co.onestep.kmp.uikit.di.UIKitServiceLocator

internal actual fun uses24HourClock(): Boolean {
    val context = UIKitServiceLocator.resourceProvider.context
    return DateFormat.is24HourFormat(context)
}
