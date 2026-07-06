package co.onestep.kmp.uikit.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.rememberLiquidState

/**
 * iOS: the native "Liquid Glass" chip — a near-white frosted disc that floats on the bar with a
 * soft drop shadow, with the `io.github.fletchmckee.liquid` lens (Skia `RuntimeEffect`) on top.
 *
 * The disc/shadow sit on their own layer that is NOT clipped (so the shadow shows). The lens +
 * clickable sit on a CircleShape-clipped layer, so the press indication (the iOS dim from
 * [osClickIndication]) is a circle, not a square.
 */
@Composable
actual fun OSTLiquidGlassCircle(
    onClick: () -> Unit,
    modifier: Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val state = rememberLiquidState()
    Box(modifier, contentAlignment = Alignment.Center) {
        // Soft drop shadow (drawn BEFORE liquefiable so it stays a true outer shadow, not sampled)
        // + near-white frosted disc (AFTER liquefiable so the lens samples it). NOT clipped.
        Box(
            Modifier
                .matchParentSize()
                .shadow(elevation = 6.dp, shape = CircleShape, clip = false)
                .liquefiable(state)
                .background(Color.White.copy(alpha = 0.85f), CircleShape),
        )
        // Lens + circular click layer. clip(CircleShape) keeps the dim press indication circular.
        Box(
            Modifier
                .matchParentSize()
                .clip(CircleShape)
                .liquid(state) {
                    shape = CircleShape
                    frost = 6.dp
                    refraction = 0.4f
                    curve = 0.4f
                    edge = 0.12f
                    tint = Color.White.copy(alpha = 0.25f)
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = osClickIndication(bounded = false),
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
            content = content,
        )
    }
}
