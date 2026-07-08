package co.onestep.kmp.uikit.navigation

/**
 * Android delivers back gestures (3-button back, gesture nav, predictive back) through the
 * activity's NavigationEventDispatcher already — the Compose edge swipe stays off so the two
 * never compete for the same edge.
 */
internal actual val platformProvidesBackGesture: Boolean = true
