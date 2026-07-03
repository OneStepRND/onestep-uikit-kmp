package co.onestep.kmp.uikit.bridge

import kotlinx.coroutines.flow.Flow

enum class Permission {
    ACTIVITY_RECOGNITION,
    NOTIFICATIONS,
    MICROPHONE,
    LOCATION_WHEN_IN_USE,
    LOCATION_ALWAYS,
    HEALTH_KIT,
}

enum class PermissionStatus {
    GRANTED,
    DENIED,
    NOT_DETERMINED,
}

data class PermissionResult(
    val permission: Permission,
    val status: PermissionStatus,
)

/**
 * Platform permissions manager.
 * Android: ActivityCompat permissions, iOS: CMMotionActivityManager + UNUserNotificationCenter + CLLocationManager + HKHealthStore
 */
expect class PlatformPermissionsManager {
    fun requestActivityRecognition(): Flow<PermissionResult>
    fun requestNotifications(): Flow<PermissionResult>
    fun requestLocationWhenInUse(): Flow<PermissionResult>
    fun requestLocationAlways(): Flow<PermissionResult>
    fun requestHealthKit(): Flow<PermissionResult>
    fun checkPermissionStatus(permission: Permission): PermissionStatus
}
