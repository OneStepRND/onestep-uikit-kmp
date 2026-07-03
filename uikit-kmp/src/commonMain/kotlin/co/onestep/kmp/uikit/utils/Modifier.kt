package co.onestep.kmp.uikit.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

fun Modifier.verticalFadingEdge(
    lazyListState: LazyListState,
    length: Dp = 100.dp,
) = composed(
    debugInspectorInfo {
        name = "length"
        value = length
    },
) {
    val topFadingEdgeStrength by remember {
        derivedStateOf {
            lazyListState.layoutInfo
                .run {
                    when {
                        visibleItemsInfo.size in 0..1 -> 0f
                        visibleItemsInfo.first().offset == viewportStartOffset -> 0f
                        visibleItemsInfo.first().offset < viewportStartOffset ->
                            visibleItemsInfo.first().run {
                                abs(offset) / size.toFloat()
                            }
                        else -> 1f
                    }
                }.coerceAtMost(1f)
        }
    }
    val bottomFadingEdgeStrength by remember {
        derivedStateOf {
            lazyListState.layoutInfo
                .run {
                    when {
                        visibleItemsInfo.size in 0..1 -> 0f
                        visibleItemsInfo.last().run { offset + size } == viewportEndOffset -> 0f
                        visibleItemsInfo.last().run { offset + size } > viewportEndOffset ->
                            visibleItemsInfo.last().run {
                                (viewportEndOffset - offset) / size.toFloat()
                            }
                        else -> 1f
                    }
                }.coerceAtMost(1f)
        }
    }

    graphicsLayer { alpha = 0.99f }.drawWithContent {
        val lengthValue = length.toPx()

        drawContent()

        drawTopEdge(topFadingEdgeStrength * lengthValue)

        drawBottomEdge(bottomFadingEdgeStrength * lengthValue)
    }
}

fun ContentDrawScope.drawTopEdge(strength: Float) =
    drawRect(
        brush =
            Brush.verticalGradient(
                colors =
                    listOf(
                        Color.Transparent,
                        Color.Black,
                    ),
                startY = 0f,
                endY = strength,
            ),
        size =
            Size(
                this.size.width,
                strength,
            ),
        blendMode = BlendMode.DstIn,
    )

fun ContentDrawScope.drawBottomEdge(strength: Float) =
    drawRect(
        brush =
            Brush.verticalGradient(
                colors =
                    listOf(
                        Color.Black,
                        Color.Transparent,
                    ),
                startY = size.height - strength,
                endY = size.height,
            ),
        topLeft = Offset(x = 0f, y = size.height - strength),
        blendMode = BlendMode.DstIn,
    )
