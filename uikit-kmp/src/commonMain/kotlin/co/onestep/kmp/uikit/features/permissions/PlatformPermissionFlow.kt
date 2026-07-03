package co.onestep.kmp.uikit.features.permissions

import androidx.compose.runtime.Composable

/**
 * Platform-specific permission flow implementation.
 *
 * - Android: Launches the full PermissionWizardViewModel + PermissionsFlowScreen flow
 * - iOS: Shows a placeholder (iOS permissions use HealthKit/CoreMotion APIs, not this flow)
 */
@Composable
internal expect fun PlatformPermissionFlow(
    mode: OSTPermissionMode,
    showExplanationScreen: Boolean = true,
    onComplete: (granted: Boolean) -> Unit,
    onDismiss: () -> Unit = {},
)
