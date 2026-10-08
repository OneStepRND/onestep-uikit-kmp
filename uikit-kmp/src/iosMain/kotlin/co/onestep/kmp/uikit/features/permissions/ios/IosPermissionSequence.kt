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
            // HealthKit support was removed from uikit-kmp (App Store guideline 2.5.1:
            // linking platform.HealthKit flags every consumer's binary; no KMP consumer
            // used it). HEALTH_KIT mode completes immediately; FULL no longer includes it.
            OSTPermissionMode.HEALTH_KIT -> emptyList()
            OSTPermissionMode.FULL -> listOf(
                IosPermissionType.LOCATION_WHILE_USING,
                IosPermissionType.LOCATION_ALWAYS,
                IosPermissionType.MOTION_FITNESS,
            )
        }
}
