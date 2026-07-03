package co.onestep.kmp.uikit.features.summary.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.kmp.uikit.utils.cleanString
import org.jetbrains.compose.ui.tooling.preview.Preview

val NoScoreStrokeWidthCollapsed = 2.dp
val NoScoreStrokeWidthExpanded = 6.dp
val NoScoreCircleOffsetYCollapsed = (-1).dp
val NoScoreCircleOffsetYExpanded = 0.dp

@Composable
internal fun MainParamCircle(
    modifier: Modifier = Modifier,
    circleOffsetY: Dp = 0.dp,
    scoreOffsetY: Dp = 0.dp,
    circleOffsetX: Dp = 0.dp,
    circleSize: Dp,
    circleStrokeWidth: Dp,
    position: Float = 0.0f,
    scoreFontSize: TextUnit,
    animateMainParam: Boolean,
    mainParam: Float? = null,
    mainParamText: String? = null,
    mainParamColor: Color,
) {
    val animatedColor by animateColorAsState(
        targetValue = mainParamColor,
        animationSpec = tween(durationMillis = 600),
        label = "circleColorAnimation",
    )

    val scoreSize =
        run {
            val digits =
                mainParam
                    ?.toString()
                    ?.replace(".", "")
                    ?.length ?: 0

            val factor =
                when {
                    digits <= 3 -> 1f
                    digits == 4 -> 0.9f
                    digits == 5 -> 0.8f
                    else -> 0.7f
                }
            scoreFontSize * factor
        }

    val scoreCircleValueTopSpacing =
        with(LocalDensity.current) {
            lerp(10.dp, 15.dp, position)
        }

    var progress by remember { mutableFloatStateOf(0f) }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        label = "",
    )

    LaunchedEffect(mainParam) {
        progress = mainParam?.let { it / 100f } ?: 0f
    }

    Box(modifier.offset(x = circleOffsetX, y = circleOffsetY)) {
        if (mainParam != null) {
            if (animateMainParam) {
                MainParamCircleProgress(
                    Modifier.align(Center),
                    circleSize,
                    circleStrokeWidth,
                    animatedProgress,
                    animatedColor,
                )
            } else {
                CircularProgressIndicator(
                    progress = { mainParam },
                    modifier =
                        Modifier
                            .size(circleSize),
                    color = animatedColor,
                    trackColor = Color.Transparent,
                    strokeWidth = circleStrokeWidth,
                    strokeCap = StrokeCap.Round,
                )
            }

            Column(
                modifier =
                    Modifier
                        .align(Center)
                        .padding(vertical = 5.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Spacer(modifier = Modifier.height(scoreCircleValueTopSpacing))
                val tightText =
                    TextStyle(
                        fontSize = scoreSize,
                        lineHeight = scoreFontSize,
                        lineHeightStyle =
                            LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.Both,
                            ),
                    )
                OSText(
                    modifier =
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .offset(y = scoreOffsetY),
                    text = mainParam.cleanString(),
                    fontWeight = FontWeight.W800,
                    fontSize = scoreSize,
                    style = tightText,
                    textAlign = TextAlign.Center,
                )
                mainParamText?.let {
                    AnimatedContent(
                        modifier =
                            Modifier
                                .align(Alignment.CenterHorizontally)
                                .offset(y = scoreOffsetY),
                        targetState = position.coerceIn(0f, 1f),
                        label = "MainParamText",
                    ) { pos ->
                        if (pos > 0.9f) {
                            OSText(
                                modifier =
                                    Modifier
                                        .graphicsLayer { alpha = position.coerceIn(0f, 1f) }
                                        .align(Alignment.CenterHorizontally)
                                        .requiredWidth(60.dp)
                                        .wrapContentHeight(),
                                text = mainParamText,
                                fontWeight = FontWeight.W400,
                                fontSize = 14.sp,
                                color = LocalOSColors.current.neutral_p1,
                                maxLines = 1,
                                lineHeight = 20.sp,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        } else {
            EmptyScoreCircle(
                Modifier.size(circleSize),
                position,
            )
        }
    }
}

@Composable
private fun MainParamCircleProgress(
    modifier: Modifier,
    circleSize: Dp,
    circleStrokeWidth: Dp,
    animatedProgress: Float,
    mainParamColor: Color,
) {
    CircularProgressIndicator(
        progress = { 100f },
        modifier =
            Modifier
                .size(circleSize)
                .padding(1.dp)
                .then(modifier),
        color = LocalOSColors.current.neutral_m2,
        trackColor = Color.Transparent,
        strokeWidth = circleStrokeWidth,
        strokeCap = StrokeCap.Round,
    )
    CircularProgressIndicator(
        progress = { animatedProgress },
        modifier =
            Modifier
                .size(circleSize)
                .then(modifier),
        color = mainParamColor,
        trackColor = Color.Transparent,
        strokeWidth = circleStrokeWidth,
        strokeCap = StrokeCap.Round,
    )
}

@Preview
@Composable
private fun MainParamCirclePreview() {
    PreviewTheme {
        MainParamCircle(
            circleSize = 115.dp,
            circleStrokeWidth = 7.dp,
            scoreFontSize = 42.sp,
            animateMainParam = false,
            mainParam = 75f,
            mainParamColor = Color.Green,
        )
    }
}
