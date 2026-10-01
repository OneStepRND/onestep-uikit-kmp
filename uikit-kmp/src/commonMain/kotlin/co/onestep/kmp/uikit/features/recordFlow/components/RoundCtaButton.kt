package co.onestep.kmp.uikit.features.recordFlow.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import co.onestep.kmp.uikit.ui.theme.osClickIndication
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.recordFlow.previewMainButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.MainButtonData
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import co.onestep.designsystem.components.OSText
import co.onestep.kmp.uikit.ui.components.PulsingCircles
import co.onestep.designsystem.theme.LocalOSColors

@Composable
fun RoundCtaButtonWithPulse(
    modifier: Modifier = Modifier,
    mainButtonData: MainButtonData,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        PulsingCircles(
            modifier = Modifier.fillMaxSize(),
            baseSize = 200.dp,
        )
        RoundCtaButton(
            modifier = Modifier.fillMaxSize(),
            mainButtonData = mainButtonData,
        )
    }
}

@Composable
fun RoundCtaButton(
    modifier: Modifier,
    mainButtonData: MainButtonData,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState().value
    val scale = animateFloatAsState(targetValue = if (isPressed) 0.95f else 1.0f, label = "")
    val containerColor = mainButtonData.color ?: LocalOSColors.current.primary_p3_main
    val labelColor = labelColorOn(containerColor)

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = labelColor,
            ),
        border = BorderStroke(10.dp, Color.White),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 12.dp,
                pressedElevation = 4.dp,
            ),
        shape = CircleShape,
        modifier =
            modifier
                .scale(scale.value)
                .clickable(
                    interactionSource = interactionSource,
                    indication = osClickIndication(bounded = true),
                ) { mainButtonData.action.invoke() },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            OSText(
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 20.dp),
                textAlign = TextAlign.Center,
                lineHeight = 30.sp,
                text = mainButtonData.text.text,
                fontSize = mainButtonData.text.textSize,
                color = labelColor,
                fontWeight = mainButtonData.text.fontWeight,
            )
        }
    }
}

// `neutral_p3`'s light-theme value, fixed rather than read from the theme: the fill is the host's
// colour whatever the theme, so the label must not flip with it.
private val DarkLabel = Color(0xFF3E3D3B)

private fun contrast(a: Color, b: Color): Float {
    val (hi, lo) = listOf(a.luminance(), b.luminance()).sortedDescending()
    return (hi + 0.05f) / (lo + 0.05f)
}

/**
 * White unless a light host colour would leave it unreadable — e.g. teal `#1CA8B0` is 2.9:1 against
 * white but 3.7:1 against [DarkLabel]. Whichever has the higher contrast wins.
 */
internal fun labelColorOn(fill: Color): Color =
    if (contrast(Color.White, fill) >= contrast(DarkLabel, fill)) Color.White else DarkLabel

@Preview
@Composable
private fun RoundCtaButtonPreview() {
    PreviewTheme {
        RoundCtaButton(
            modifier = androidx.compose.ui.Modifier,
            mainButtonData = previewMainButtonData,
        )
    }
}

@Preview
@Composable
private fun RoundCtaButtonWithPulsePreview() {
    PreviewTheme {
        RoundCtaButtonWithPulse(mainButtonData = previewMainButtonData)
    }
}

// Host-supplied activity colour — see [MainButtonData.color].
@Preview
@Composable
private fun RoundCtaButtonWithHostColorPreview() {
    PreviewTheme {
        RoundCtaButtonWithPulse(mainButtonData = previewMainButtonData.copy(color = Color(0xFF0D5097)))
    }
}

// A light host colour, which flips the label to dark — see [labelColorOn].
@Preview
@Composable
private fun RoundCtaButtonWithLightHostColorPreview() {
    PreviewTheme {
        RoundCtaButtonWithPulse(mainButtonData = previewMainButtonData.copy(color = Color(0xFF1CA8B0)))
    }
}
