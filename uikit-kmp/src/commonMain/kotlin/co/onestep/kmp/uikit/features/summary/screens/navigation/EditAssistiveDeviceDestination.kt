package co.onestep.kmp.uikit.features.summary.screens.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.UiKitScreen
import co.onestep.kmp.uikit.features.recordFlow.screensData.UiKitScreenData
import co.onestep.kmp.uikit.utils.UIktDestination
import kotlinx.serialization.Serializable

@Serializable
internal data object EditAssistiveDeviceDestination : UIktDestination

internal fun EntryProviderScope<NavKey>.editAssistiveDeviceScreen(
    screenData: @Composable () -> UiKitScreenData,
    onBackPress: () -> Unit,
) {
    entry<EditAssistiveDeviceDestination> {
        UiKitScreen(
            modifier = Modifier.padding(top = 40.dp),
            screenData = screenData(),
            onBackPress = onBackPress,
        )
    }
}
