package co.onestep.kmp.uikit.features.recordFlow.destinations

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import co.onestep.kmp.uikit.features.recordFlow.RecordFlowDataFactory
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.UiKitScreen
import co.onestep.kmp.uikit.models.OSTActivityType
import co.onestep.kmp.uikit.utils.UIktDestination
import kotlinx.serialization.Serializable

@Serializable
data object StartRecordDestination : UIktDestination

// Preview skipped: requires NavController

fun EntryProviderScope<NavKey>.startRecordScreen(
    activityType: OSTActivityType,
    playAudio: ((String) -> Unit)? = null,
    primaryAction: () -> Unit,
    secondaryAction: () -> Unit,
    onBackPress: (() -> Unit)? = null,
) {
    entry<StartRecordDestination> {
        UiKitScreen(
            modifier = Modifier.fillMaxSize(),
            playAudio = playAudio,
            onBackPress = onBackPress,
            screenData = RecordFlowDataFactory.startRecordData(
                activityType = activityType,
                onMainButton = primaryAction,
                onBottomButton = secondaryAction,
            ),
        )
    }
}
