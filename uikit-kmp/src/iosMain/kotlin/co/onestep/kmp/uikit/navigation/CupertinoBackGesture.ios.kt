package co.onestep.kmp.uikit.navigation

/**
 * A pure-Compose screen on iOS has no UINavigationController, so nothing feeds back gestures into
 * the NavigationEventDispatcher — the Compose edge swipe provides them.
 */
internal actual val platformProvidesBackGesture: Boolean = false
