package co.onestep.kmp.uikit.features.recordFlow.destinations

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import co.onestep.kmp.uikit.features.recordFlow.RecordFlowDataFactory
import co.onestep.kmp.uikit.features.recordFlow.components.ToolBarHeight
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.UiKitScreen
import co.onestep.kmp.uikit.features.recordFlow.screensData.UiKitScreenData
import co.onestep.kmp.uikit.features.permissions.PermissionScreenData
import co.onestep.kmp.uikit.utils.ResourceProvider
import co.onestep.kmp.uikit.utils.UIktDestination
import kotlinx.serialization.Serializable

@Serializable
data object SoundPermissionDestination : UIktDestination

@Serializable
data object SoundPermissionDeniedAlwaysDestination : UIktDestination

/**
 * Sound permission screen using UiKitScreen for KMP compatibility.
 * Converts PermissionScreenData to UiKitScreenData to reuse the common screen composable.
 */
fun NavGraphBuilder.soundPermissionScreen(
    resourceProvider: ResourceProvider,
    onAskMicrophonePermission: () -> Unit,
    onSkip: () -> Unit,
    onGoToSettings: () -> Unit,
) {
    composable<SoundPermissionDestination> {
        val permData = RecordFlowDataFactory.soundPermissionData(
            resourceProvider = resourceProvider,
            onSelection = onAskMicrophonePermission,
            onSkip = onSkip,
        )
        UiKitScreen(
            modifier = Modifier.padding(top = ToolBarHeight.dp),
            screenData = permData.toUiKitScreenData(),
        )
    }

    composable<SoundPermissionDeniedAlwaysDestination> {
        val permData = RecordFlowDataFactory.soundPermissionDeniedAlwaysData(
            resourceProvider = resourceProvider,
            onGoToSettings = onGoToSettings,
            onSkip = onSkip,
        )
        UiKitScreen(
            modifier = Modifier.padding(top = ToolBarHeight.dp),
            screenData = permData.toUiKitScreenData(),
        )
    }
}

/**
 * Convert PermissionScreenData to UiKitScreenData for rendering in UiKitScreen.
 */
private fun PermissionScreenData.toUiKitScreenData() = UiKitScreenData(
    mainIcon = mainIcon,
    title = title,
    brandButton = brandButton,
    outlineBrandButton = outlineBrandButton,
)
