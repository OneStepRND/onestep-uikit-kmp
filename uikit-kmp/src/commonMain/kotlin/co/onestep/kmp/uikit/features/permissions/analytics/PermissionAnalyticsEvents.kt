package co.onestep.kmp.uikit.features.permissions.analytics

/**
 * Analytics event names and property constants for permission flow tracking.
 *
 * All event names and property keys follow snake_case convention to match
 * existing analytics patterns in the codebase.
 */
internal object PermissionAnalyticsEvents {
    // Screen view events
    const val SCREEN_DATA_SAFETY = "screen: data_safety"
    const val SCREEN_PERMISSION_REQUEST = "screen: permission_request"

    // Click events
    const val CLICKED_ALLOW = "clicked: allow"
    const val CLICKED_GO_TO_SETTINGS = "clicked: go_to_settings"
    const val CLICKED_PERMISSION_CLOSE = "clicked: permission_close"
    const val CLICKED_HOW_IS_MY_DATA_USED = "clicked: how_is_my_data_used"

    /**
     * Property keys used across all permission analytics events
     */
    object Properties {
        const val PERMISSION = "permission"
        const val VARIANT = "variant"
        const val FLOW_NAME = "flow_name"
        const val ORGANIZATION_NAME = "organization_name"
        const val PATIENT_UUID = "patient_uuid"
    }
}
