package co.onestep.kmp.uikit.navigation

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay

/**
 * Drop-in [NavDisplay] styled after UINavigationController push/pop: parallax underlay, scrim,
 * leading-edge shadow, ~350ms Cupertino curve (see [CupertinoTransition] for the full spec and
 * tuning constants), mirrored automatically for RTL layouts.
 *
 * Back navigation is interactive on every platform: on Android the system (predictive) back
 * gesture seeks the pop transition; on iOS a Compose edge swipe ([cupertinoEdgeSwipeBack]) feeds
 * the same predictive-back channel, so the drag and the programmatic pop can never diverge.
 * Guarded screens keep working — a back handler registered via
 * `androidx.navigationevent.compose.NavigationBackHandler` (or the uikit `PlatformBackHandler`)
 * intercepts the swipe before it pops.
 *
 * This is the consumer-facing entry point for apps migrating flows to Navigation 3 — same
 * parameters as a plain [NavDisplay], no other uikit coupling:
 *
 * ```
 * val backStack = rememberNavBackStack<AppDestination>(Home)
 * CupertinoNavDisplay(
 *     backStack = backStack,
 *     onBack = { backStack.removeLastOrNull() },
 *     screenBackground = AppTheme.colors.background,
 *     entryProvider = entryProvider { ... },
 * )
 * ```
 *
 * @param backStack the Navigation 3 back stack; never empty.
 * @param onBack invoked for system back / a committed edge swipe — pop your back stack here.
 * @param interactiveBackGesture opt-in/out override for the Compose edge-swipe gesture. Defaults
 *   to on only where the platform provides no back gesture of its own (i.e. on iOS).
 * @param screenBackground opaque backdrop painted behind every screen. The Cupertino parallax
 *   shows the underlying screen through anything transparent, so each entry must be opaque on its
 *   own — pass your theme's screen background if your screens rely on a backdrop painted outside
 *   the NavDisplay, or [Color.Unspecified] (default) if they are already fully opaque.
 * @param entryDecorators forwarded to [NavDisplay] — add e.g. a ViewModel store decorator here.
 * @param entryProvider maps a key to its [NavEntry]; entries are rewrapped to carry the backdrop
 *   and the leading-edge shadow.
 */
@Composable
public fun <T : Any> CupertinoNavDisplay(
    backStack: List<T>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    interactiveBackGesture: Boolean = !platformProvidesBackGesture,
    screenBackground: Color = Color.Unspecified,
    entryDecorators: List<NavEntryDecorator<T>> =
        listOf(rememberSaveableStateHolderNavEntryDecorator()),
    entryProvider: (T) -> NavEntry<T>,
) {
    // slideInHorizontally is not layout-direction aware, so mirror the offsets manually for RTL.
    val dir = if (LocalLayoutDirection.current == LayoutDirection.Ltr) 1 else -1
    NavDisplay(
        backStack = backStack,
        modifier = modifier.cupertinoEdgeSwipeBack(
            enabled = interactiveBackGesture && backStack.size > 1,
        ),
        onBack = { onBack() },
        entryDecorators = entryDecorators,
        transitionSpec = cupertinoPushSpec(dir),
        popTransitionSpec = cupertinoPopSpec(dir),
        predictivePopTransitionSpec = cupertinoPredictivePopSpec(),
        entryProvider = { key ->
            val entry = entryProvider(key)
            // Rewrap so every screen is opaque and carries the Cupertino leading-edge shadow.
            NavEntry(navEntry = entry) {
                Box(
                    Modifier
                        .cupertinoEdgeShadow()
                        .then(
                            if (screenBackground.isSpecified) {
                                Modifier.background(screenBackground)
                            } else {
                                Modifier
                            },
                        ),
                ) { entry.Content() }
            }
        },
    )
}
