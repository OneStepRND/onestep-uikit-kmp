package co.onestep.kmp.uikit.ui.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.designsystem.theme.Themes

private fun calculatePrimaryColors(): ColorScheme {
    val colors = Themes.osLightTheme(Themes.primaryOverride.value)
    return lightColorScheme(
        primary = colors.primary_0,
        onPrimary = colors.neutral_p3,
        primaryContainer = colors.primary_p3_main,
        onPrimaryContainer = colors.primary_m2,
        secondary = colors.neutral_m2,
        onSecondary = colors.neutral_p3,
        secondaryContainer = colors.neutral_p1,
        onSecondaryContainer = colors.neutral_m5,
        tertiary = colors.info_p1,
        onTertiary = colors.neutral_p3,
        background = colors.neutral_p3,
        onBackground = colors.neutral_m5,
        surface = colors.neutral_p3,
        onSurface = colors.neutral_m5,
        surfaceVariant = colors.neutral_p1,
        onSurfaceVariant = colors.neutral_m2,
        error = colors.error_0,
        onError = colors.neutral_p3,
        outline = colors.neutral_0,
        outlineVariant = colors.neutral_p1,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneStepUiKitTheme(
    content: @Composable () -> Unit,
) {
    val nunitoTypography = nunitoTypography()

    CompositionLocalProvider(
        LocalOSColors provides Themes.osLightTheme(Themes.primaryOverride.value),
        LocalPalette provides DefaultLightPalette,
    ) {
        MaterialTheme(
            colorScheme = calculatePrimaryColors(),
            typography = nunitoTypography,
        ) {
            // MUST be inside MaterialTheme: MaterialTheme itself does
            // `LocalIndication provides ripple()`, which would clobber ours if we provided it
            // outside. Overriding here makes our press feedback win for all content — Material
            // ripple on Android, a UIKit-style opacity dim on iOS. LocalIndication covers
            // clickables that read it (incl. design-system OSButton); LocalRippleConfiguration
            // covers Material3 components that draw their own ripple directly.
            CompositionLocalProvider(
                LocalIndication provides osClickIndication(),
                LocalRippleConfiguration provides osRippleConfiguration(),
            ) {
                content()
            }
        }
    }
}

@Composable
fun PreviewTheme(
    content: @Composable () -> Unit,
) {
    OneStepUiKitTheme(
        content = content,
    )
}
