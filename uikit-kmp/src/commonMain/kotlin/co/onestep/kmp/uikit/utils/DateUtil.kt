package co.onestep.kmp.uikit.utils

/**
 * Platform-abstracted date/time formatting utilities.
 *
 * - Android actual: delegates to SimpleDateFormat + Context for 24h preference
 * - iOS actual: delegates to NSDateFormatter
 */

/**
 * Formats a timestamp (epoch millis) as "MMM dd, yyyy | h:mm a" or "MMM dd, yyyy | HH:mm"
 * depending on the user's clock preference.
 */
expect fun Long.toLocalizedTimeString(): String

/**
 * Formats a timestamp (epoch millis) as "h:mm a" or "HH:mm"
 * depending on the user's clock preference.
 */
expect fun Long.toDeviceTimeString(): String
