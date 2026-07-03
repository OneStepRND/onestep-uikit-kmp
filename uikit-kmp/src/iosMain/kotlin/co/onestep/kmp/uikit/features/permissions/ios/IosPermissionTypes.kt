package co.onestep.kmp.uikit.features.permissions.ios

/**
 * iOS-specific permission types used in the permission flow.
 * These map to native iOS framework permissions (CoreMotion, CoreLocation, HealthKit).
 */
enum class IosPermissionType {
    MOTION_FITNESS,
    LOCATION_WHILE_USING,
    LOCATION_ALWAYS,
    HEALTH_KIT,
    MICROPHONE,
}

/**
 * Status of an iOS permission.
 */
enum class IosPermissionStatus {
    GRANTED,
    DENIED,
    NOT_DETERMINED,
    RESTRICTED,
}
