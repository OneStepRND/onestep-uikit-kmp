package co.onestep.kmp.uikit.features.permissions

/**
 * Represents the source/intent of a permission request.
 *
 * This distinction is critical for handling the product requirement:
 * - User denials IN OUR FLOW → exit immediately
 * - Pre-existing "don't ask again" from outside → show Settings redirect
 */
enum class RequestSource {
    /**
     * Probe request used to disambiguate [UiKitPermissionStatus.UnknownCouldRequest].
     *
     * When status is UnknownCouldRequest (!granted && !shouldShow), we can't tell if:
     * - This is the first time requesting (dialog will show)
     * - User already selected "Don't ask again" (no dialog)
     *
     * A probe request resolves this:
     * - If dialog shows and user denies → treat as in-flow denial, exit immediately
     * - If no dialog shows (already "don't ask again") → NOT a denial in our flow, show Settings
     */
    Probe,

    /**
     * Regular in-flow permission request.
     *
     * Used when status is [UiKitPermissionStatus.DeniedCanRequestAgain] or when
     * user explicitly retries after seeing Settings redirect.
     *
     * Any denial from an InFlow request counts as "user denied in our flow" → exit immediately.
     */
    InFlow,
}
