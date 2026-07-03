package co.onestep.kmp.uikit.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import kotlin.math.roundToInt
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ScaleToFit(
    designWidth: Dp,
    designHeight: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) = Layout(
    modifier = modifier,
    content = content,
) { measures, constraints ->

    val placeable =
        measures.first().measure(
            Constraints.fixed(designWidth.roundToPx(), designHeight.roundToPx()),
        )

    val scale =
        minOf(
            constraints.maxWidth / designWidth.toPx(),
            constraints.maxHeight / designHeight.toPx(),
        ).coerceAtMost(1f)

    val w = (designWidth.toPx() * scale).roundToInt()
    val h = (designHeight.toPx() * scale).roundToInt()

    layout(w, h) {
        placeable.placeRelativeWithLayer(0, 0) {
            scaleX = scale
            scaleY = scale
            transformOrigin = TransformOrigin(0f, 0f)
        }
    }
}

@Preview
@Composable
private fun ScaleToFitPreview() {
    PreviewTheme {
        ScaleToFit(designWidth = 300.dp, designHeight = 100.dp) {
            Text(text = "Scaled content")
        }
    }
}
