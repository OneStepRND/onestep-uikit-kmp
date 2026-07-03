package co.onestep.kmp.uikit.features.summary.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import kotlin.math.roundToInt

@Composable
internal fun CollapsingLayout(
    collapsingTop: @Composable BoxScope.() -> Unit,
    bodyContent: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    onCollapseProgressChanged: ((Float) -> Unit)? = null,
) {
    var collapsingTopHeight by remember { mutableFloatStateOf(0f) }
    var offset by remember { mutableFloatStateOf(0f) }

    val collapseProgress =
        if (collapsingTopHeight > 0f) {
            (-offset / collapsingTopHeight).coerceIn(0f, 1f)
        } else {
            0f
        }

    LaunchedEffect(collapseProgress) {
        onCollapseProgressChanged?.invoke(collapseProgress)
    }

    fun calculateOffset(delta: Float): Offset {
        val oldOffset = offset
        val newOffset = (oldOffset + delta).coerceIn(-collapsingTopHeight, 0f)
        offset = newOffset
        return Offset(0f, newOffset - oldOffset)
    }

    val nestedScrollConnection =
        remember {
            object : NestedScrollConnection {
                override fun onPreScroll(
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset =
                    when {
                        available.y >= 0 -> Offset.Zero
                        offset == -collapsingTopHeight -> Offset.Zero
                        else -> calculateOffset(available.y)
                    }

                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset =
                    when {
                        available.y <= 0 -> Offset.Zero
                        offset == 0f -> Offset.Zero
                        else -> calculateOffset(available.y)
                    }

                override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                    return if (available.y > 0 && offset < 0f) {
                        val animatable = Animatable(offset)
                        animatable.animateTo(
                            targetValue = 0f,
                            animationSpec = spring(stiffness = Spring.StiffnessMedium),
                            initialVelocity = available.y,
                        ) {
                            offset = value
                        }
                        available
                    } else {
                        Velocity.Zero
                    }
                }
            }
        }

    val toolbarScrollableState = remember {
        ScrollableState { delta -> calculateOffset(delta).y }
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection),
    ) {
        Box(
            modifier =
                Modifier
                    .onSizeChanged { size ->
                        collapsingTopHeight = size.height.toFloat()
                    }
                    .offset { IntOffset(x = 0, y = offset.roundToInt()) }
                    .scrollable(
                        orientation = Orientation.Vertical,
                        state = toolbarScrollableState,
                    ),
            content = collapsingTop,
        )
        Box(
            modifier =
                Modifier.offset {
                    IntOffset(
                        x = 0,
                        y = (collapsingTopHeight + offset).roundToInt(),
                    )
                },
            content = bodyContent,
        )
    }
}
