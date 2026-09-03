package co.onestep.kmp.uikit.features.recordFlow.destinations

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import co.onestep.kmp.uikit.features.recordFlow.RecordFlowDataFactory
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.UiKitScreen
import co.onestep.kmp.uikit.models.OSTActivityType
import co.onestep.kmp.uikit.testing.OSTTestTags
import co.onestep.kmp.uikit.utils.UIktDestination
import kotlinx.serialization.Serializable

@Serializable
data object SelectWalkDurationDestination : UIktDestination

// Preview skipped: requires NavController

/**
 * The shared duration-selection screen. [activityType] picks the option set it renders — the
 * walk tests' 1/3/5 minutes plus "long walk", or Static Balance's 10/20/30 seconds (OS-17175) —
 * and must be the same activity the tapped index is later resolved against.
 */
fun EntryProviderScope<NavKey>.selectWalkDurationScreen(
    activityType: OSTActivityType,
    recordingLimit: String,
    onPrimaryAction: (Int) -> Unit,
) {
    entry<SelectWalkDurationDestination> {
        UiKitScreen(
            modifier = Modifier,
            screenTag = OSTTestTags.RecordFlow.SELECT_DURATION_SCREEN,
            screenData = RecordFlowDataFactory.walkDurationSelectionScreenData(
                activityType = activityType,
                recordingLimit = recordingLimit,
                onSelection = onPrimaryAction,
            ),
        )
    }
}
