package co.onestep.kmp.uikit.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import co.onestep.kmp.uikit.utils.rtlMirror
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.ic_back_arrow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

@Composable
fun SlideToStopButton(
    modifier: Modifier = Modifier,
    trackColor: Color = Color(0xFFB00404),
    trackText: String,
    trackTextSize: TextUnit = 20.sp,
    trackTextColor: Color = Color.White,
    thumbSizeDp: Dp = 60.dp,
    thumbColor: Color = Color.White,
    onDone: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val thumbOffsetAnim = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val trackMaxWidthPx = with(density) { maxWidth.toPx() }
        val thumbSizePx = with(density) { thumbSizeDp.toPx() }
        val maxOffsetPx = (trackMaxWidthPx - thumbSizePx).coerceAtLeast(0f)

        // In RTL the thumb starts at the far end and drags toward 0
        val displayOffset = if (isRtl) -thumbOffsetAnim.value else thumbOffsetAnim.value

        val textAlpha =
            if (maxOffsetPx > 0f) {
                (1f - (thumbOffsetAnim.value / maxOffsetPx)).coerceIn(0f, 1f)
            } else {
                1f
            }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(thumbSizeDp)
                    .background(trackColor, shape = RoundedCornerShape(8.dp))
                    .border(
                        BorderStroke(1.dp, LocalOSColors.current.neutral_m5),
                        shape = RoundedCornerShape(8.dp),
                    ),
            contentAlignment = Alignment.Center,
        ) {
            OSText(
                text = trackText,
                color = trackTextColor,
                fontSize = trackTextSize,
                fontWeight = W700,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier =
                    Modifier
                        .padding(start = thumbSizeDp, end = Variables.GapL)
                        .alpha(textAlpha),
            )
        }

        Box(
            modifier =
                Modifier
                    .absoluteOffset { IntOffset(displayOffset.roundToInt(), 0) }
                    .size(thumbSizeDp)
                    .background(thumbColor, shape = RoundedCornerShape(8.dp))
                    .draggable(
                        orientation = Orientation.Horizontal,
                        reverseDirection = isRtl,
                        state =
                            rememberDraggableState { delta ->
                                if (isDragging) {
                                    val newValue = (thumbOffsetAnim.value + delta).coerceIn(0f, maxOffsetPx)
                                    scope.launch {
                                        thumbOffsetAnim.snapTo(newValue)
                                    }
                                }
                            },
                        onDragStarted = { isDragging = true },
                        onDragStopped = {
                            isDragging = false
                            if (thumbOffsetAnim.value > maxOffsetPx / 2f) {
                                scope.launch {
                                    thumbOffsetAnim.animateTo(
                                        targetValue = maxOffsetPx,
                                        animationSpec = tween(durationMillis = 300),
                                    )
                                    onDone()
                                }
                            } else {
                                scope.launch {
                                    thumbOffsetAnim.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(durationMillis = 300),
                                    )
                                }
                            }
                        },
                    ),
            contentAlignment = Alignment.Center,
        ) {
            // Arrow icon: points right in LTR, points left in RTL
            Icon(
                painter = painterResource(Res.drawable.ic_back_arrow),
                contentDescription = null,
                modifier = Modifier.size(24.dp).rotate(180f).rtlMirror(),
                tint = Color.Black,
            )
        }
    }
}

@Preview
@Composable
private fun SlideToStopButtonPreview() {
    PreviewTheme {
        SlideToStopButton(
            trackText = "Slide to stop",
            onDone = {},
        )
    }
}
