package co.onestep.kmp.uikit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun FadingSurfaceToTransparent(
    modifier: Modifier = Modifier,
    fadeColor: Color? = null,
    direction: FadeDirection = FadeDirection.UP,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    if (direction == FadeDirection.UP) {
                                        Color.Transparent
                                    } else {
                                        fadeColor
                                            ?: LocalOSColors.current.neutral_m5
                                    },
                                    fadeColor ?: LocalOSColors.current.neutral_m5,
                                    fadeColor ?: LocalOSColors.current.neutral_m5,
                                    fadeColor ?: LocalOSColors.current.neutral_m5,
                                    fadeColor ?: LocalOSColors.current.neutral_m5,
                                    fadeColor ?: LocalOSColors.current.neutral_m5,
                                    fadeColor ?: LocalOSColors.current.neutral_m5,
                                    fadeColor ?: LocalOSColors.current.neutral_m5,
                                    if (direction == FadeDirection.DOWN) {
                                        Color.Transparent
                                    } else {
                                        fadeColor
                                            ?: LocalOSColors.current.neutral_m5
                                    },
                                ),
                        ),
                ),
    )
}

enum class FadeDirection {
    UP,
    DOWN,
}

@Preview
@Composable
private fun FadingSurfaceToTransparentPreview() {
    PreviewTheme {
        FadingSurfaceToTransparent()
    }
}
