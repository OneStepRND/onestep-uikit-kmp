package co.onestep.kmp.uikit.bridge

import co.onestep.kmp.uikit.models.OSTMeasurementSystem
import kotlinx.coroutines.flow.Flow

/**
 * Bridge interface abstracting CorePreferences.
 * Exposes only the preferences UIKit actually needs.
 */
interface PreferencesBridge {
    val updates: Flow<String>

    var measurementsSystem: String?
    // The last-entered hallway length now lives in the SDK-managed custom-metadata store (see
    // HallwayDistanceManager), so it follows the user across devices — it is no longer a
    // device-local preference here.
    var suppressShortHallwayWarning6Min: Boolean
    var suppressShortHallwayWarning2Min: Boolean
    var permissionExplanationScreenShown: Boolean
    var activityRecognitionProbeCompleted: Boolean
    var postNotificationsProbeCompleted: Boolean

    fun getMeasurementSystem(): OSTMeasurementSystem {
        return when (measurementsSystem) {
            "imperial" -> OSTMeasurementSystem.IMPERIAL
            "metric" -> OSTMeasurementSystem.METRIC
            else -> OSTMeasurementSystem.DEFAULT
        }
    }

    fun setPermissionRequested(permission: String)
    fun hasPermissionRequestedBefore(permission: String): Boolean
}
