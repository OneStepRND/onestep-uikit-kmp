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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
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

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = LocalOSColors.current.primary_p3_main,
                contentColor = Color.White,
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
                    indication = ripple(bounded = true),
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
                color = Color.White,
                fontWeight = mainButtonData.text.fontWeight,
            )
        }
    }
}

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
