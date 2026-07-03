package co.onestep.kmp.uikit.features.permissions

/**
 * Manages the sequence of permissions to request based on the permission mode.
 *
 * @property mode The permission mode (IN_APP or BACKGROUND)
 * @property permissions The ordered list of permissions for this mode
 */
data class PermissionSequence(
    val mode: OSTPermissionMode,
    val permissions: List<PermissionType>,
) {
    /**
     * Returns the list of permissions required for the current Android version.
     * Filters out permissions that are not applicable to the current OS version.
     *
     * @param currentAndroidVersion The current Android SDK version (Build.VERSION.SDK_INT)
     * @return List of permissions applicable to the current Android version
     */
    fun getRequiredPermissions(currentAndroidVersion: Int): List<PermissionType> =
        permissions.filter { type ->
            when (type) {
                PermissionType.ACTIVITY_RECOGNITION -> currentAndroidVersion >= 29 // Build.VERSION_CODES.Q
                PermissionType.POST_NOTIFICATIONS -> currentAndroidVersion >= 33 // Build.VERSION_CODES.TIRAMISU
                PermissionType.RECORD_AUDIO -> true
                PermissionType.BATTERY_OPTIMIZATION -> true // Available on all versions
            }
        }

    companion object {
        /**
         * Factory method to create a PermissionSequence for the given mode.
         *
         * @param mode The permission mode (IN_APP or BACKGROUND)
         * @return A PermissionSequence configured for the specified mode
         */
        fun forMode(mode: OSTPermissionMode): PermissionSequence =
            when (mode) {
                OSTPermissionMode.IN_APP ->
                    PermissionSequence(
                        mode,
                        listOf(
                            PermissionType.ACTIVITY_RECOGNITION,
                            PermissionType.POST_NOTIFICATIONS,
                        ),
                    )
                OSTPermissionMode.BACKGROUND ->
                    PermissionSequence(
                        mode,
                        listOf(
                            PermissionType.ACTIVITY_RECOGNITION,
                            PermissionType.POST_NOTIFICATIONS,
                            PermissionType.BATTERY_OPTIMIZATION,
                        ),
                    )
                // iOS-only modes: no Android permissions needed
                OSTPermissionMode.HEALTH_KIT,
                OSTPermissionMode.FULL ->
                    PermissionSequence(mode, emptyList())
            }
    }
}
