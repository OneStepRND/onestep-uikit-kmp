package co.onestep.kmp.uikit.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.unveilIn
import androidx.compose.animation.veilOut
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation3.scene.Scene
import androidx.navigationevent.NavigationEvent
import kotlin.math.roundToInt

/**
 * iOS `UINavigationController`-style push/pop transition for [androidx.navigation3.ui.NavDisplay],
 * implemented purely in Compose (no UIKit interop).
 *
 * ## The shared "progress -> visual state" contract
 *
 * Everything below is a function of a single normalized progress `p` where `p = 0` means the top
 * screen fully covers the stack and `p = 1` means it is fully dismissed (pop finished):
 *
 * - top screen offset: `p * width` toward the trailing edge
 * - underlying screen offset: `-UNDERLAY_PARALLAX * (1 - p) * width` (moves slower — parallax)
 * - scrim over the underlying screen: `SCRIM_MAX_ALPHA * (1 - p)` black
 * - a soft gradient shadow on the top screen's leading edge (drawn just outside its bounds by
 *   [cupertinoEdgeShadow], so it only becomes visible while the screen is displaced)
 *
 * There is deliberately **one** implementation of this mapping: the [ContentTransform] factories
 * below. A programmatic push/pop plays them on a clock ([Easing], [DURATION_MILLIS]); the
 * interactive edge swipe ([cupertinoEdgeSwipeBack]) drives the *same* transforms by seeking
 * NavDisplay's internal `SeekableTransitionState` through `NavigationEvent.progress`, so the drag
 * and the animation can never diverge. The predictive (gesture) variant uses [LinearEasing] so the
 * content tracks the finger 1:1; the timed variants use the Cupertino curve.
 *
 * All magic numbers live in [CupertinoTransition].
 */
internal object CupertinoTransition {

    /** Duration of a programmatic push/pop, matching UIKit's ~0.35s. */
    const val DURATION_MILLIS = 350

    /** CSS `ease` / CupertinoTiming curve: cubic-bezier(0.25, 0.1, 0.25, 1.0). */
    val Easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)

    /** Fraction of its width the underlying screen slides while (un)covered. */
    const val UNDERLAY_PARALLAX = 0.3f

    /** Peak alpha of the black scrim dimming the underlying screen. */
    const val SCRIM_MAX_ALPHA = 0.1f

    /** Width of the touch zone at the leading screen edge that starts a back swipe. */
    val EdgeSwipeWidth = 20.dp

    /** Release a drag past this progress -> commit the pop; otherwise spring back. */
    const val DISMISS_PROGRESS_THRESHOLD = 0.5f

    /** Fling velocity (per second, toward dismissal) that commits the pop regardless of progress. */
    val DismissVelocityThreshold = 300.dp

    /** Width of the soft shadow gradient on the top screen's leading edge. */
    val EdgeShadowWidth = 16.dp

    /** Alpha of the edge shadow where it touches the screen edge (fades to 0 outward). */
    const val EDGE_SHADOW_MAX_ALPHA = 0.15f

    val ScrimColor = Color.Black.copy(alpha = SCRIM_MAX_ALPHA)
}

private fun cupertinoOffsetSpec() =
    tween<androidx.compose.ui.unit.IntOffset>(
        CupertinoTransition.DURATION_MILLIS,
        easing = CupertinoTransition.Easing,
    )

private fun cupertinoColorSpec() =
    tween<Color>(CupertinoTransition.DURATION_MILLIS, easing = CupertinoTransition.Easing)

/**
 * Push: incoming screen slides in from the trailing edge (100% -> 0% of its width) while the
 * screen underneath slides to `-30%` of its width behind a fading-in scrim.
 *
 * Receiver-free so it can also drive plain [androidx.compose.animation.AnimatedContent]s (e.g.
 * the recording screen's stage changes), not just NavDisplay.
 *
 * @param dir `1` for LTR, `-1` for RTL — mirrors all horizontal motion.
 */
@OptIn(ExperimentalAnimationApi::class)
internal fun cupertinoPushTransform(dir: Int): ContentTransform =
    slideInHorizontally(cupertinoOffsetSpec()) { it * dir } togetherWith
        slideOutHorizontally(cupertinoOffsetSpec()) {
            -(it * CupertinoTransition.UNDERLAY_PARALLAX).roundToInt() * dir
        } + veilOut(cupertinoColorSpec(), CupertinoTransition.ScrimColor)

/** Pop: the exact reverse of [cupertinoPushTransform]. */
@OptIn(ExperimentalAnimationApi::class)
internal fun cupertinoPopTransform(dir: Int): ContentTransform =
    slideInHorizontally(cupertinoOffsetSpec()) {
        -(it * CupertinoTransition.UNDERLAY_PARALLAX).roundToInt() * dir
    } + unveilIn(cupertinoColorSpec(), CupertinoTransition.ScrimColor) togetherWith
        slideOutHorizontally(cupertinoOffsetSpec()) { it * dir }

/** [cupertinoPushTransform] as a NavDisplay transition spec. */
internal fun <T : Any> cupertinoPushSpec(
    dir: Int,
): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    cupertinoPushTransform(dir)
}

/** [cupertinoPopTransform] as a NavDisplay pop transition spec. */
internal fun <T : Any> cupertinoPopSpec(
    dir: Int,
): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    cupertinoPopTransform(dir)
}

/**
 * Pop driven by a back gesture (our edge swipe, or Android predictive back). Identical geometry to
 * [cupertinoPopSpec], but with [LinearEasing] so that while NavDisplay *seeks* this transition from
 * `NavigationEvent.progress`, the content tracks the finger 1:1. Direction follows the physical
 * swipe edge (right-edge swipes in RTL mirror automatically).
 */
@OptIn(ExperimentalAnimationApi::class)
internal fun <T : Any> cupertinoPredictivePopSpec():
    AnimatedContentTransitionScope<Scene<T>>.(Int) -> ContentTransform = { edge ->
    val dir = if (edge == NavigationEvent.EDGE_RIGHT) -1 else 1
    val offsetSpec = tween<androidx.compose.ui.unit.IntOffset>(
        CupertinoTransition.DURATION_MILLIS,
        easing = LinearEasing,
    )
    val colorSpec = tween<Color>(CupertinoTransition.DURATION_MILLIS, easing = LinearEasing)
    slideInHorizontally(offsetSpec) {
        -(it * CupertinoTransition.UNDERLAY_PARALLAX).roundToInt() * dir
    } + unveilIn(colorSpec, CupertinoTransition.ScrimColor) togetherWith
        slideOutHorizontally(offsetSpec) { it * dir }
}

/**
 * Soft shadow on the leading edge of a screen, drawn in the `EdgeShadowWidth` strip *outside* the
 * screen's leading bound. At rest that strip is off-screen, so the shadow costs nothing visually;
 * while the screen is displaced toward the trailing edge (push-in, pop-out, or drag) the strip
 * rides along and falls over the underlying screen — no transition-progress plumbing needed.
 * Mirrored automatically for RTL via [androidx.compose.ui.graphics.drawscope.DrawScope]'s
 * layoutDirection.
 */
internal fun Modifier.cupertinoEdgeShadow(): Modifier = drawBehind {
    val shadowWidth = CupertinoTransition.EdgeShadowWidth.toPx()
    val edgeColor = Color.Black.copy(alpha = CupertinoTransition.EDGE_SHADOW_MAX_ALPHA)
    val ltr = layoutDirection == LayoutDirection.Ltr
    val left = if (ltr) -shadowWidth else size.width
    drawRect(
        brush = Brush.horizontalGradient(
            colors = if (ltr) listOf(Color.Transparent, edgeColor) else listOf(edgeColor, Color.Transparent),
            startX = left,
            endX = left + shadowWidth,
        ),
        topLeft = Offset(left, 0f),
        size = Size(shadowWidth, size.height),
    )
}
