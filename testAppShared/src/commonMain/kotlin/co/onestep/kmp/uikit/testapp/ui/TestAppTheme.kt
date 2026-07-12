package co.onestep.kmp.uikit.testapp.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import co.onestep.kmp.uikit.ui.theme.OneStepUiKit
import co.onestep.kmp.uikit.ui.theme.ThemeMode

/**
 * Material theme for the harness's own screens (Home, Settings, pickers). Follows the same
 * [OneStepUiKit.themeMode] selector that drives the library flows, so the Light/Dark/System
 * chips on Home restyle the whole app consistently. The root [Surface] gives screens without
 * a Scaffold (Settings, clinician login) a themed background and content color.
 */
@Composable
fun TestAppTheme(content: @Composable () -> Unit) {
    val dark = when (OneStepUiKit.themeMode) {
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
        ThemeMode.System -> isSystemInDarkTheme()
    }
    MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            content()
        }
    }
}
