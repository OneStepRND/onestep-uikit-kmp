package co.onestep.kmp.uikit.features.recordFlow.destinations

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import co.onestep.kmp.uikit.features.recordFlow.RecordFlowDataFactory
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.UiKitScreen
import co.onestep.kmp.uikit.testing.OSTTestTags
import co.onestep.kmp.uikit.utils.UIktDestination
import kotlinx.serialization.Serializable

@Serializable
data object SelectWalkDurationDestination : UIktDestination

// Preview skipped: requires NavController

fun EntryProviderScope<NavKey>.selectWalkDurationScreen(
    recordingLimit: String,
    onPrimaryAction: (Int) -> Unit,
) {
    entry<SelectWalkDurationDestination> {
        UiKitScreen(
            modifier = Modifier,
            screenTag = OSTTestTags.RecordFlow.SELECT_DURATION_SCREEN,
            screenData = RecordFlowDataFactory.walkDurationSelectionScreenData(
                recordingLimit = recordingLimit,
                onSelection = onPrimaryAction,
            ),
        )
    }
}
