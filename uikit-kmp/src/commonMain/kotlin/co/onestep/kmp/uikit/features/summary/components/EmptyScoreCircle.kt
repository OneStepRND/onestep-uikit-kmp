package co.onestep.kmp.uikit.features.summary.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.not_available
import co.onestep.kmp.uikit_kmp.generated.resources.score_unavailable
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

private const val NO_SCORE_DASH_LENGTH_COLLAPSED = 15f
private const val NO_SCORE_DASH_LENGTH_EXPANDED = 40f

private fun lerpFloat(start: Float, stop: Float, fraction: Float): Float =
    start + (stop - start) * fraction

@Composable
internal fun EmptyScoreCircle(
    modifier: Modifier = Modifier,
    progress: Float,
) {
    val strokeWidth =
        with(LocalDensity.current) {
            lerpFloat(NoScoreStrokeWidthCollapsed.toPx(), NoScoreStrokeWidthExpanded.toPx(), progress)
        }

    val circleOffset =
        with(LocalDensity.current) {
            lerpFloat(NoScoreCircleOffsetYCollapsed.toPx(), NoScoreCircleOffsetYExpanded.toPx(), progress)
        }

    val scoreTitleOffsetY =
        with(LocalDensity.current) {
            lerpFloat(NoScoreCircleOffsetYCollapsed.toPx(), NoScoreCircleOffsetYExpanded.toPx(), progress)
        }

    val dashLength =
        lerpFloat(NO_SCORE_DASH_LENGTH_COLLAPSED, NO_SCORE_DASH_LENGTH_EXPANDED, progress)

    // #E7E6E6 in the light theme; reading the role adapts the ring for dark. (no light change)
    val ringColor = LocalOSColors.current.neutral_m2

    Box(
        contentAlignment = Center,
        modifier = modifier,
    ) {
        Canvas(
            modifier =
                Modifier
                    .matchParentSize()
                    .offset(y = circleOffset.dp),
        ) {
            val innerRadius = (size.minDimension - strokeWidth) / 2

            drawCircle(
                color = ringColor,
                radius = innerRadius,
                style =
                    Stroke(
                        width = strokeWidth,
                        pathEffect =
                            PathEffect.dashPathEffect(
                                floatArrayOf(
                                    dashLength,
                                    dashLength,
                                ),
                                60f,
                            ),
                    ),
            )
        }

        val currentDensity = LocalDensity.current
        val noFontScaleDensity =
            Density(
                density = currentDensity.density,
                fontScale = 1f,
            )
        CompositionLocalProvider(LocalDensity provides noFontScaleDensity) {
            OSText(
                modifier =
                    Modifier
                        .align(Center)
                        .offset(y = scoreTitleOffsetY.dp)
                        .graphicsLayer { alpha = progress.coerceIn(0f, 1f) },
                text = stringResource(Res.string.score_unavailable),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
        }

        OSText(
            modifier =
                Modifier
                    .align(Center)
                    .offset(y = scoreTitleOffsetY.dp)
                    .graphicsLayer { alpha = 1f - progress.coerceIn(0f, 1f) },
            text = stringResource(Res.string.not_available),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview
@Composable
private fun EmptyScoreCirclePreview() {
    PreviewTheme {
        EmptyScoreCircle(
            modifier = Modifier.size(115.dp),
            progress = 1f,
        )
    }
}
