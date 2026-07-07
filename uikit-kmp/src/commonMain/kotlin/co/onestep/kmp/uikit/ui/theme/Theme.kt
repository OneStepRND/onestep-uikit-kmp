package co.onestep.kmp.uikit.ui.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.designsystem.theme.OSColors
import co.onestep.designsystem.theme.Themes

/**
 * Whether the UI kit renders in light or dark colors.
 *
 * - [Light] / [Dark] force that appearance regardless of the OS setting.
 * - [System] follows the host platform's light/dark setting.
 *
 * The global default is [Light]: consumers who never call
 * [OneStepUiKit.setThemeMode] keep the exact light appearance the kit has always
 * shipped, so this is a non-breaking addition.
 */
enum class ThemeMode {
    Light,
    Dark,
    System,
}

/**
 * Global entry point for controlling the UI kit's appearance.
 *
 * Call [setThemeMode] once (e.g. at app start, or from a settings screen) to
 * force light/dark or follow the system. It is backed by Compose state, so any
 * [OneStepUiKitTheme] already on screen recomposes to the new mode.
 */
object OneStepUiKit {
    // shortcut: in-memory only. Persisting the choice across launches is the
    // consuming app's concern (it owns storage + HIPAA-safe prefs); the kit just
    // holds the current selection. Upgrade path: expose a saver if a consumer needs it.
    internal var themeModeState by mutableStateOf(ThemeMode.Light)
        private set

    /** The currently selected [ThemeMode] (defaults to [ThemeMode.Light]). */
    val themeMode: ThemeMode get() = themeModeState

    /** Globally force the UI kit into [mode]. */
    fun setThemeMode(mode: ThemeMode) {
        themeModeState = mode
    }
}

/**
 * True when the enclosing [OneStepUiKitTheme] is rendering dark colors. Owned by the
 * kit (not the design-system global flow), so it is correct per-subtree — including the
 * two independent halves of [PreviewTheme]. Defaults to `false` (light) outside a theme.
 */
internal val LocalUiKitDarkTheme = staticCompositionLocalOf { false }

// Light design-system neutral literals that non-composable data factories bake into
// TextData/StyledSegment colors. `#3E3D3B` is `neutral_p3`, `#716D69` is `neutral_p2`
// in the light theme (see DefaultLightPalette).
private val LightNeutralP3 = Color(0xFF3E3D3B)
private val LightNeutralP2 = Color(0xFF716D69)

/**
 * Remaps a color that a non-composable data factory baked in as a *light* neutral
 * literal to the matching [OSColors] role.
 *
 * In light mode ([isDark] false) the literal already equals the resolved role, so this
 * returns the color unchanged (no regression). In dark mode it returns the theme's role
 * color so factory-authored instruction/label text stays legible. Any color that isn't
 * one of the known baked neutrals is returned as-is.
 *
 * Apply this at text render sites that honour a factory-supplied color, e.g.
 * `textData.color?.adaptBakedNeutral(colors, isDark) ?: colors.neutral_p3`.
 */
fun Color.adaptBakedNeutral(colors: OSColors, isDark: Boolean): Color =
    if (!isDark) {
        this
    } else {
        when (this) {
            LightNeutralP3 -> colors.neutral_p3
            LightNeutralP2 -> colors.neutral_p2
            else -> this
        }
    }

/** Resolves a [ThemeMode] to a concrete "is dark" boolean for the current frame. */
@Composable
internal fun ThemeMode.isDark(): Boolean =
    when (this) {
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
        ThemeMode.System -> isSystemInDarkTheme()
    }

// The same design-system [OSColors] role→Material3 slot mapping is used for both
// light and dark; only the source theme (and the light/dark scheme baseline) differ,
// OSColors neutral fields form a scale where `neutral_p3` is the strongest foreground
// (dark text in light, near-white text in dark) and `neutral_m5` is the main surface
// (white in light, dark in dark). The light mapping below is the original, kept verbatim.
private fun lightColors(): ColorScheme {
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

// Dark mirrors the light role→slot mapping, sourced from the design-system's dark palette.
// NOTE: as of design-system-kmp 1.3.0 `osDarkTheme` is incomplete — its surface neutrals
// (neutral_m3/m4/m5) are still white and neutral_m4/m5 double as on-primary foreground, so
// dark mode does not yet render correct surfaces. The fix belongs in the design system; this
// mapping is intentionally symmetric with light so dark "just works" once that lands. See
// docs/design-system-dark-mode-requirements.md.
private fun darkColors(): ColorScheme {
    val colors = Themes.osDarkTheme(Themes.primaryOverride.value)
    return darkColorScheme(
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
    mode: ThemeMode = OneStepUiKit.themeMode,
    content: @Composable () -> Unit,
) {
    val dark = mode.isDark()
    val nunitoTypography = nunitoTypography()

    CompositionLocalProvider(
        LocalUiKitDarkTheme provides dark,
        LocalOSColors provides
            if (dark) {
                Themes.osDarkTheme(Themes.primaryOverride.value)
            } else {
                Themes.osLightTheme(Themes.primaryOverride.value)
            },
        LocalPalette provides if (dark) DefaultDarkPalette else DefaultLightPalette,
    ) {
        MaterialTheme(
            colorScheme = if (dark) darkColors() else lightColors(),
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

/**
 * Preview wrapper: renders [content] once in light and once in dark, stacked
 * vertically, each in its own forced-mode [OneStepUiKitTheme] with the matching
 * background. This gives every `@Preview` in the module light + dark coverage
 * without touching call sites. It does not read/write the global [OneStepUiKit]
 * mode, so the two halves stay independent.
 */
@Composable
fun PreviewTheme(
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        listOf(ThemeMode.Light, ThemeMode.Dark).forEach { mode ->
            OneStepUiKitTheme(mode = mode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background),
                ) {
                    content()
                }
            }
        }
    }
}
