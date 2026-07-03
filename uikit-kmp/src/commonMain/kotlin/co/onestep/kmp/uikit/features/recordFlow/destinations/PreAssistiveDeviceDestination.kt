package co.onestep.kmp.uikit.features.recordFlow.destinations

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import co.onestep.kmp.uikit.features.recordFlow.RecordFlowDataFactory
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.UiKitScreen
import co.onestep.kmp.uikit.models.OSTAssistiveDevice
import co.onestep.kmp.uikit.utils.UIktDestination
import kotlinx.serialization.Serializable

@Serializable
data object PreAssistiveDeviceDestination : UIktDestination

// Preview skipped: requires NavController

/**
 * Optional pre-recording clinical question: pick the [OSTAssistiveDevice] used for this recording.
 * Gated by [OSTRecordingConfiguration.showPreRecordingAssistiveDeviceSelection]. Mirrors the
 * Android uikit `preAssistiveDeviceScreen`.
 */
fun NavGraphBuilder.preAssistiveDeviceScreen(
    onDeviceSelected: (OSTAssistiveDevice) -> Unit,
) {
    composable<PreAssistiveDeviceDestination> {
        // Analytics (screen-view + assistive-device-selected) are fired by the NavGraph:
        // the screen-view via the route-keyed screen-view effect, the selection via the
        // onDeviceSelected callback. Kept here to avoid threading a tracker into leaf screens.
        UiKitScreen(
            modifier = Modifier,
            screenData = RecordFlowDataFactory.selectAssistiveDeviceScreenData(
                onSelection = onDeviceSelected,
            ),
        )
    }
}
