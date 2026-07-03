package co.onestep.kmp.uikit.features.permissions.analytics

import co.onestep.kmp.uikit.features.permissions.OSTPermissionMode
import co.onestep.kmp.uikit.features.permissions.PermissionType

/**
 * Maps [PermissionType] to analytics permission name strings.
 *
 * Returns null for permissions that should not be tracked (e.g., BATTERY_OPTIMIZATION).
 */
internal fun PermissionType.toAnalyticsPermissionName(): String? =
    when (this) {
        PermissionType.ACTIVITY_RECOGNITION -> "physical_activity"
        PermissionType.POST_NOTIFICATIONS -> "notifications"
        PermissionType.RECORD_AUDIO -> "microphone"
        PermissionType.BATTERY_OPTIMIZATION -> null // Skip in analytics
    }

/**
 * Maps [OSTPermissionMode] to analytics flow_name strings.
 */
internal fun OSTPermissionMode.toAnalyticsFlowName(): String =
    when (this) {
        OSTPermissionMode.IN_APP -> "in_app"
        OSTPermissionMode.BACKGROUND -> "background"
        OSTPermissionMode.HEALTH_KIT -> "health_kit"
        OSTPermissionMode.FULL -> "full"
    }

/**
 * Determines the variant string based on probe completion state and grant status.
 *
 * @param probeCompleted Whether the probe request has been completed for this permission
 * @param isGranted Whether the permission is currently granted
 * @return "first_time" if user has never been asked, "after_denied" otherwise
 */
internal fun determineVariant(
    probeCompleted: Boolean,
    isGranted: Boolean,
): String =
    if (!probeCompleted && !isGranted) {
        "first_time"
    } else {
        "after_denied"
    }
