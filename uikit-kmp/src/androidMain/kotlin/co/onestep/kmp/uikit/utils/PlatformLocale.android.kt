package co.onestep.kmp.uikit.utils

import java.util.Locale

actual fun getDefaultCountryCode(): String = Locale.getDefault().country

actual fun getLocaleUnicodeType(key: String): String? =
    Locale.getDefault(Locale.Category.FORMAT).getUnicodeLocaleType(key)
