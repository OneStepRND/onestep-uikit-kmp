package co.onestep.kmp.uikit.utils

import platform.Foundation.NSLocale
import platform.Foundation.countryCode
import platform.Foundation.currentLocale

actual fun getDefaultCountryCode(): String = NSLocale.currentLocale.countryCode ?: ""

actual fun getLocaleUnicodeType(key: String): String? = null // iOS doesn't have Unicode locale type
