package co.onestep.kmp.uikit.features.recordFlow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import co.onestep.kmp.uikit.features.permissions.OSTPermissionMode
import co.onestep.kmp.uikit.features.permissions.PlatformPermissionFlow
import co.onestep.kmp.uikit.features.permissions.hasRequiredInAppPermissions
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingConfiguration
import co.onestep.kmp.uikit.features.recordFlow.screens.RecordFlowNavGraph
import co.onestep.kmp.uikit.models.OSTEvent
import co.onestep.kmp.uikit.ui.theme.OneStepUiKitTheme

private enum class FlowPhase { PERMISSION_FLOW, RECORDING_FLOW }

/**
 * Main composable entry point for the recording flow.
 * This is the primary CMP API — renders on both Android and iOS.
 *
 * Gates on in-app permissions before launching the recording flow.
 * If permissions are already granted, goes directly to recording (no flicker).
 * If not, shows the permission flow first; denied → calls onDismiss.
 *
 * @param config The recording configuration for this flow.
 * @param onResult Callback invoked when the recording flow produces an event (completion, error, etc.).
 * @param onDismiss Callback invoked when the user dismisses the flow.
 * @param shouldShowSoundInstructions Platform callback: returns true if device volume is low/silent.
 * @param onAskMicrophonePermission Platform callback: triggers system microphone permission dialog.
 * @param onGoToSettings Platform callback: opens device settings for microphone permission.
 */
@Composable
fun OSTRecordingFlow(
    config: OSTRecordingConfiguration,
    onResult: (OSTEvent) -> Unit,
    onDismiss: () -> Unit = {},
    shouldShowSoundInstructions: () -> Boolean = { false },
    onAskMicrophonePermission: () -> Unit = {},
    onGoToSettings: () -> Unit = {},
) {
    var phase by remember {
        mutableStateOf(
            if (hasRequiredInAppPermissions()) FlowPhase.RECORDING_FLOW
            else FlowPhase.PERMISSION_FLOW
        )
    }

    OneStepUiKitTheme {
        when (phase) {
            FlowPhase.PERMISSION_FLOW -> {
                PlatformPermissionFlow(
                    mode = OSTPermissionMode.IN_APP,
                    showExplanationScreen = config.showPermissionExplanationScreen,
                    onComplete = { granted ->
                        println("DEBUG OSTRecordingFlow: onComplete(granted=$granted) phase=$phase")
                        if (granted) {
                            phase = FlowPhase.RECORDING_FLOW
                        } else {
                            onDismiss()
                        }
                        println("DEBUG OSTRecordingFlow: phase set to $phase")
                    },
                    onDismiss = {
                        println("DEBUG OSTRecordingFlow: onDismiss (user closed permission flow)")
                        onDismiss()
                    },
                )
            }
            FlowPhase.RECORDING_FLOW -> {
                RecordFlowNavGraph(
                    config = config,
                    onResult = onResult,
                    onDismiss = onDismiss,
                    shouldShowSoundInstructions = shouldShowSoundInstructions,
                    onAskMicrophonePermission = onAskMicrophonePermission,
                    onGoToSettings = onGoToSettings,
                )
            }
        }
    }
}
