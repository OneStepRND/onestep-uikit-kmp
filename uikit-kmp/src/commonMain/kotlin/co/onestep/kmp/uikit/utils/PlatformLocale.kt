package co.onestep.kmp.uikit.utils

/**
 * Returns the default country code for the current locale (e.g., "US", "GB").
 */
expect fun getDefaultCountryCode(): String

/**
 * Returns the Unicode locale type for the given key from the current format locale.
 * For example, key "ms" returns the measurement system type ("ussystem", "metric", etc.).
 *
 * @param key The Unicode locale extension key
 * @return The locale type string, or null if not available
 */
expect fun getLocaleUnicodeType(key: String): String?
