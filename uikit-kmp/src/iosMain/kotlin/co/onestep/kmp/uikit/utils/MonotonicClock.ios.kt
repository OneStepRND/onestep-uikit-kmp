package co.onestep.kmp.uikit.utils

import platform.Foundation.NSProcessInfo

/**
 * `ProcessInfo.systemUptime` — seconds since boot on a monotonic clock, unaffected by wall-clock
 * changes. Swift must use the same property when pushing a recording window through
 * `SwiftRecorderBridgeAdapter.onRecordingWindowChanged`, so both sides share one origin.
 */
actual fun monotonicNowMillis(): Long =
    (NSProcessInfo.processInfo.systemUptime * 1_000.0).toLong()
