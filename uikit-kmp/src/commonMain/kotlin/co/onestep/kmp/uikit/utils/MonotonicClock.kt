package co.onestep.kmp.uikit.utils

/**
 * "Now" on the platform's monotonic clock, in milliseconds.
 *
 * Duration math for a recording must never use wall-clock time: an NTP sync or a user changing
 * the device clock moves it, and a recording that spans such a jump would report a wrong length.
 * This clock only moves forward, at one millisecond per millisecond.
 *
 * The origin is arbitrary and differs per platform, so a reading is only meaningful when
 * subtracted from another reading of this same function — which is exactly what
 * [co.onestep.kmp.uikit.models.OSTRecordingWindow] does. Whoever stamps a window
 * ([co.onestep.kmp.uikit.bridge.RecorderBridge.currentRecordingWindow]) must stamp it on this
 * clock:
 * - Android: `SystemClock.elapsedRealtime()` — matches the core SDK's `OSTRecordingWindow`.
 * - iOS: `ProcessInfo.processInfo.systemUptime`.
 */
expect fun monotonicNowMillis(): Long
