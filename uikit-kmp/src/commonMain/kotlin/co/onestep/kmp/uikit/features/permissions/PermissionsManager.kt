package co.onestep.kmp.uikit.features.permissions

/**
 * Platform-agnostic permissions manager interface.
 *
 * Provides basic permission checking and request tracking.
 * Platform-specific operations (like [getPermissionStatus] on Android which requires Activity)
 * are defined as extension functions in platform source sets.
 */
interface PermissionsManager {
    fun checkPermissions(permission: String): Boolean

    fun markPermissionRequested(permission: String)

    fun hasRequestedPermissionBefore(permission: String): Boolean
}
