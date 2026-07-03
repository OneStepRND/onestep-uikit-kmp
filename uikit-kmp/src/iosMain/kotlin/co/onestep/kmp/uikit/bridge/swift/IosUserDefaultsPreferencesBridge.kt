package co.onestep.kmp.uikit.bridge.swift

import co.onestep.kmp.uikit.bridge.PreferencesBridge
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import platform.Foundation.NSNumber
import platform.Foundation.NSUserDefaults
import platform.Foundation.numberWithFloat

/**
 * iOS [PreferencesBridge] backed by `NSUserDefaults.standardUserDefaults`.
 *
 * Fully implemented in Kotlin — no Swift is required since preferences are plain key-value storage.
 * Keys are namespaced with [KEY_PREFIX]. Nullable [Float] values are stored as [NSNumber] objects so
 * that `null` round-trips correctly (a missing key returns `null` rather than `0f`).
 *
 * [updates] emits the changed key on every setter so observers stay in sync.
 */
class IosUserDefaultsPreferencesBridge : PreferencesBridge {

    private val defaults = NSUserDefaults.standardUserDefaults

    private val _updates = MutableSharedFlow<String>(extraBufferCapacity = 16)
    override val updates: Flow<String> = _updates.asSharedFlow()

    private fun notifyChanged(key: String) {
        _updates.tryEmit(key)
    }

    override var measurementsSystem: String?
        get() = defaults.stringForKey(KEY_MEASUREMENTS_SYSTEM)
        set(value) {
            if (value != null) {
                defaults.setObject(value, KEY_MEASUREMENTS_SYSTEM)
            } else {
                defaults.removeObjectForKey(KEY_MEASUREMENTS_SYSTEM)
            }
            notifyChanged(KEY_MEASUREMENTS_SYSTEM)
        }

    override var sixMinHallwayLengthM: Float?
        get() = defaults.floatOrNull(KEY_6MIN_HALLWAY)
        set(value) {
            defaults.setFloatOrNull(KEY_6MIN_HALLWAY, value)
            notifyChanged(KEY_6MIN_HALLWAY)
        }

    override var twoMinHallwayLengthM: Float?
        get() = defaults.floatOrNull(KEY_2MIN_HALLWAY)
        set(value) {
            defaults.setFloatOrNull(KEY_2MIN_HALLWAY, value)
            notifyChanged(KEY_2MIN_HALLWAY)
        }

    override var suppressShortHallwayWarning6Min: Boolean
        get() = defaults.boolForKey(KEY_SUPPRESS_6MIN)
        set(value) {
            defaults.setBool(value, KEY_SUPPRESS_6MIN)
            notifyChanged(KEY_SUPPRESS_6MIN)
        }

    override var suppressShortHallwayWarning2Min: Boolean
        get() = defaults.boolForKey(KEY_SUPPRESS_2MIN)
        set(value) {
            defaults.setBool(value, KEY_SUPPRESS_2MIN)
            notifyChanged(KEY_SUPPRESS_2MIN)
        }

    override var permissionExplanationScreenShown: Boolean
        get() = defaults.boolForKey(KEY_PERMISSION_EXPLANATION_SHOWN)
        set(value) {
            defaults.setBool(value, KEY_PERMISSION_EXPLANATION_SHOWN)
            notifyChanged(KEY_PERMISSION_EXPLANATION_SHOWN)
        }

    override var activityRecognitionProbeCompleted: Boolean
        get() = defaults.boolForKey(KEY_ACTIVITY_RECOGNITION_PROBE)
        set(value) {
            defaults.setBool(value, KEY_ACTIVITY_RECOGNITION_PROBE)
            notifyChanged(KEY_ACTIVITY_RECOGNITION_PROBE)
        }

    override var postNotificationsProbeCompleted: Boolean
        get() = defaults.boolForKey(KEY_POST_NOTIFICATIONS_PROBE)
        set(value) {
            defaults.setBool(value, KEY_POST_NOTIFICATIONS_PROBE)
            notifyChanged(KEY_POST_NOTIFICATIONS_PROBE)
        }

    override fun setPermissionRequested(permission: String) {
        val key = KEY_PERMISSION_PREFIX + permission
        defaults.setBool(true, key)
        notifyChanged(key)
    }

    override fun hasPermissionRequestedBefore(permission: String): Boolean =
        defaults.boolForKey(KEY_PERMISSION_PREFIX + permission)

    private fun NSUserDefaults.floatOrNull(key: String): Float? =
        (objectForKey(key) as? NSNumber)?.floatValue

    private fun NSUserDefaults.setFloatOrNull(key: String, value: Float?) {
        if (value != null) {
            setObject(NSNumber.numberWithFloat(value), key)
        } else {
            removeObjectForKey(key)
        }
    }

    companion object {
        private const val KEY_PREFIX = "ost_uikit_"
        private const val KEY_MEASUREMENTS_SYSTEM = KEY_PREFIX + "measurements_system"
        private const val KEY_6MIN_HALLWAY = KEY_PREFIX + "six_min_hallway_length_m"
        private const val KEY_2MIN_HALLWAY = KEY_PREFIX + "two_min_hallway_length_m"
        private const val KEY_SUPPRESS_6MIN = KEY_PREFIX + "suppress_short_hallway_warning_6min"
        private const val KEY_SUPPRESS_2MIN = KEY_PREFIX + "suppress_short_hallway_warning_2min"
        private const val KEY_PERMISSION_EXPLANATION_SHOWN = KEY_PREFIX + "permission_explanation_screen_shown"
        private const val KEY_ACTIVITY_RECOGNITION_PROBE = KEY_PREFIX + "activity_recognition_probe_completed"
        private const val KEY_POST_NOTIFICATIONS_PROBE = KEY_PREFIX + "post_notifications_probe_completed"
        private const val KEY_PERMISSION_PREFIX = KEY_PREFIX + "permission_requested_"
    }
}
