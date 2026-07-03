package co.onestep.kmp.uikit.features.carlog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import co.onestep.kmp.uikit.di.UIKitServiceLocator
import co.onestep.kmp.uikit.features.carlog.models.NavigationCommand
import co.onestep.kmp.uikit.features.carlog.presentation.CareLogViewModel
import co.onestep.kmp.uikit.features.carlog.screens.CarLogScreen
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingConfiguration
import co.onestep.kmp.uikit.ui.theme.OneStepUiKitTheme

/**
 * Composable entry point for the Care Log screen.
 *
 * @param onClose Callback when the care log is dismissed.
 * @param onNavigateToRecording Callback when the user initiates a recording flow.
 * @param onNavigateToSummary Callback when the user taps a measurement to view its summary.
 * @param onNavigateToPermissions Callback when the user needs to grant permissions.
 */
@Composable
fun OSTCareLog(
    onClose: () -> Unit = {},
    onNavigateToRecording: (config: OSTRecordingConfiguration?) -> Unit = {},
    onNavigateToSummary: (measurementId: String) -> Unit = {},
    onNavigateToPermissions: () -> Unit = {},
) {
    val viewModel = remember {
        CareLogViewModel(
            resourceProvider = UIKitServiceLocator.resourceProvider,
            preferencesBridge = UIKitServiceLocator.preferencesBridge,
            recorderBridge = UIKitServiceLocator.recorderBridge,
            motionDataBridge = UIKitServiceLocator.motionDataBridge,
            sdkBridge = UIKitServiceLocator.sdkBridge,
        ).apply {
            setupToolBar(endAction = onClose)
        }
    }

    val inAppState by viewModel.inAppState.collectAsState()
    val backgroundScreenState by viewModel.backgroundScreenState.collectAsState()
    val navigationCommand by viewModel.navigationCommand.collectAsState()

    LaunchedEffect(navigationCommand) {
        when (val command = navigationCommand) {
            is NavigationCommand.ToRecordingFlow -> {
                onNavigateToRecording(command.recordingConfiguration)
                viewModel.clearNavigationCommand()
            }
            is NavigationCommand.ToSummary -> {
                onNavigateToSummary(command.motionMeasurementId)
                viewModel.clearNavigationCommand()
            }
            is NavigationCommand.ToPermissionFlow -> {
                onNavigateToPermissions()
                viewModel.clearNavigationCommand()
            }
            null -> { /* no-op */ }
        }
    }

    OneStepUiKitTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            CarLogScreen(
                modifier = Modifier.fillMaxSize(),
                carLogScreenState = inAppState,
                backgroundScreenState = backgroundScreenState,
                toolBarData = viewModel.toolbarState.value,
                onClickItem = { measurementId ->
                    viewModel.goToSummary(measurementId)
                },
            )
        }
    }
}
