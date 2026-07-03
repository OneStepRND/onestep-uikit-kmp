package co.onestep.kmp.uikit.features.recordFlow.destinations

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import co.onestep.kmp.uikit.features.recordFlow.RecordFlowDataFactory
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.UiKitScreen
import co.onestep.kmp.uikit.models.OSTActivityType
import co.onestep.kmp.uikit.utils.UIktDestination
import kotlinx.serialization.Serializable

@Serializable
data object StartRecordDestination : UIktDestination

// Preview skipped: requires NavController

fun NavGraphBuilder.startRecordScreen(
    activityType: OSTActivityType,
    playAudio: ((String) -> Unit)? = null,
    primaryAction: () -> Unit,
    secondaryAction: () -> Unit,
    onBackPress: (() -> Unit)? = null,
) {
    composable<StartRecordDestination> {
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
