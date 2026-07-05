package co.onestep.kmp.uikit.features.permissions

import android.app.Activity
import android.os.Build
import androidx.lifecycle.ViewModel
import co.onestep.kmp.uikit.bridge.PreferencesBridge
import co.onestep.kmp.uikit.features.permissions.analytics.PermissionAnalyticsTracker
import co.onestep.kmp.uikit.features.permissions.analytics.determineVariant
import co.onestep.kmp.uikit.features.permissions.analytics.toAnalyticsFlowName
import co.onestep.kmp.uikit.features.permissions.analytics.toAnalyticsPermissionName
import co.onestep.kmp.uikit.features.permissions.destinations.ARRequestScreenDestination
import co.onestep.kmp.uikit.features.permissions.destinations.ARSettingsRedirectDestination
import co.onestep.kmp.uikit.features.permissions.destinations.BatteryOptimizationRequestScreenDestination
import co.onestep.kmp.uikit.features.permissions.destinations.ExplanationScreenDestination
import co.onestep.kmp.uikit.features.permissions.destinations.NotificationRequestScreenDestination
import co.onestep.kmp.uikit.features.permissions.destinations.NotificationSettingsRedirectDestination
import co.onestep.kmp.uikit.features.permissions.destinations.PermissionDestinations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class PermissionWizardViewModel(
    val prefs: PreferencesBridge,
    val permissionsManager: PermissionsManager,
    private val analyticsTracker: PermissionAnalyticsTracker,
    private val showPermissionExplanationScreen: Boolean = true,
    private val mode: OSTPermissionMode,
) : ViewModel() {
    private val permissionSequence = PermissionSequence.forMode(mode)

    private val _currentPermissionType = MutableStateFlow<PermissionType?>(null)
    val currentPermissionType: StateFlow<PermissionType?> = _currentPermissionType

    private val _permissionStates =
        MutableStateFlow<Map<PermissionType, PermissionNavigator.PermissionState>>(emptyMap())
    val permissionStates: StateFlow<Map<PermissionType, PermissionNavigator.PermissionState>> =
        _permissionStates

    private val _currentScreen =
        MutableStateFlow<PermissionDestinations>(ExplanationScreenDestination)
    val currentDestination: StateFlow<PermissionDestinations?> = _currentScreen

    private val _shouldExit = MutableStateFlow(false)
    val shouldExit: StateFlow<Boolean> = _shouldExit

    private val _returningFromSettings = MutableStateFlow(false)
    val returningFromSettings: StateFlow<Boolean> = _returningFromSettings

    private fun updatePermissionState(
        permissionType: PermissionType,
        updater: (PermissionNavigator.PermissionState) -> PermissionNavigator.PermissionState,
    ) {
        _permissionStates.value =
            _permissionStates.value.mapValues { (key, value) ->
                if (key == permissionType) updater(value) else value
            }
    }

    fun init(activity: Activity) {
        initializePermissionStates(activity)

        _currentPermissionType.value =
            permissionSequence
                .getRequiredPermissions(Build.VERSION.SDK_INT)
                .firstOrNull()
    }

    // F6: probe-completion lookup extracted from initializePermissionStates
    private fun PermissionType.probeCompleted(): Boolean =
        when (this) {
            PermissionType.ACTIVITY_RECOGNITION -> prefs.activityRecognitionProbeCompleted
            PermissionType.POST_NOTIFICATIONS -> prefs.postNotificationsProbeCompleted
            else -> false
        }

    // F6: permission-status resolution extracted from initializePermissionStates
    private fun PermissionType.resolveStatus(
        activity: Activity,
        probeCompleted: Boolean,
    ): UiKitPermissionStatus =
        if (androidPermission != null) {
            permissionsManager.getPermissionStatus(
                androidPermission!!,
                activity,
                probeCompleted = probeCompleted,
            )
        } else {
            if (isGranted(activity)) UiKitPermissionStatus.Granted
            else UiKitPermissionStatus.UnknownCouldRequest
        }

    private fun initializePermissionStates(activity: Activity) {
        val states =
            permissionSequence
                .getRequiredPermissions(Build.VERSION.SDK_INT)
                .associateWith { type ->
                    val isGranted = type.isGranted(activity)
                    val probeCompleted = type.probeCompleted()
                    val status = type.resolveStatus(activity, probeCompleted)

                    PermissionNavigator.PermissionState(
                        type = type,
                        isGranted = isGranted,
                        status = status,
                        probeCompleted = probeCompleted,
                        requestSource =
                            if (status == UiKitPermissionStatus.UnknownCouldRequest && type.androidPermission != null) {
                                RequestSource.Probe
                            } else {
                                RequestSource.InFlow
                            },
                        justDenied = false,
                    )
                }

        _permissionStates.value = states
    }

    // F7: inner else-branch of determineStartDestination extracted for readability
    private fun resolveFirstUngrantedDestination(): PermissionDestinations {
        val firstUngranted =
            _permissionStates.value.entries
                .firstOrNull { !it.value.isGranted }

        return if (firstUngranted != null) {
            _currentPermissionType.value = firstUngranted.key
            val destination =
                when (firstUngranted.value.status) {
                    UiKitPermissionStatus.Granted -> {
                        getRequestDestination(
                            _currentPermissionType.value ?: PermissionType.ACTIVITY_RECOGNITION,
                            mode,
                        )
                    }

                    UiKitPermissionStatus.UnknownCouldRequest,
                    UiKitPermissionStatus.DeniedCanRequestAgain,
                    -> {
                        getRequestDestination(firstUngranted.key, mode)
                    }

                    UiKitPermissionStatus.DeniedAlways -> {
                        getSettingsDestination(firstUngranted.key, mode)
                    }
                }
            trackPermissionScreenShown(firstUngranted.key)
            destination
        } else {
            val permType = _currentPermissionType.value ?: PermissionType.ACTIVITY_RECOGNITION
            val destination = getRequestDestination(permType, mode)
            trackPermissionScreenShown(permType)
            destination
        }
    }

    fun determineStartDestination(activity: Activity?): PermissionDestinations {
        if (activity == null) return _currentScreen.value
        _currentScreen.value =
            when {
                showPermissionExplanationScreen && !prefs.permissionExplanationScreenShown -> {
                    analyticsTracker.trackDataSafetyScreen(mode.toAnalyticsFlowName())
                    ExplanationScreenDestination
                }

                else -> resolveFirstUngrantedDestination()
            }
        return _currentScreen.value
    }

    fun processPermissionResult(
        activity: Activity,
        granted: Boolean,
    ) {
        val currentType = _currentPermissionType.value ?: return
        val currentState = _permissionStates.value[currentType] ?: return

        if (granted) {
            updatePermissionState(currentType) { state ->
                state.copy(
                    isGranted = true,
                    status = UiKitPermissionStatus.Granted,
                    justDenied = false,
                )
            }

            moveToNextStep()
            return
        }

        val canPromptRationale =
            currentType.androidPermission?.let {
                activity.shouldShowRequestPermissionRationale(it)
            } ?: false

        val newStatus =
            when {
                canPromptRationale -> {
                    UiKitPermissionStatus.DeniedCanRequestAgain
                }

                else -> {
                    UiKitPermissionStatus.DeniedAlways
                }
            }

        val shouldExit =
            when (currentState.requestSource) {
                RequestSource.Probe -> {
                    canPromptRationale
                }

                RequestSource.InFlow -> {
                    true
                }
            }

        if (currentState.requestSource == RequestSource.Probe) {
            markProbeCompleted(currentType)
        }

        updatePermissionState(currentType) { state ->
            state.copy(
                isGranted = false,
                status = newStatus,
                probeCompleted = true,
                requestSource = RequestSource.InFlow,
                justDenied = shouldExit,
            )
        }

        if (shouldExit) {
            _shouldExit.value = true
        } else {
            moveToNextStep()
        }
    }

    private fun markProbeCompleted(type: PermissionType) {
        when (type) {
            PermissionType.ACTIVITY_RECOGNITION ->
                prefs.activityRecognitionProbeCompleted = true

            PermissionType.POST_NOTIFICATIONS ->
                prefs.postNotificationsProbeCompleted = true

            else -> {}
        }
    }

    fun setExplanationShown() {
        prefs.permissionExplanationScreenShown = true
        moveToNextStep()
    }

    fun markGoingToSettings() {
        _returningFromSettings.value = true
    }

    fun checkPermissionsAfterSettings(activity: Activity) {
        val currentType = _currentPermissionType.value

        _returningFromSettings.value = false

        if (currentType == null) {
            return
        }

        val isNowGranted = currentType.isGranted(activity)

        if (isNowGranted) {
            updatePermissionState(currentType) { state ->
                state.copy(
                    isGranted = true,
                    status = UiKitPermissionStatus.Granted,
                    justDenied = false,
                )
            }

            moveToNextStep()
        }
    }

    private fun moveToNextStep() {
        val currentType = _currentPermissionType.value
        val currentState = currentType?.let { _permissionStates.value[it] }

        if (currentState?.justDenied == true) {
            _shouldExit.value = true
            return
        }

        val stage =
            if (showPermissionExplanationScreen && !prefs.permissionExplanationScreenShown) {
                PermissionNavigator.Stage.FirstEntry
            } else {
                PermissionNavigator.Stage.ReentryNotFirst
            }

        val next =
            PermissionNavigator.decideMultiPermission(
                stage = stage,
                currentPermissionType = currentType,
                permissionStates = _permissionStates.value,
                showExplanationFirst = showPermissionExplanationScreen && !prefs.permissionExplanationScreenShown,
            )

        when (next) {
            is PermissionNavigator.NextAction.ShowExplanation -> {
                _currentScreen.value = ExplanationScreenDestination
                analyticsTracker.trackDataSafetyScreen(mode.toAnalyticsFlowName())
            }

            is PermissionNavigator.NextAction.ShowPermissionPrompt -> {
                _currentScreen.value = getRequestDestination(next.permissionType, mode)
                trackPermissionScreenShown(next.permissionType)
            }

            is PermissionNavigator.NextAction.ShowSettingsRedirect -> {
                _currentScreen.value = getSettingsDestination(next.permissionType, mode)
                trackPermissionScreenShown(next.permissionType)
            }

            is PermissionNavigator.NextAction.MoveToNextPermission -> {
                _currentPermissionType.value = next.permissionType
                moveToNextStep()
            }

            PermissionNavigator.NextAction.Exit -> {
                _shouldExit.value = true
            }
        }
    }

    private fun getRequestDestination(
        type: PermissionType,
        permissionMode: OSTPermissionMode,
    ): PermissionDestinations =
        when (type) {
            PermissionType.ACTIVITY_RECOGNITION -> ARRequestScreenDestination
            PermissionType.POST_NOTIFICATIONS -> NotificationRequestScreenDestination(permissionMode)
            PermissionType.BATTERY_OPTIMIZATION -> BatteryOptimizationRequestScreenDestination
            else -> ARRequestScreenDestination
        }

    private fun getSettingsDestination(
        type: PermissionType,
        permissionMode: OSTPermissionMode,
    ): PermissionDestinations =
        when (type) {
            PermissionType.ACTIVITY_RECOGNITION -> ARSettingsRedirectDestination
            PermissionType.POST_NOTIFICATIONS ->
                NotificationSettingsRedirectDestination(
                    permissionMode,
                )

            PermissionType.BATTERY_OPTIMIZATION -> BatteryOptimizationRequestScreenDestination
            else -> ARSettingsRedirectDestination
        }

    private fun trackPermissionScreenShown(permissionType: PermissionType) {
        val permissionName = permissionType.toAnalyticsPermissionName() ?: return
        val state = _permissionStates.value[permissionType]
        val variant =
            determineVariant(
                state?.probeCompleted ?: false,
                state?.isGranted ?: false,
            )
        analyticsTracker.trackPermissionRequestScreen(
            permissionName,
            variant,
            mode.toAnalyticsFlowName(),
        )
    }

    // F1: shared preamble extracted from the four track*Click methods
    private inline fun trackAction(crossinline action: PermissionAnalyticsTracker.(String, String, String) -> Unit) {
        val currentType = _currentPermissionType.value ?: return
        val permissionName = currentType.toAnalyticsPermissionName() ?: return
        val state = _permissionStates.value[currentType]
        val variant = determineVariant(
            state?.probeCompleted ?: false,
            state?.isGranted ?: false,
        )
        analyticsTracker.action(permissionName, variant, mode.toAnalyticsFlowName())
    }

    fun trackAllowButtonClick() = trackAction { p, v, f -> trackAllowClicked(p, v, f) }

    fun trackGoToSettingsButtonClick() = trackAction { p, v, f -> trackGoToSettingsClicked(p, v, f) }

    fun trackCloseButtonClick() = trackAction { p, v, f -> trackPermissionCloseClicked(p, v, f) }

    fun trackDataUsageInfoClick() = trackAction { p, v, f -> trackHowIsMyDataUsedClicked(p, v, f) }

    companion object {
        const val TAG = "PermissionWizardViewModel"
    }
}
