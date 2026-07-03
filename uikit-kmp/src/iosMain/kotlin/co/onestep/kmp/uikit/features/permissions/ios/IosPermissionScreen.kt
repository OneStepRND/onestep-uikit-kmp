package co.onestep.kmp.uikit.features.permissions.ios

/**
 * Screens in the iOS permission flow.
 */
internal sealed class IosPermissionScreen {
    /** Rationalization screen explaining why permissions are needed. */
    data object Rationalization : IosPermissionScreen()

    /** Motion/Fitness permission screen. */
    data class Motion(val showSettings: Boolean = false) : IosPermissionScreen()

    /** Location permission screen with phase indicator. */
    data class Location(
        val phase: LocationPhase,
        val showSettings: Boolean = false,
        val isDowngraded: Boolean = false,
    ) : IosPermissionScreen()

    /** HealthKit permission screen. */
    data class HealthKit(val showSettings: Boolean = false) : IosPermissionScreen()

    /** Flow completed — all permissions handled. */
    data object Completed : IosPermissionScreen()
}

/**
 * Phases for location permission requests.
 */
internal enum class LocationPhase {
    /** Request "While Using" location access. */
    WHILE_USING,
    /** Request "Always" location access (requires While Using first). */
    ALWAYS,
}
