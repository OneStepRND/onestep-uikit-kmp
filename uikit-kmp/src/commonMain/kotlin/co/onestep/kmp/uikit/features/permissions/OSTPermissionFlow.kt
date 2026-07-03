package co.onestep.kmp.uikit.features.permissions

import androidx.compose.runtime.Composable
import co.onestep.kmp.uikit.ui.theme.OneStepUiKitTheme

/**
 * Composable entry point for the permission flow.
 *
 * @param mode The permission mode (IN_APP or BACKGROUND)
 * @param onComplete Callback invoked when permissions flow completes. [granted] is true if all required permissions were granted.
 */
@Composable
fun OSTPermissionFlow(
    mode: OSTPermissionMode = OSTPermissionMode.IN_APP,
    showExplanationScreen: Boolean = true,
    onComplete: (granted: Boolean) -> Unit,
) {
    OneStepUiKitTheme {
        PlatformPermissionFlow(
            mode = mode,
            showExplanationScreen = showExplanationScreen,
            onComplete = onComplete,
            onDismiss = { onComplete(false) },
        )
    }
}
