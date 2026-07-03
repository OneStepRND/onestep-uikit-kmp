package co.onestep.kmp.uikit.features.summary.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import co.onestep.kmp.uikit.models.OSTNorm
import co.onestep.kmp.uikit.models.OSTNormPart
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit.utils.getNormColor
import co.onestep.kmp.uikit.utils.mockNorm
import co.onestep.kmp.uikit.utils.toPartColor
import co.onestep.kmp.uikit.utils.toText
import org.jetbrains.compose.ui.tooling.preview.Preview

const val DEFAULT_CIRCLE_HEIGHT = 6
const val COMPARED_CIRCLE_HEIGHT = 46

@Composable
internal fun NormIndicator(
    modifier: Modifier = Modifier,
    norm: OSTNorm,
    value: Float,
    previousValue: Float? = null,
) {
    val circleText = remember { value.toText() }
    val previousCircleText = remember { previousValue?.toText().toString() }
    var valueColor by remember { mutableStateOf(Color.Unspecified) }
    var previousValueColor by remember { mutableStateOf(Color.Unspecified) }
    val currentDensity = LocalDensity.current
    val noFontScaleDensity =
        Density(
            density = currentDensity.density,
            fontScale = 1f,
        )
    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        val totalWidth = remember { maxWidth }
        val totalStart = remember { (norm.parts?.firstOrNull()?.start ?: 0f) }
        val totalEnd = remember { norm.parts?.lastOrNull()?.end ?: 1f }
        val totalRange = remember { totalEnd - totalStart }

        val clampedValue = remember { value.coerceIn(totalStart, totalEnd) }
        val clampedPreviousValue = remember { previousValue?.coerceIn(totalStart, totalEnd) }

        Column(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(Variables.GapL)
                    .align(Alignment.Center),
        ) {
            CompositionLocalProvider(LocalDensity provides noFontScaleDensity) {
                Box {
                    ParameterValueCircle(
                        Modifier
                            .align(BottomCenter)
                            .zIndex(0f),
                        totalWidth,
                        clampedValue,
                        totalStart,
                        totalRange,
                        valueColor,
                        circleText,
                        filled = true,
                        height =
                            if (previousValue == null) {
                                DEFAULT_CIRCLE_HEIGHT.dp
                            } else {
                                COMPARED_CIRCLE_HEIGHT.dp
                            },
                    )

                    clampedPreviousValue?.let {
                        ParameterValueCircle(
                            Modifier
                                .align(BottomCenter)
                                .zIndex(1f),
                            totalWidth,
                            clampedPreviousValue,
                            totalStart,
                            totalRange,
                            previousValueColor,
                            previousCircleText,
                        )
                    }
                }
            }

            Row(
                modifier =
                    modifier
                        .fillMaxWidth(),
            ) {
                norm.parts?.forEachIndexed { index, part ->
                    val partColor = remember { part.color.toPartColor() }
                    val isFirst = remember { index == 0 }
                    val isLast = remember { index == (norm.parts?.size?.minus(1)) }

                    val shape =
                        remember {
                            when {
                                isFirst -> RoundedCornerShape(topStart = 5.dp, bottomStart = 5.dp)
                                isLast -> RoundedCornerShape(topEnd = 5.dp, bottomEnd = 5.dp)
                                else -> RoundedCornerShape(0.dp)
                            }
                        }

                    val partRange = remember { part.end - part.start }
                    val weight = remember { partRange / totalRange }

                    valueColor = remember { value.getNormColor(norm) }

                    previousValue?.let { prevVal ->
                        previousValueColor = remember { prevVal.getNormColor(norm) }
                    }

                    Column(
                        modifier =
                            Modifier
                                .weight(weight)
                                .wrapContentHeight(),
                    ) {
                        PartColoredLine(partColor, shape)
                        Spacer(modifier = Modifier.height(4.dp))
                        CompositionLocalProvider(LocalDensity provides noFontScaleDensity) {
                            PartTextValues(isFirst, isLast, part, norm.parts?.size == 2)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PartColoredLine(
    partColor: Color,
    shape: RoundedCornerShape,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(5.dp)
                .background(color = partColor, shape = shape),
    )
}

@Composable
private fun PartTextValues(
    isFirst: Boolean,
    isLast: Boolean,
    normPart: OSTNormPart,
    isOnlyTwoParts: Boolean,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        if (isFirst || isLast) {
            OSText(
                modifier =
                    Modifier
                        .padding(start = 4.dp)
                        .align(Alignment.CenterStart)
                        .offset(x = if (isLast) (-8).dp else 0.dp),
                text = normPart.start.toInt().toString(),
            )
        }
        if (isLast || isFirst && !isOnlyTwoParts) {
            OSText(
                modifier =
                    Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = if (isFirst) 8.dp else 0.dp),
                text = normPart.end.toInt().toString(),
            )
        }
    }
}

@Preview
@Composable
private fun NormIndicatorPreview() {
    PreviewTheme {
        NormIndicator(
            norm = mockNorm,
            value = 5f,
        )
    }
}

@Composable
private fun ParameterValueCircle(
    modifier: Modifier = Modifier,
    totalWidth: Dp,
    clampedValue: Float,
    totalStart: Float,
    totalRange: Float,
    correspondingColor: Color,
    text: String,
    filled: Boolean = false,
    height: Dp = DEFAULT_CIRCLE_HEIGHT.dp,
) {
    val boxWidth =
        remember {
            when (text.length) {
                1 -> 32.dp
                2 -> 32.dp
                3 -> 40.dp
                4 -> 50.dp
                5 -> 54.dp
                else -> 52.dp
            }
        }
    val padding = 4.dp
    val maxOffset = totalWidth - boxWidth
    val valuePosition =
        (((clampedValue - totalStart) / totalRange) * (maxOffset - padding)) + padding

    val circleModifier =
        if (filled) {
            Modifier.background(color = correspondingColor, shape = RoundedCornerShape(50))
        } else {
            Modifier
                .background(color = Color.White, shape = RoundedCornerShape(50))
                .border(width = 1.dp, color = correspondingColor, shape = RoundedCornerShape(50))
        }

    Column(
        Modifier
            .offset(x = valuePosition - (boxWidth / 2))
            .then(modifier),
    ) {
        Box(
            modifier =
                Modifier
                    .size(boxWidth, height = 32.dp)
                    .then(circleModifier),
        ) {
            OSText(
                modifier =
                    Modifier
                        .padding(Variables.GapS)
                        .align(Alignment.Center),
                text = text,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.W400,
                fontSize = 16.sp,
                color = if (filled) Color.White else correspondingColor,
            )
        }
        Spacer(
            modifier =
                Modifier
                    .height(height)
                    .width(2.dp)
                    .background(LocalOSColors.current.neutral_m1)
                    .padding(bottom = Variables.GapM)
                    .align(Alignment.CenterHorizontally),
        )
    }
}
