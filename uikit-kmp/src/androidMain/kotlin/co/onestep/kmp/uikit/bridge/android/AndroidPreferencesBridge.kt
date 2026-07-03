package co.onestep.kmp.uikit.bridge.android

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import co.onestep.kmp.uikit.bridge.PreferencesBridge
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidPreferencesBridge(context: Context) : PreferencesBridge {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override val updates: Flow<String> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key != null) trySend(key)
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override var measurementsSystem: String?
        get() = prefs.getString(KEY_MEASUREMENTS_SYSTEM, null)
        set(value) = prefs.edit { putString(KEY_MEASUREMENTS_SYSTEM, value) }

    override var sixMinHallwayLengthM: Float?
        get() = if (prefs.contains(KEY_6MIN_HALLWAY)) prefs.getFloat(KEY_6MIN_HALLWAY, 0f) else null
        set(value) = prefs.edit {
            if (value != null) putFloat(KEY_6MIN_HALLWAY, value) else remove(KEY_6MIN_HALLWAY)
        }

    override var twoMinHallwayLengthM: Float?
        get() = if (prefs.contains(KEY_2MIN_HALLWAY)) prefs.getFloat(KEY_2MIN_HALLWAY, 0f) else null
        set(value) = prefs.edit {
            if (value != null) putFloat(KEY_2MIN_HALLWAY, value) else remove(KEY_2MIN_HALLWAY)
        }

    override var suppressShortHallwayWarning6Min: Boolean
        get() = prefs.getBoolean(KEY_SUPPRESS_6MIN, false)
        set(value) = prefs.edit { putBoolean(KEY_SUPPRESS_6MIN, value) }

    override var suppressShortHallwayWarning2Min: Boolean
        get() = prefs.getBoolean(KEY_SUPPRESS_2MIN, false)
        set(value) = prefs.edit { putBoolean(KEY_SUPPRESS_2MIN, value) }

    override var permissionExplanationScreenShown: Boolean
        get() = prefs.getBoolean(KEY_PERMISSION_EXPLANATION_SHOWN, false)
        set(value) = prefs.edit { putBoolean(KEY_PERMISSION_EXPLANATION_SHOWN, value) }

    override var activityRecognitionProbeCompleted: Boolean
        get() = prefs.getBoolean(KEY_ACTIVITY_RECOGNITION_PROBE, false)
        set(value) = prefs.edit { putBoolean(KEY_ACTIVITY_RECOGNITION_PROBE, value) }

    override var postNotificationsProbeCompleted: Boolean
        get() = prefs.getBoolean(KEY_POST_NOTIFICATIONS_PROBE, false)
        set(value) = prefs.edit { putBoolean(KEY_POST_NOTIFICATIONS_PROBE, value) }

    override fun setPermissionRequested(permission: String) {
        prefs.edit { putBoolean("$KEY_PERMISSION_PREFIX$permission", true) }
    }

    override fun hasPermissionRequestedBefore(permission: String): Boolean =
        prefs.getBoolean("$KEY_PERMISSION_PREFIX$permission", false)

    companion object {
        private const val PREFS_NAME = "ost_uikit_kmp_prefs"
        private const val KEY_MEASUREMENTS_SYSTEM = "measurements_system"
        private const val KEY_6MIN_HALLWAY = "six_min_hallway_length_m"
        private const val KEY_2MIN_HALLWAY = "two_min_hallway_length_m"
        private const val KEY_SUPPRESS_6MIN = "suppress_short_hallway_warning_6min"
        private const val KEY_SUPPRESS_2MIN = "suppress_short_hallway_warning_2min"
        private const val KEY_PERMISSION_EXPLANATION_SHOWN = "permission_explanation_screen_shown"
        private const val KEY_ACTIVITY_RECOGNITION_PROBE = "activity_recognition_probe_completed"
        private const val KEY_POST_NOTIFICATIONS_PROBE = "post_notifications_probe_completed"
        private const val KEY_PERMISSION_PREFIX = "permission_requested_"
    }
}
