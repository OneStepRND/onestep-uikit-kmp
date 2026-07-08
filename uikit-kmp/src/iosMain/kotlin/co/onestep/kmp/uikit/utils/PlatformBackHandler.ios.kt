package co.onestep.kmp.uikit.utils

import androidx.compose.runtime.Composable
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState

/**
 * iOS has no system back button, but back events do arrive through the
 * NavigationEventDispatcher — the Compose edge-swipe gesture
 * ([co.onestep.kmp.uikit.navigation.cupertinoEdgeSwipeBack]) dispatches there. Registering a
 * handler here mirrors Android's [androidx.activity.compose.BackHandler] semantics: because a
 * screen's handler is composed after (inside) NavDisplay's own back handler, it takes precedence,
 * so guarded screens (e.g. the recording screen's stop-measurement confirmation) intercept the
 * swipe instead of being popped underneath their own state.
 */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No dispatcher owner (e.g. previews, hosts without navigation event support) — nothing to
    // intercept, and NavigationBackHandler would throw.
    LocalNavigationEventDispatcherOwner.current ?: return
    val state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None)
    NavigationBackHandler(
        state = state,
        isBackEnabled = enabled,
        onBackCompleted = onBack,
    )
}
