package co.onestep.kmp.uikit.features.summary.screens.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.UiKitScreen
import co.onestep.kmp.uikit.features.recordFlow.screensData.UiKitScreenData
import co.onestep.kmp.uikit.utils.UIktDestination
import kotlinx.serialization.Serializable

@Serializable
internal data object EditFootwearDestination : UIktDestination

internal fun NavGraphBuilder.editFootwearDestination(
    screenData: @Composable () -> UiKitScreenData,
    onBackPress: () -> Unit,
) {
    composable<EditFootwearDestination> {
        UiKitScreen(
            modifier = Modifier.padding(top = 40.dp),
            screenData = screenData(),
            onBackPress = onBackPress,
        )
    }
}
