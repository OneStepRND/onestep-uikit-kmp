package co.onestep.kmp.uikit.features.permissions

/**
 * Permission flow navigation logic.
 * Screens: Explanation -> Physical Activity Permission -> Notification Permission -> Settings/Exit
 */
object PermissionNavigator {

    enum class Stage {
        /** User enters the activity for the first time */
        FirstEntry,

        /** User navigated into the permission activity again (not first time) */
        ReentryNotFirst,
    }

    /**
     * Enhanced navigation action for multi-permission flows.
     * Includes permission-specific actions and sequencing support.
     */
    sealed interface NextAction {
        /** Finish the activity */
        data object Exit : NextAction

        /** Show explanation screen */
        data object ShowExplanation : NextAction

        /** Show the system permission dialog screen for the specified permission */
        data class ShowPermissionPrompt(
            val permissionType: PermissionType,
        ) : NextAction

        /** Show the settings redirect screen for the specified permission */
        data class ShowSettingsRedirect(
            val permissionType: PermissionType,
        ) : NextAction

        /** Move to the next permission in the sequence */
        data class MoveToNextPermission(
            val permissionType: PermissionType,
        ) : NextAction
    }

    /**
     * State of a permission in the flow.
     */
    data class PermissionState(
        val type: PermissionType,
        val isGranted: Boolean,
        val status: UiKitPermissionStatus,
        val probeCompleted: Boolean,
        val requestSource: RequestSource,
        val justDenied: Boolean,
    )

    /**
     * Enhanced decision logic for multi-permission sequential flows.
     *
     * @param stage Where we are in the flow (FirstEntry or ReentryNotFirst)
     * @param currentPermissionType The current permission being processed, or null to determine first permission
     * @param permissionStates Map of all permissions and their states
     * @param showExplanationFirst If true, show explanation screen on first entry
     * @return NextAction indicating the next step in the flow
     */
    fun decideMultiPermission(
        stage: Stage,
        currentPermissionType: PermissionType?,
        permissionStates: Map<PermissionType, PermissionState>,
        showExplanationFirst: Boolean = false,
    ): NextAction {
        // If no current permission, find first ungranted permission
        if (currentPermissionType == null) {
            val firstUngranted =
                permissionStates.entries
                    .firstOrNull { !it.value.isGranted }
                    ?.key

            return if (firstUngranted != null) {
                NextAction.ShowPermissionPrompt(firstUngranted)
            } else {
                NextAction.Exit
            }
        }

        val currentState = permissionStates[currentPermissionType] ?: return NextAction.Exit

        // If just denied, exit immediately
        if (currentState.justDenied) {
            return NextAction.Exit
        }

        // Show explanation screen only on first entry if requested
        if (stage == Stage.FirstEntry && showExplanationFirst) {
            return NextAction.ShowExplanation
        }

        // If current permission is granted, move to next ungranted permission
        if (currentState.isGranted) {
            val entries = permissionStates.entries.toList()
            val currentIndex = entries.indexOfFirst { it.key == currentPermissionType }

            val nextUngranted =
                entries
                    .drop(currentIndex + 1)
                    .firstOrNull { !it.value.isGranted }
                    ?.key

            return if (nextUngranted != null) {
                NextAction.MoveToNextPermission(nextUngranted)
            } else {
                NextAction.Exit
            }
        }

        val mustGoToSettings =
            when (currentPermissionType) {
                PermissionType.BATTERY_OPTIMIZATION -> false // can always be requested
                else -> {
                    // Use explicit status instead of inferred logic
                    currentState.status == UiKitPermissionStatus.DeniedAlways
                }
            }

        return if (!mustGoToSettings) {
            NextAction.ShowPermissionPrompt(currentPermissionType)
        } else {
            NextAction.ShowSettingsRedirect(currentPermissionType)
        }
    }
}
