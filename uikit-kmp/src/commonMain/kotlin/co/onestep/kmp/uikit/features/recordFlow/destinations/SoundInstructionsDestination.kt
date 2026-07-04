package co.onestep.kmp.uikit.features.recordFlow.destinations

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import co.onestep.kmp.uikit.features.recordFlow.RecordFlowDataFactory
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.UiKitScreen
import co.onestep.kmp.uikit.utils.UIktDestination
import kotlinx.serialization.Serializable

@Serializable
data object SoundInstructionsDestination : UIktDestination

// Preview skipped: requires NavController

fun EntryProviderScope<NavKey>.soundInstructionsScreen(
    primaryAction: () -> Unit,
) {
    entry<SoundInstructionsDestination> {
        UiKitScreen(
            modifier = Modifier,
            screenData = RecordFlowDataFactory.soundInstructionData(
                onSelection = primaryAction,
            ),
        )
    }
}
