package co.onestep.kmp.uikit.utils

import android.os.SystemClock

/**
 * `elapsedRealtime()` — monotonic and, unlike `uptimeMillis()`, it keeps advancing while the
 * device is in deep sleep. It is also the clock the core SDK stamps `OSTRecordingWindow` with,
 * so window readings and this function share one origin.
 */
actual fun monotonicNowMillis(): Long = SystemClock.elapsedRealtime()
