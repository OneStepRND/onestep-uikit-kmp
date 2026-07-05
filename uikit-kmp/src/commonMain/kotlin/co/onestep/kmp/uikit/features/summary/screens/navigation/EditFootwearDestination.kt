package co.onestep.kmp.uikit.features.summary.screens.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import co.onestep.kmp.uikit.features.recordFlow.components.ToolBarHeight
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.UiKitScreen
import co.onestep.kmp.uikit.features.recordFlow.screensData.UiKitScreenData
import co.onestep.kmp.uikit.utils.UIktDestination
import kotlinx.serialization.Serializable

@Serializable
internal data object EditFootwearDestination : UIktDestination

internal fun EntryProviderScope<NavKey>.editFootwearDestination(
    screenData: @Composable () -> UiKitScreenData,
    onBackPress: () -> Unit,
) {
    entry<EditFootwearDestination> {
        // The shared Toolbar overlay occupies (statusBars inset + ToolBarHeight); reserve the same
        // so the title clears it — a flat top padding let the toolbar cover the title on iOS, where
        // the top safe-area inset is large. Applied on a wrapping Box because UiKitScreen applies
        // its `modifier` to two nested layouts (a padding passed directly would be doubled).
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = ToolBarHeight.dp),
        ) {
            UiKitScreen(
                screenData = screenData(),
                onBackPress = onBackPress,
            )
        }
    }
}
