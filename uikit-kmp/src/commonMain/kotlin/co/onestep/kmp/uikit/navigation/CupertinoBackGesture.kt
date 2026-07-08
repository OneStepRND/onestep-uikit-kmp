package co.onestep.kmp.uikit.navigation

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import kotlinx.coroutines.CancellationException

/**
 * Whether the platform already provides an interactive back gesture that reaches
 * [androidx.navigation3.ui.NavDisplay] (Android's system back / predictive back). When it does,
 * the Compose-implemented edge swipe stays off by default so the two never compete.
 */
internal expect val platformProvidesBackGesture: Boolean

/**
 * iOS-style interactive swipe-back: a horizontal drag starting within
 * [CupertinoTransition.EdgeSwipeWidth] of the leading screen edge (left in LTR, right in RTL) is
 * translated into predictive-back [NavigationEvent]s on the [NavigationEventDispatcher] provided
 * by [LocalNavigationEventDispatcherOwner] — the same channel Android's system predictive back
 * uses. `NavDisplay` reacts by *seeking* its pop transition, so the drag reuses the exact
 * [cupertinoPredictivePopSpec] visuals (offsets, parallax, scrim, shadow); there is no separate
 * offset math for the gesture.
 *
 * Drag progress mapping: `progress = dragDistanceTowardTrailingEdge / containerWidth`, clamped to
 * `[0, 1]`; `0` = top screen fully covering, `1` = fully dismissed.
 *
 * On release the pop is committed when `progress > DISMISS_PROGRESS_THRESHOLD` **or** the fling
 * velocity toward dismissal exceeds [CupertinoTransition.DismissVelocityThreshold]; otherwise a
 * cancel event lets NavDisplay animate back to rest. An interrupted gesture (e.g. a child consumed
 * the pointer, or this node left composition mid-drag) dispatches a cancel as well.
 *
 * Apply to the NavDisplay container. Touches are only claimed after horizontal slop toward the
 * trailing edge, so taps and vertical scrolls near the edge keep working.
 */
@Composable
internal fun Modifier.cupertinoEdgeSwipeBack(enabled: Boolean): Modifier {
    if (!enabled) return this
    val dispatcher =
        LocalNavigationEventDispatcherOwner.current?.navigationEventDispatcher ?: return this
    val input = remember(dispatcher) { DirectNavigationEventInput() }
    DisposableEffect(dispatcher, input) {
        dispatcher.addInput(input)
        onDispose { dispatcher.removeInput(input) }
    }
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    return pointerInput(input, isRtl) {
        val edgeWidthPx = CupertinoTransition.EdgeSwipeWidth.toPx()
        val velocityThresholdPx = CupertinoTransition.DismissVelocityThreshold.toPx()
        // +1 when dismissal moves content toward +x (LTR), -1 in RTL.
        val dismissSign = if (isRtl) -1f else 1f
        val swipeEdge = if (isRtl) NavigationEvent.EDGE_RIGHT else NavigationEvent.EDGE_LEFT

        awaitEachGesture {
            val down = awaitFirstDown()
            val inEdgeZone =
                if (isRtl) down.position.x >= size.width - edgeWidthPx
                else down.position.x <= edgeWidthPx
            if (!inEdgeZone) return@awaitEachGesture

            // Claim the pointer only once it moves past touch slop *toward* dismissal, so edge
            // taps and vertical scrolls are unaffected.
            var claimed = false
            val firstDrag = awaitHorizontalTouchSlopOrCancellation(down.id) { change, overSlop ->
                if (overSlop * dismissSign > 0f) {
                    claimed = true
                    change.consume()
                }
            }
            if (firstDrag == null || !claimed) return@awaitEachGesture

            input.backStarted(
                NavigationEvent(
                    swipeEdge = swipeEdge,
                    progress = 0f,
                    touchX = down.position.x,
                    touchY = down.position.y,
                ),
            )
            val velocityTracker = VelocityTracker()
            var progress = 0f
            var settled = false
            try {
                val releasedNormally = horizontalDrag(firstDrag.id) { change ->
                    velocityTracker.addPointerInputChange(change)
                    // Drag distance toward the trailing edge, as a fraction of container width.
                    val towardDismiss = (change.position.x - down.position.x) * dismissSign
                    progress = (towardDismiss / size.width).coerceIn(0f, 1f)
                    input.backProgressed(
                        NavigationEvent(
                            swipeEdge = swipeEdge,
                            progress = progress,
                            touchX = change.position.x,
                            touchY = change.position.y,
                        ),
                    )
                    change.consume()
                }
                val flingVelocity = velocityTracker.calculateVelocity().x * dismissSign
                val commit = releasedNormally &&
                    (progress > CupertinoTransition.DISMISS_PROGRESS_THRESHOLD ||
                        flingVelocity > velocityThresholdPx)
                settled = true
                if (commit) input.backCompleted() else input.backCancelled()
            } catch (e: CancellationException) {
                // Gesture torn down mid-drag (composition left, pointer stolen) — settle cleanly.
                if (!settled) input.backCancelled()
                throw e
            }
        }
    }
}
