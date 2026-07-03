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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

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

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(modifier) {
            text.forEachIndexed { index, char ->
                key(index) {
                    AnimatedContent(
                        targetState = char,
                        transitionSpec = spec,
                        label = "counter",
                    ) { animatedChar ->
                        OSText(
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
