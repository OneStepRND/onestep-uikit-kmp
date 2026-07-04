package co.onestep.kmp.uikit.features.recordFlow.destinations

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
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
fun EntryProviderScope<NavKey>.soundPermissionScreen(
    resourceProvider: ResourceProvider,
    onAskMicrophonePermission: () -> Unit,
    onSkip: () -> Unit,
    onGoToSettings: () -> Unit,
) {
    entry<SoundPermissionDestination> {
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

    entry<SoundPermissionDeniedAlwaysDestination> {
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
