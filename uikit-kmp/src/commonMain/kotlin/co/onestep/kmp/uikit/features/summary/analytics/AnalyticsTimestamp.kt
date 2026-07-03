package co.onestep.kmp.uikit.features.summary.analytics

/**
 * Formats an epoch-millisecond instant as the analytics `activity_date` value: a
 * millisecond-precision UTC timestamp with a literal `Z`, e.g. `2026-05-20T11:22:09.815Z`.
 *
 * Mirrors uikit's `Date.toDeviceTimestamp()` (pattern `yyyy-MM-dd'T'HH:mm:ss.SSS'Z'`, UTC,
 * ASCII digits) so both SDKs emit an identical `activity_date` string.
 */
internal expect fun Long.toAnalyticsDeviceTimestamp(): String
