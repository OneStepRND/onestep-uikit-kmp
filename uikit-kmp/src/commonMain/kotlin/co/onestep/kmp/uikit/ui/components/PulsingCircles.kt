package co.onestep.kmp.uikit.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun PulsingCircles(
    modifier: Modifier = Modifier,
    baseSize: Dp,
) {
    val infiniteTransition = rememberInfiniteTransition()

    val scale1 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 9000
                0f at 0
                0.5f at 4500
                0f at 9000
            },
            repeatMode = RepeatMode.Restart,
        ),
    )

    val scale2 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 6000
                0f at 0
                0.5f at 3000
                0f at 6000
            },
            repeatMode = RepeatMode.Restart,
        ),
    )

    val scale3 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )
    val alpha3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                0f at 0
                0.5f at 1500
                0f at 3000
            },
            repeatMode = RepeatMode.Restart,
        ),
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        val grad1 = LocalOSColors.current.primary_0
        val grad2 = LocalOSColors.current.primary_m1
        val grad3 = LocalOSColors.current.primary_m2

        Canvas(modifier = Modifier.fillMaxSize()) {
            val baseRadius = size.minDimension / 2f
            val gradientColors = listOf(grad1, grad2, grad3)
            drawCircle(
                brush = Brush.radialGradient(colors = gradientColors, center = center, radius = baseRadius),
                radius = baseRadius * scale1,
                center = center,
                alpha = alpha1,
            )
            drawCircle(
                brush = Brush.radialGradient(colors = gradientColors, center = center, radius = baseRadius),
                radius = baseRadius * scale2,
                center = center,
                alpha = alpha2,
            )
            drawCircle(
                brush = Brush.radialGradient(colors = gradientColors, center = center, radius = baseRadius),
                radius = baseRadius * scale3,
                center = center,
                alpha = alpha3,
            )
        }
    }
}

@Preview
@Composable
private fun PulsingCirclesPreview() {
    PreviewTheme {
        PulsingCircles(baseSize = 200.dp)
    }
}
