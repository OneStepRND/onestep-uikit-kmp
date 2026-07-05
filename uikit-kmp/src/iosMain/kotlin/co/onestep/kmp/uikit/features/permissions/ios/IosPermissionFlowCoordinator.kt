package co.onestep.kmp.uikit.features.permissions.ios

import co.onestep.kmp.uikit.OSTUIKitAnalyticsHandler
import co.onestep.kmp.uikit.features.permissions.OSTPermissionMode
import co.onestep.kmp.uikit.features.permissions.analytics.PermissionAnalyticsTracker
import co.onestep.kmp.uikit.features.permissions.analytics.toAnalyticsFlowName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * State machine coordinating the iOS permission flow.
 *
 * Manages screen transitions based on permission mode, filters out already-granted
 * permissions, and handles denied→settings redirect flows.
 */
internal class IosPermissionFlowCoordinator(
    private val mode: OSTPermissionMode,
    private val checker: IosPermissionChecker,
    private val onComplete: (granted: Boolean) -> Unit,
    private val onDismissFlow: () -> Unit = {},
    private val preferencesBridge: co.onestep.kmp.uikit.bridge.PreferencesBridge? = null,
    analyticsHandler: OSTUIKitAnalyticsHandler? = null,
    private val showExplanationScreen: Boolean = true,
) {
    private val _currentScreen = MutableStateFlow<IosPermissionScreen>(IosPermissionScreen.Rationalization)
    val currentScreen: StateFlow<IosPermissionScreen> = _currentScreen.asStateFlow()

    private val tracker: PermissionAnalyticsTracker? =
        analyticsHandler?.let { PermissionAnalyticsTracker(it) }
    private val flowName: String = mode.toAnalyticsFlowName()

    private var screens = mutableListOf<IosPermissionScreen>()
    private var currentIndex = -1
    private var allGranted = true

    /**
     * Initialize the flow: build the screen list from the mode,
     * filter out already-granted permissions, optionally insert rationalization.
     */
    fun initialize() {
        val requiredTypes = IosPermissionSequence.forMode(mode)
        val pendingTypes = requiredTypes.filter { type ->
            checker.checkStatus(type) != IosPermissionStatus.GRANTED
        }

        if (pendingTypes.isEmpty()) {
            onComplete(true)
            return
        }

        screens = mutableListOf()

        // Add rationalization screen if there are 2+ permissions to request,
        // UNLESS the only pending types are location (2-step flow is one logical permission)
        val isOnlyLocationTwoStep = pendingTypes.all {
            it == IosPermissionType.LOCATION_WHILE_USING || it == IosPermissionType.LOCATION_ALWAYS
        }
        val explanationAlreadyShown = preferencesBridge?.permissionExplanationScreenShown ?: false
        if (showExplanationScreen && !explanationAlreadyShown && pendingTypes.size >= 2 && !isOnlyLocationTwoStep) {
            screens.add(IosPermissionScreen.Rationalization)
            preferencesBridge?.let { it.permissionExplanationScreenShown = true }
        }

        screens.addAll(buildScreensForTypes(pendingTypes))

        currentIndex = 0
        _currentScreen.value = screens[currentIndex]

        // Track rationalization screen view if shown
        if (screens.firstOrNull() is IosPermissionScreen.Rationalization) {
            tracker?.trackDataSafetyScreen(flowName)
        }
    }

    /** Advance to the next screen in the flow. */
    fun nextScreen() {
        currentIndex++
        if (currentIndex >= screens.size) {
            onComplete(allGranted)
            return
        }
        val screen = screens[currentIndex]
        if (screen is IosPermissionScreen.Completed) {
            onComplete(allGranted)
            return
        }
        _currentScreen.value = screen
    }

    /** Called when a permission is granted — advance to next screen. */
    fun onPermissionGranted(type: IosPermissionType) {
        tracker?.trackAllowClicked(
            permission = type.analyticsName(),
            variant = if (hasBeenRequestedBefore(type)) "after_denied" else "first_time",
            flowName = flowName,
        )
        trackPermissionRequested(type)
        nextScreen()
    }

    /** Called when a permission is denied — track denial and exit the flow (PRD: exit on deny). */
    fun onPermissionDenied(type: IosPermissionType) {
        trackPermissionRequested(type)
        allGranted = false
        // PRD Section 6: "If user denies a permission: Exit the flow"
        // Next time the flow opens, hasBeenRequestedBefore + DENIED status → settings variant
        onComplete(false)
    }

    /**
     * Called when returning from system Settings.
     * Re-checks the permission status and advances if granted, otherwise stays on settings screen.
     */
    fun onReturnFromSettings(type: IosPermissionType) {
        val status = checker.checkStatus(type)
        if (status == IosPermissionStatus.GRANTED) {
            nextScreen()
        }
        // If still not granted, user stays on the settings screen
    }

    /** Called when user dismisses the flow early (X button). */
    fun onDismiss() {
        val current = _currentScreen.value
        val permName = current.permissionName() ?: "unknown"
        tracker?.trackPermissionCloseClicked(
            permission = permName,
            variant = "close",
            flowName = flowName,
        )
        onDismissFlow()
    }

    /** Track when the "Go to Settings" button is clicked. */
    fun trackGoToSettings(type: IosPermissionType) {
        tracker?.trackGoToSettingsClicked(
            permission = type.analyticsName(),
            variant = "after_denied",
            flowName = flowName,
        )
    }

    /** Track that a permission was requested via PreferencesBridge. */
    fun trackPermissionRequested(type: IosPermissionType) {
        preferencesBridge?.setPermissionRequested(type.preferencesKey())
    }

    /** Check if a permission was previously requested. */
    fun hasBeenRequestedBefore(type: IosPermissionType): Boolean {
        return preferencesBridge?.hasPermissionRequestedBefore(type.preferencesKey()) ?: false
    }

    // PRD: "After 1 denial [iOS], the next time the flow is opened, show the 'Go to Settings' variant"
    private fun buildScreensForTypes(pendingTypes: List<IosPermissionType>): List<IosPermissionScreen> {
        val result = mutableListOf<IosPermissionScreen>()
        for (type in pendingTypes) {
            val wasPreviouslyDenied = hasBeenRequestedBefore(type) &&
                checker.checkStatus(type) == IosPermissionStatus.DENIED
            when (type) {
                IosPermissionType.MOTION_FITNESS ->
                    result.add(IosPermissionScreen.Motion(showSettings = wasPreviouslyDenied))
                IosPermissionType.LOCATION_WHILE_USING ->
                    result.add(IosPermissionScreen.Location(phase = LocationPhase.WHILE_USING, showSettings = wasPreviouslyDenied))
                IosPermissionType.LOCATION_ALWAYS -> {
                    // PRD 4.3: Detect location downgrade (was Always, now WhenInUse)
                    val hasWhileUsing = checker.checkStatus(IosPermissionType.LOCATION_WHILE_USING) == IosPermissionStatus.GRANTED
                    val wasDowngraded = hasBeenRequestedBefore(IosPermissionType.LOCATION_ALWAYS) && hasWhileUsing
                    if (wasDowngraded) {
                        // Downgrade detected: show settings redirect to re-enable "Always"
                        result.add(IosPermissionScreen.Location(phase = LocationPhase.ALWAYS, showSettings = true, isDowngraded = true))
                    } else if (!hasBeenRequestedBefore(IosPermissionType.LOCATION_ALWAYS)) {
                        result.add(IosPermissionScreen.Location(phase = LocationPhase.ALWAYS))
                    }
                }
                IosPermissionType.HEALTH_KIT ->
                    result.add(IosPermissionScreen.HealthKit(showSettings = wasPreviouslyDenied))
                IosPermissionType.MICROPHONE -> {
                    // Microphone is handled separately, not part of the main flow
                }
            }
        }
        return result
    }
}

private fun IosPermissionType.preferencesKey(): String = when (this) {
    IosPermissionType.MOTION_FITNESS -> "ios_motion_fitness"
    IosPermissionType.LOCATION_WHILE_USING -> "ios_location_when_in_use"
    IosPermissionType.LOCATION_ALWAYS -> "ios_location_always"
    IosPermissionType.HEALTH_KIT -> "ios_health_kit"
    IosPermissionType.MICROPHONE -> "ios_microphone"
}

internal fun IosPermissionType.analyticsName(): String = when (this) {
    IosPermissionType.MOTION_FITNESS -> "motion_fitness"
    IosPermissionType.LOCATION_WHILE_USING -> "location_when_in_use"
    IosPermissionType.LOCATION_ALWAYS -> "location_always"
    IosPermissionType.HEALTH_KIT -> "health_kit"
    IosPermissionType.MICROPHONE -> "microphone"
}

private fun IosPermissionScreen.permissionName(): String? = when (this) {
    is IosPermissionScreen.Rationalization -> "rationalization"
    is IosPermissionScreen.Motion -> "motion_fitness"
    is IosPermissionScreen.Location -> if (phase == LocationPhase.ALWAYS) "location_always" else "location_when_in_use"
    is IosPermissionScreen.HealthKit -> "health_kit"
    is IosPermissionScreen.Completed -> null
}
