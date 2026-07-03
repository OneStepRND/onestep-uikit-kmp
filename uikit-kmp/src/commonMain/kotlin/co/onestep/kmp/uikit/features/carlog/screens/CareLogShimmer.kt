package co.onestep.kmp.uikit.features.carlog.screens

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.designsystem.theme.Variables
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun CarLogShimmerPreview() {
    PreviewTheme {
        CarLogShimmer()
    }
}

@Composable
internal fun CarLogShimmer() {
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .padding(Variables.GapL),
        ) {
            Row(Modifier.fillMaxWidth()) {
                ShimmerBox(Modifier.weight(1f).height(25.dp))
                Spacer(Modifier.width(8.dp))
                ShimmerBox(Modifier.weight(1f).height(25.dp))
            }
            Spacer(modifier = Modifier.size(Variables.GapL))
            ShimmerBox(Modifier.fillMaxWidth(0.65f).height(30.dp))
            Spacer(modifier = Modifier.size(Variables.GapL))
            ShimmerBox(Modifier.fillMaxWidth().height(150.dp))
            Spacer(modifier = Modifier.size(Variables.GapL))
            ShimmerBox(Modifier.fillMaxWidth().height(100.dp))
            Spacer(modifier = Modifier.size(Variables.GapL))
            ShimmerBox(Modifier.fillMaxWidth(0.65f).height(30.dp))
            Spacer(modifier = Modifier.size(Variables.GapL))
            ShimmerBox(Modifier.fillMaxWidth().height(250.dp))
            Spacer(modifier = Modifier.size(Variables.GapL))
            ShimmerBox(Modifier.fillMaxWidth().height(150.dp))
            Spacer(modifier = Modifier.size(Variables.GapL))
            ShimmerBox(Modifier.fillMaxWidth(0.65f).height(30.dp))
            Spacer(modifier = Modifier.size(Variables.GapL))
            ShimmerBox(Modifier.fillMaxWidth().height(150.dp))
            Spacer(modifier = Modifier.size(Variables.GapL))
        }
    }
}

@Composable
fun ShimmerCircle(modifier: Modifier) {
    Box(
        modifier
            .clip(CircleShape)
            .shimmer(),
    )
}

@Composable
private fun ShimmerBox(modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .shimmer(),
    )
}

fun Modifier.shimmer(): Modifier =
    composed {
        var size by remember { mutableStateOf(IntSize.Zero) }
        val transition = rememberInfiniteTransition(label = "")
        val startOffsetX by transition.animateFloat(
            initialValue = -2 * size.width.toFloat(),
            targetValue = 2 * size.width.toFloat(),
            animationSpec =
                infiniteRepeatable(
                    tween(1500),
                ),
            label = "",
        )
        background(
            brush =
                Brush.linearGradient(
                    colors =
                        listOf(
                            LocalOSColors.current.neutral_m1,
                            LocalOSColors.current.neutral_m2,
                            LocalOSColors.current.neutral_m1,
                        ),
                    start = Offset(startOffsetX, 0f),
                    end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat()),
                ),
        ).onGloballyPositioned {
            size = it.size
        }
    }
