package co.onestep.kmp.uikit.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.wheel_picker_blank
import kotlin.math.abs
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Visible item count (must be odd so there is a clear center slot).
 */
private const val VISIBLE_ITEMS = 5
private const val PAD_SLOTS = (VISIBLE_ITEMS - 1) / 2 // 2 padding slots above & below
private val ITEM_HEIGHT: Dp = 44.dp
private val PICKER_HEIGHT: Dp = ITEM_HEIGHT * VISIBLE_ITEMS
private val PAD_HEIGHT: Dp = ITEM_HEIGHT * PAD_SLOTS

// Visual interpolation endpoints.
private val CENTER_FONT_SIZE = 32.sp
private val EDGE_FONT_SIZE = 14.sp
private const val CENTER_ALPHA = 1f
private const val EDGE_ALPHA = 0.15f

// distance (in item-heights) at which the edge values apply. Items further
// than this are clamped to the edge styling. With 5 visible items the
// outermost slot is 2 item-heights from the centre, so 2f is the natural max.
private const val MAX_DISTANCE_SLOTS = 2f

/**
 * A vertical wheel-style number picker. The center slot is the selected value.
 *
 * As items scroll past the centre, their font size and alpha interpolate
 * continuously between the centre and edge values, driven by the underlying
 * [LazyListState.layoutInfo] — no separate animation primitives are needed.
 *
 * Layout:
 *  - The picker viewport is [PICKER_HEIGHT] tall ([VISIBLE_ITEMS] rows of [ITEM_HEIGHT]).
 *  - A pill is drawn behind ONLY the center row (height = [ITEM_HEIGHT]); the four
 *    neighbour rows render on the plain background.
 *  - `LazyColumn.contentPadding` reserves [PAD_HEIGHT] (= 2 rows) of empty space
 *    at top and bottom, so item 0 can scroll down into the center slot and the
 *    last item can scroll up into the center slot — and neither can leave it.
 *
 * @param range The inclusive integer range to scroll through (e.g. 0..40).
 * @param value Currently selected value, or `null` for the blank "---" state.
 * @param onValueChange Invoked with the integer that lands in the center slot after a snap.
 */
@Composable
internal fun WheelNumberPicker(
    value: Int?,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    range: IntRange = 0..40,
) {
    val colors = LocalOSColors.current
    val items = remember(range) { range.toList() }

    val initialIndex = remember(items) {
        val idx = items.indexOf(value)
        if (idx < 0) 0 else idx
    }

    val listState: LazyListState =
        rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // While `value == null`, the user hasn't touched the wheel — show the "---"
    // overlay over the center slot. As soon as they scroll, hide it.
    var showBlankOverlay by remember { mutableStateOf(value == null) }

    val density = LocalDensity.current
    val itemHeightPx = remember(density) { with(density) { ITEM_HEIGHT.toPx() } }

    // The centered item is whichever real item is closest to the viewport center.
    // With our contentPadding scheme, item index 0 sits at the center when
    // firstVisibleItemIndex == 0 and offset == 0.
    val centerIndex by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val visible = info.visibleItemsInfo
            if (visible.isEmpty()) {
                listState.firstVisibleItemIndex
            } else {
                val viewportCenter =
                    (info.viewportStartOffset + info.viewportEndOffset) / 2
                visible.minBy { abs((it.offset + it.size / 2) - viewportCenter) }.index
            }
        }
    }

    // The effect below is keyed on (listState, items) only, so it would capture
    // `value`/`onValueChange` from the composition it was launched in. Route them
    // through rememberUpdatedState so the collect lambda always compares against
    // the CURRENT value — otherwise scrolling away and back to the initial value
    // suppresses the callback and Save submits a stale number.
    val currentValue by rememberUpdatedState(value)
    val currentOnValueChange by rememberUpdatedState(onValueChange)

    LaunchedEffect(listState, items) {
        snapshotFlow {
            listState.isScrollInProgress to centerIndex
        }
            .distinctUntilChanged()
            .collect { (scrolling, idx) ->
                if (scrolling) {
                    // Any user interaction dismisses the blank overlay.
                    if (showBlankOverlay) showBlankOverlay = false
                } else if (idx in items.indices) {
                    val picked = items[idx]
                    if (picked != currentValue) currentOnValueChange(picked)
                }
            }
    }

    val blankLabel = stringResource(Res.string.wheel_picker_blank)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(PICKER_HEIGHT),
        contentAlignment = Alignment.Center,
    ) {
        // Pill behind ONLY the center row.
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(ITEM_HEIGHT)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.neutral_m2),
        )

        // Number list. contentPadding lets index 0 reach the center slot and
        // prevents the last index from leaving it (no empty centered slot
        // past either bound).
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = PAD_HEIGHT),
            modifier = Modifier
                .fillMaxWidth()
                .height(PICKER_HEIGHT)
                .background(Color.Transparent),
        ) {
            items(items.size) { index ->
                // Continuous distance-from-centre fraction, derived from
                // layoutInfo. Reads happen inside lambdas (derivedStateOf for
                // the fontSize that flows through Text, graphicsLayer{} for
                // alpha) so we recompose/redraw smoothly as the list scrolls.
                val fraction by remember(index, itemHeightPx) {
                    derivedStateOf {
                        val info = listState.layoutInfo
                        val item = info.visibleItemsInfo.firstOrNull { it.index == index }
                        if (item == null || itemHeightPx <= 0f) {
                            1f
                        } else {
                            val viewportCenter =
                                (info.viewportStartOffset + info.viewportEndOffset) / 2f
                            val itemCenter = item.offset + item.size / 2f
                            val distanceSlots =
                                abs(itemCenter - viewportCenter) / itemHeightPx
                            (distanceSlots / MAX_DISTANCE_SLOTS).coerceIn(0f, 1f)
                        }
                    }
                }

                val fontSize = lerp(CENTER_FONT_SIZE, EDGE_FONT_SIZE, fraction)
                val weight = if (fraction < 0.25f) FontWeight.Bold else FontWeight.Medium

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ITEM_HEIGHT)
                        .padding(horizontal = 16.dp)
                        .graphicsLayer {
                            alpha = androidx.compose.ui.util.lerp(
                                CENTER_ALPHA,
                                EDGE_ALPHA,
                                fraction,
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    OSText(
                        text = items[index].toString(),
                        fontSize = fontSize,
                        fontWeight = weight,
                        color = colors.neutral_p3,
                    )
                }
            }
        }

        // Blank "---" overlay shown until the user scrolls.
        if (showBlankOverlay && value == null) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(ITEM_HEIGHT)
                    .background(colors.neutral_m2, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                OSText(
                    text = blankLabel,
                    fontSize = CENTER_FONT_SIZE,
                    fontWeight = FontWeight.Bold,
                    color = colors.neutral_p3,
                )
            }
        }
    }
}

@Preview
@Composable
private fun WheelNumberPickerPreviewBlank() {
    PreviewTheme {
        WheelNumberPicker(
            value = null,
            onValueChange = {},
            modifier = Modifier.padding(16.dp),
            range = 0..40,
        )
    }
}

@Preview
@Composable
private fun WheelNumberPickerPreviewSelected() {
    PreviewTheme {
        WheelNumberPicker(
            value = 2,
            onValueChange = {},
            modifier = Modifier.padding(16.dp),
            range = 0..40,
        )
    }
}

@Preview
@Composable
private fun WheelNumberPickerPreviewMax() {
    PreviewTheme {
        WheelNumberPicker(
            value = 40,
            onValueChange = {},
            modifier = Modifier.padding(16.dp),
            range = 0..40,
        )
    }
}
