package co.onestep.kmp.uikit.features.permissions

/**
 * Represents the types of permissions requested in the permission flow.
 *
 * This is the common multiplatform enum containing only type names.
 * Platform-specific properties (androidPermission, isGranted) are defined
 * as extensions in androidMain.
 */
enum class PermissionType {
    /** Physical activity recognition permission (required for Android Q+ / API 29+) */
    ACTIVITY_RECOGNITION,

    /** Notification permission (required for Android TIRAMISU+ / API 33+) */
    POST_NOTIFICATIONS,

    /** Battery optimization exemption (uses platform-specific API, not a runtime permission) */
    BATTERY_OPTIMIZATION,

    /** Audio recording permission */
    RECORD_AUDIO,
    ;

    companion object
}
