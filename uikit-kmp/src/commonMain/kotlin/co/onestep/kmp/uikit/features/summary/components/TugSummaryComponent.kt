package co.onestep.kmp.uikit.features.summary.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import co.onestep.kmp.uikit.ui.theme.osClickIndication
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.summary.models.TugChairData
import co.onestep.kmp.uikit.features.summary.models.TugComponentData
import co.onestep.designsystem.components.OSText
import co.onestep.kmp.uikit.ui.components.ScaleToFit
import co.onestep.kmp.uikit.ui.icons.tugCShape
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.designsystem.theme.Variables
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import org.jetbrains.compose.ui.tooling.preview.Preview
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.ic_arrow_down
import co.onestep.kmp.uikit_kmp.generated.resources.ic_arrow_up
import co.onestep.kmp.uikit_kmp.generated.resources.ic_chair
import co.onestep.kmp.uikit_kmp.generated.resources.ic_info_circle
import co.onestep.kmp.uikit_kmp.generated.resources.tug_c_shape
import co.onestep.kmp.uikit_kmp.generated.resources.tug_info_description
import co.onestep.kmp.uikit_kmp.generated.resources.tug_info_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun TugSummaryComponent(
    modifier: Modifier = Modifier,
    tugComponentData: TugComponentData,
    onInfoClick: (@Composable () -> Unit) -> Unit,
) {
    val gapDesignWidth = 32.dp
    val chairDesignWidth = 75.dp
    val tugCDesignWidth = 265.dp
    val rowDesignWidth = chairDesignWidth + gapDesignWidth + tugCDesignWidth

    SummaryItemCard(modifier) {
        // Force LTR for the TUG diagram — it's a medical illustration with fixed spatial semantics
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box {
            BoxWithConstraints(Modifier.padding(Variables.GapL)) {
                val scale =
                    if (maxWidth < rowDesignWidth) {
                        maxWidth / rowDesignWidth
                    } else {
                        1f
                    }
                Row {
                    TugUpDownChair(
                        modifier = Modifier,
                        tugChairData = tugComponentData.tugChairData,
                    )

                    Spacer(Modifier.width(gapDesignWidth * scale))

                    ScaleToFit(
                        modifier =
                            Modifier
                                .align(CenterVertically),
                        designWidth = 265.dp,
                        designHeight = 150.dp,
                    ) {
                        TugUShape(tugComponentData = tugComponentData)
                    }
                }
            }

            Icon(
                painter = painterResource(Res.drawable.ic_info_circle),
                contentDescription = null,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = osClickIndication(bounded = false),
                        ) { onInfoClick { TugInfo() } },
            )
        }
        }
    }
}

@Composable
fun TugUpDownChair(
    modifier: Modifier = Modifier,
    tugChairData: TugChairData,
) {
    Column(
        modifier =
            Modifier
                .requiredWidth(75.dp)
                .wrapContentHeight()
                .then(modifier),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp),
            horizontalArrangement = Arrangement.Start,
        ) {
            OSText(
                text = tugChairData.standing.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier =
                    Modifier
                        .rotate(270f)
                        .align(CenterVertically),
            )
            Image(
                modifier =
                    Modifier
                        .align(CenterVertically)
                        .height(42.dp),
                painter = painterResource(Res.drawable.ic_arrow_up),
                contentDescription = "",
            )
        }

        Spacer(Modifier.height(12.dp))

        Box(Modifier.fillMaxWidth()) {
            Box(
                modifier =
                    Modifier
                        .size(45.dp)
                        .background(
                            color = LocalOSColors.current.neutral_p2,
                            shape = CircleShape,
                        ).align(CenterEnd),
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_chair),
                    contentDescription = "",
                    modifier = Modifier.align(Center),
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp),
            horizontalArrangement = Arrangement.Start,
        ) {
            OSText(
                text = tugChairData.sitting.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier =
                    Modifier
                        .rotate(270f)
                        .align(CenterVertically),
            )
            Image(
                modifier =
                    Modifier
                        .align(CenterVertically)
                        .height(42.dp),
                painter = painterResource(Res.drawable.ic_arrow_down),
                contentDescription = "",
            )
        }
    }
}

@Composable
fun TugUShape(
    modifier: Modifier = Modifier,
    tugComponentData: TugComponentData,
) {
    Box(
        modifier =
            modifier
                .wrapContentSize(),
    ) {
        TugC(
            Modifier,
            tugComponentData.forward.toString(),
            tugComponentData.turning.toString(),
            tugComponentData.backward.toString(),
        )
        Image(
            painter = painterResource(Res.drawable.tug_c_shape),
            contentDescription = "",
            modifier =
                Modifier
                    .align(Alignment.CenterStart),
        )
        OSText(
            text = tugComponentData.turningToChair.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 10.dp, end = Variables.GapL),
        )
    }
}

@Composable
fun TugC(
    modifier: Modifier = Modifier,
    startValue: String,
    turnValue: String,
    endValue: String,
) {
    Box(
        modifier =
            Modifier
                .width(265.dp)
                .height(150.dp)
                .then(modifier),
    ) {
        BarLeft(
            modifier = Modifier.align(Alignment.BottomStart),
            value = endValue,
        )
        BarRight(
            modifier =
                Modifier
                    .align(Alignment.TopStart),
            value = startValue,
        )

        Box(
            modifier =
                Modifier
                    .align(CenterEnd),
        ) {
            Image(
                imageVector = tugCShape(),
                contentDescription = "",
            )
            Ring(
                Modifier
                    .align(Center)
                    .padding(end = 36.dp),
                size = 24.dp,
                thickness = 9.dp,
            )
            OSText(
                text = turnValue,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier =
                    Modifier
                        .align(CenterEnd)
                        .padding(end = 8.dp),
            )
        }
    }
}

@Composable
fun BarRight(
    modifier: Modifier = Modifier,
    value: String,
    color: Color = LocalOSColors.current.neutral_m2,
) {
    Box(
        modifier =
            Modifier
                .width(185.dp)
                .height(40.dp)
                .then(modifier),
    ) {
        Canvas(Modifier) {
            val path =
                Path().apply {
                    moveTo(0f, 0f)
                    lineTo(0f, 40.dp.toPx())
                    lineTo(164.dp.toPx(), 40.dp.toPx())
                    lineTo(184.dp.toPx(), 21.dp.toPx())
                    lineTo(164.dp.toPx(), 0f)
                    lineTo(0f, 0f)
                }

            drawPath(
                path = path,
                color = color,
                style = Fill,
            )
        }
        OSText(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Center),
        )
    }
}

@Composable
fun BarLeft(
    modifier: Modifier = Modifier,
    value: String,
    color: Color = LocalOSColors.current.neutral_m2,
) {
    Box(
        modifier =
            Modifier
                .width(185.dp)
                .height(40.dp)
                .then(modifier),
    ) {
        Canvas(
            modifier = Modifier,
        ) {
            val path =
                Path().apply {
                    moveTo(0f, 0f)
                    lineTo(0f, 40.dp.toPx())
                    lineTo(177.dp.toPx(), 40.dp.toPx())
                    lineTo(157.dp.toPx(), 20.dp.toPx())
                    lineTo(177.dp.toPx(), 0f)
                    lineTo(0f, 0f)
                }

            drawPath(
                path = path,
                color = color,
                style = Fill,
            )
        }
        OSText(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Center),
        )
    }
}

@Composable
fun TugInfo(modifier: Modifier = Modifier) {
    Column(Modifier.padding(24.dp)) {
        OSText(
            text = stringResource(Res.string.tug_info_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier =
                Modifier
                    .then(modifier),
        )
        Spacer(modifier = Modifier.height(8.dp))
        OSText(
            text = stringResource(Res.string.tug_info_description),
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            modifier =
                Modifier
                    .then(modifier),
        )
    }
}

@Composable
fun Ring(
    modifier: Modifier = Modifier,
    color: Color = LocalOSColors.current.neutral_p2,
    size: Dp = 24.dp,
    thickness: Dp = Variables.GapL,
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = thickness.toPx()
        val radius = (size.toPx() - strokeWidth) / 2f

        drawCircle(
            color = color,
            radius = radius,
            style = Stroke(width = strokeWidth),
        )
    }
}

@Preview
@Composable
private fun TugSummaryComponentPreview() {
    PreviewTheme {
        TugSummaryComponent(
            tugComponentData = TugComponentData(
                forward = 2.5f,
                turning = 1.2f,
                backward = 2.8f,
                turningToChair = 1.0f,
                tugChairData = TugChairData(standing = 0.8f, sitting = 0.7f),
                distance = 6f,
                duration = 12f,
            ),
            onInfoClick = {},
        )
    }
}
