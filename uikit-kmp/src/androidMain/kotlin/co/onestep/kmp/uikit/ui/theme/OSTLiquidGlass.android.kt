package co.onestep.kmp.uikit.ui.theme

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

/**
 * Android: liquid glass is an iOS-only treatment, so there is no chip — just the centered icon with
 * the app's standard circular press indication. No shader/library is referenced here.
 */
@Composable
actual fun OSTLiquidGlassCircle(
    onClick: () -> Unit,
    modifier: Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = osClickIndication(bounded = false),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
        content = content,
    )
}
