package co.onestep.kmp.uikit.features.permissions.analytics

import co.onestep.kmp.uikit.OSTUIKitAnalyticsHandler
import co.onestep.kmp.sdk.OSTEvent

/**
 * Central analytics tracker for the permissions flow.
 *
 * This class handles all analytics event tracking for the permission request flow,
 * including screen views, button clicks, and permission status events.
 *
 * @param analytics The analytics handler that emits events
 */
internal class PermissionAnalyticsTracker(
    private val analytics: OSTUIKitAnalyticsHandler,
) {
    /**
     * Builds the base property map shared across all permission events.
     */
    private fun buildBaseProperties(
        flowName: String,
        permission: String? = null,
        variant: String? = null,
    ): Map<String, String> =
        buildMap {
            put(PermissionAnalyticsEvents.Properties.FLOW_NAME, flowName)
            permission?.let { put(PermissionAnalyticsEvents.Properties.PERMISSION, it) }
            variant?.let { put(PermissionAnalyticsEvents.Properties.VARIANT, it) }
        }

    private fun trackEvent(eventName: String, properties: Map<String, String>) {
        analytics.onEvent(OSTEvent(name = eventName, properties = properties))
    }

    /**
     * Track when the data safety explanation screen is viewed.
     */
    fun trackDataSafetyScreen(flowName: String) {
        trackEvent(
            PermissionAnalyticsEvents.SCREEN_DATA_SAFETY,
            buildBaseProperties(flowName),
        )
    }

    /**
     * Track when a permission request screen is viewed.
     */
    fun trackPermissionRequestScreen(
        permission: String,
        variant: String,
        flowName: String,
    ) {
        trackEvent(
            PermissionAnalyticsEvents.SCREEN_PERMISSION_REQUEST,
            buildBaseProperties(flowName, permission, variant),
        )
    }

    /**
     * Track when the "Allow" button is clicked.
     */
    fun trackAllowClicked(
        permission: String,
        variant: String,
        flowName: String,
    ) {
        trackEvent(
            PermissionAnalyticsEvents.CLICKED_ALLOW,
            buildBaseProperties(flowName, permission, variant),
        )
    }

    /**
     * Track when the "Go to Settings" button is clicked.
     */
    fun trackGoToSettingsClicked(
        permission: String,
        variant: String,
        flowName: String,
    ) {
        trackEvent(
            PermissionAnalyticsEvents.CLICKED_GO_TO_SETTINGS,
            buildBaseProperties(flowName, permission, variant),
        )
    }

    /**
     * Track when the close (X) button is clicked.
     */
    fun trackPermissionCloseClicked(
        permission: String,
        variant: String,
        flowName: String,
    ) {
        trackEvent(
            PermissionAnalyticsEvents.CLICKED_PERMISSION_CLOSE,
            buildBaseProperties(flowName, permission, variant),
        )
    }

    /**
     * Track when the "How is my data used" bottom sheet is opened.
     */
    fun trackHowIsMyDataUsedClicked(
        permission: String,
        variant: String,
        flowName: String,
    ) {
        trackEvent(
            PermissionAnalyticsEvents.CLICKED_HOW_IS_MY_DATA_USED,
            buildBaseProperties(flowName, permission, variant),
        )
    }
}
