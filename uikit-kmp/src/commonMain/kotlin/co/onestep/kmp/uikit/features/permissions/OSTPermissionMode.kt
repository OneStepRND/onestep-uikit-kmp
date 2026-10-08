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
 * - [HEALTH_KIT]: deprecated no-op — HealthKit support was removed from uikit-kmp
 *   (linking platform.HealthKit put HK API references into every consumer's binary, which
 *   App Review flags under guideline 2.5.1 in apps with no HealthKit feature). The mode is
 *   kept for API compatibility and completes immediately on both platforms.
 *
 * - [FULL]: iOS only — requests Motion/Fitness and Location Always (HealthKit removed, see
 *   [HEALTH_KIT]). Returns an empty sequence on Android.
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
     * Deprecated no-op: HealthKit support was removed from uikit-kmp. The mode completes
     * immediately on both platforms; kept only so existing callers keep compiling.
     */
    HEALTH_KIT,

    /**
     * Full mode (iOS only): Requests Motion/Fitness → Location (Always).
     * On Android, this mode produces an empty permission sequence.
     */
    FULL,
}
