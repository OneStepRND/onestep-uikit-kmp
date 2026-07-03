package co.onestep.kmp.uikit.features.permissions.ios

import co.onestep.kmp.uikit.features.permissions.OSTPermissionMode

/**
 * Maps [OSTPermissionMode] to an ordered list of iOS permission types to request.
 */
internal object IosPermissionSequence {

    fun forMode(mode: OSTPermissionMode): List<IosPermissionType> =
        when (mode) {
            OSTPermissionMode.IN_APP -> listOf(
                IosPermissionType.LOCATION_WHILE_USING,
                IosPermissionType.MOTION_FITNESS,
            )
            OSTPermissionMode.BACKGROUND -> listOf(
                IosPermissionType.LOCATION_WHILE_USING,
                IosPermissionType.LOCATION_ALWAYS,
                IosPermissionType.MOTION_FITNESS,
            )
            OSTPermissionMode.HEALTH_KIT -> listOf(
                IosPermissionType.HEALTH_KIT,
            )
            OSTPermissionMode.FULL -> listOf(
                IosPermissionType.LOCATION_WHILE_USING,
                IosPermissionType.LOCATION_ALWAYS,
                IosPermissionType.MOTION_FITNESS,
                IosPermissionType.HEALTH_KIT,
            )
        }
}
