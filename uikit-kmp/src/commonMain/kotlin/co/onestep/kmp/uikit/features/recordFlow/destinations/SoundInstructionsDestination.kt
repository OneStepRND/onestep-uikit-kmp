package co.onestep.kmp.uikit.features.recordFlow.destinations

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import co.onestep.kmp.uikit.features.recordFlow.RecordFlowDataFactory
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.UiKitScreen
import co.onestep.kmp.uikit.utils.UIktDestination
import kotlinx.serialization.Serializable

@Serializable
data object SoundInstructionsDestination : UIktDestination

// Preview skipped: requires NavController

fun NavGraphBuilder.soundInstructionsScreen(
    primaryAction: () -> Unit,
) {
    composable<SoundInstructionsDestination> {
        UiKitScreen(
            modifier = Modifier,
            screenData = RecordFlowDataFactory.soundInstructionData(
                onSelection = primaryAction,
            ),
        )
    }
}
