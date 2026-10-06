package co.onestep.kmp.uikit.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.roundToInt
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData
import co.onestep.designsystem.components.OSTextFixedSize
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * A number (or `mm:ss` timer) whose characters animate individually as they change.
 *
 * The per-character animation is visual only. Semantically the counter is ONE node carrying the
 * whole [text] — `Row` merges its descendants and exposes [text] as its text, and each animated
 * character clears its own semantics. Before this, every character was its own node: a screen
 * reader read the recording timer as "0", "1", ":", "0", "5", and no node ever matched `01:05`,
 * so a host's UI test that waits for a timer mark (the clinician app's
 * `stop_recording_at.yaml`, which ends count-up recordings at a given `mm:ss`) could never fire
 * and recorded to the activity's ceiling. Found on a Pixel 7a emulator, 2026-10-04.
 *
 * A test tag the caller puts on [modifier] (`RECORDING_TIMER`) lands on that same merged node,
 * so one node carries both the id and the readable value. No live region: a per-second timer
 * announcing itself every second would be noise, and the stage changes around it already speak.
 */
@Composable
fun AnimatedCounter(
    modifier: Modifier = Modifier,
    text: String,
    textData: TextData,
    reset: Boolean = false,
) {
    val enter = if (reset) fadeIn() else counterInUp
    val exit = if (reset) fadeOut() else counterOutUp
    val spec: AnimatedContentTransitionScope<Char>.() -> ContentTransform =
        { enter togetherWith exit }

    // The whole value, named apart from the semantics receiver's own `text` property below.
    val wholeValue = text
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier
                .shrinkToFitWidth()
                .semantics(mergeDescendants = true) {
                this.text = AnnotatedString(wholeValue)
            },
        ) {
            text.forEachIndexed { index, char ->
                key(index) {
                    AnimatedContent(
                        targetState = char,
                        transitionSpec = spec,
                        label = "counter",
                        // Visual only: the merged parent speaks for the whole value.
                        modifier = Modifier.clearAndSetSemantics { },
                    ) { animatedChar ->
                        // Fixed size: the counter ignores the phone's font-size setting. It is
                        // already large, and scaling it broke the recording layout (OS-17707,
                        // OS-17708).
                        OSTextFixedSize(
                            text = animatedChar.toString(),
                            fontSize = textData.textSize,
                            fontWeight = textData.fontWeight,
                            color = LocalOSColors.current.neutral_m5,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Scales the content down when its natural width is wider than the space it gets.
 *
 * A fixed font size alone is not enough: the phone's display Zoom raises the density, so a fixed
 * `sp` still grows in pixels and the timer's last digit was clipped (OS-17708). At normal sizes
 * the content fits and this does nothing.
 */
private fun Modifier.shrinkToFitWidth(): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints.copy(minWidth = 0, maxWidth = Constraints.Infinity))
        val scale =
            if (constraints.hasBoundedWidth && placeable.width > constraints.maxWidth) {
                constraints.maxWidth.toFloat() / placeable.width
            } else {
                1f
            }
        val width = (placeable.width * scale).roundToInt()
        val height = (placeable.height * scale).roundToInt()
        layout(width, height) {
            // The layer scales around the content's centre, so centre it on the scaled box.
            placeable.placeWithLayer((width - placeable.width) / 2, (height - placeable.height) / 2) {
                scaleX = scale
                scaleY = scale
            }
        }
    }

private const val STIFFNESS = 150f

val counterInUp =
    slideInVertically(
        spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = STIFFNESS,
        ),
    ) { it } + scaleIn() + fadeIn()

val counterOutUp =
    slideOutVertically(
        spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = STIFFNESS,
        ),
    ) { -it } + scaleOut() + fadeOut()

@Preview
@Composable
private fun AnimatedCounterPreview() {
    PreviewTheme {
        AnimatedCounter(
            text = "42",
            textData = TextData("42", 48.sp, FontWeight.Bold),
        )
    }
}
