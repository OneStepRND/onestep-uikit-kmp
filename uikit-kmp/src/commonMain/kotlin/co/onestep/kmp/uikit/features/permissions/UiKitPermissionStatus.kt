package co.onestep.kmp.uikit.features.permissions

/**
 * Represents the status of a permission in the UIKit permission flow.
 *
 * This enum implements a "probe once" strategy to handle the platform's ambiguous permission state.
 * On Android, shouldShowRequestPermissionRationale returns false in two cases:
 * 1. The app has never requested the permission (dialog will show)
 * 2. The user selected "Don't ask again" (dialog won't show, must redirect to Settings)
 *
 * Since we cannot distinguish between these cases without persisted state, we use a probe request:
 * - Initial state when !granted && !shouldShow → [UnknownCouldRequest]
 * - Make one permission request (the "probe")
 * - After the probe, if still !granted && !shouldShow → [DeniedAlways]
 *
 * This eliminates the need for preference-based bookkeeping while accurately determining
 * whether to show the permission dialog or redirect to Settings.
 */
enum class UiKitPermissionStatus {
    /**
     * Permission is granted by the user.
     */
    Granted,

    /**
     * Permission is not granted and shouldShowRequestPermissionRationale
     * returns false. This is ambiguous and could mean:
     * - We've never asked for this permission before (system dialog will show)
     * - User previously selected "Don't ask again" (must redirect to Settings)
     *
     * We resolve this ambiguity by making a single probe request. After the request,
     * the status will transition to either [Granted], [DeniedCanRequestAgain], or [DeniedAlways].
     */
    UnknownCouldRequest,

    /**
     * Permission was denied but shouldShowRequestPermissionRationale
     * returns true, meaning we can request the permission again and the system dialog will show.
     */
    DeniedCanRequestAgain,

    /**
     * Permission was denied and shouldShowRequestPermissionRationale
     * returns false AFTER we've made at least one request (probe). This definitively means
     * the user selected "Don't ask again" and we must redirect to Settings.
     */
    DeniedAlways,

    ;

    companion object {
        /**
         * Derive permission status from platform APIs without persistent state.
         *
         * @param isGranted Current permission grant status
         * @param shouldShowRationale Platform's shouldShowRequestPermissionRationale result
         * @param probeCompleted Whether we've already probed this permission in current session
         * @return The appropriate UiKitPermissionStatus
         */
        fun fromAndroidState(
            isGranted: Boolean,
            shouldShowRationale: Boolean,
            probeCompleted: Boolean,
        ): UiKitPermissionStatus =
            when {
                isGranted -> Granted
                shouldShowRationale -> DeniedCanRequestAgain
                probeCompleted -> DeniedAlways // After probe: no dialog = permanent denial
                else -> UnknownCouldRequest // Before probe: ambiguous state
            }
    }
}
