package co.onestep.kmp.uikit.features.permissions

/**
 * Defines the mode of permission flow.
 *
 * - [IN_APP]: Requests only permissions needed for in-app measurements
 *   (ACTIVITY_RECOGNITION and POST_NOTIFICATIONS on Android; Motion/Fitness and Location on iOS)
 *
 * - [BACKGROUND]: Requests all permissions needed for background monitoring
 *   (ACTIVITY_RECOGNITION, POST_NOTIFICATIONS, and Battery Optimization on Android; Motion/Fitness and Location Always on iOS)
 *
 * - [HEALTH_KIT]: iOS only — requests HealthKit data access permissions.
 *   Returns an empty sequence on Android.
 *
 * - [FULL]: iOS only — requests Motion/Fitness, Location Always, and HealthKit.
 *   Returns an empty sequence on Android.
 */
enum class OSTPermissionMode {
    /**
     * In-app mode: Requests ACTIVITY_RECOGNITION → POST_NOTIFICATIONS (in that order)
     */
    IN_APP,

    /**
     * Background mode: Requests ACTIVITY_RECOGNITION → POST_NOTIFICATIONS → Battery Optimization (in that order)
     */
    BACKGROUND,

    /**
     * HealthKit mode (iOS only): Requests HealthKit data access permissions.
     * On Android, this mode produces an empty permission sequence.
     */
    HEALTH_KIT,

    /**
     * Full mode (iOS only): Requests Motion/Fitness → Location (Always) → HealthKit.
     * On Android, this mode produces an empty permission sequence.
     */
    FULL,
}
