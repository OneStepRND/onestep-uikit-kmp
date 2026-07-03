package co.onestep.kmp.uikit.features.permissions

/**
 * Returns true if ALL required in-app permissions are already granted.
 * Platform-specific: Android checks AR + Notifications, iOS checks Motion + Location.
 */
internal expect fun hasRequiredInAppPermissions(): Boolean
