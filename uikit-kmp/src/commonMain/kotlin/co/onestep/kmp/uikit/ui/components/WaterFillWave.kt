package co.onestep.kmp.uikit.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WaterFillWave(
    modifier: Modifier = Modifier,
    percent: Float,
    color: Color,
    isFlat: Boolean = false,
) {
    val amplitudeDp = 24.dp
    val targetAmplitude = if (isFlat || percent >= 100f) 0f else amplitudeDp.value

    val amplitudePx by animateFloatAsState(
        targetValue = targetAmplitude,
        animationSpec = tween(600),
        label = "waveAmplitude",
    )

    val phase by if (!isFlat && amplitudePx > 0f) {
        rememberInfiniteTransition(label = "wavePhase")
            .animateFloat(
                initialValue = 0f,
                targetValue = (2 * PI).toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 2000, easing = LinearEasing),
                ),
                label = "phase",
            )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    Canvas(modifier = modifier) {
        if (size.width == 0f || size.height == 0f) return@Canvas

        val clampedPercent = percent.coerceIn(0f, 100f)
        val waterTop = size.height * (1f - clampedPercent / 100f)
        val wavelength = size.width / 1.5f
        val step = 8.dp.toPx()

        val path = Path().apply {
            moveTo(0f, waterTop)
            var x = 0f
            while (x <= size.width) {
                val y = (waterTop + amplitudePx * sin((2 * PI * x / wavelength) + phase)).toFloat()
                lineTo(x, y)
                x += step
            }
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        drawPath(path, color)
    }
}

@Preview
@Composable
private fun WaterFillWavePreview() {
    PreviewTheme {
        WaterFillWave(
            percent = 60f,
            color = Color(0xFF4A90E2),
        )
    }
}
