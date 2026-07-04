package co.onestep.kmp.uikit.features.recordFlow.destinations

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.preRecord.HallwayDistanceScreen
import co.onestep.kmp.uikit.features.recordFlow.screensData.HallwayDistanceScreenState
import co.onestep.kmp.uikit.utils.UIktDestination
import kotlinx.serialization.Serializable

@Serializable
data object HallwayDistanceDestination : UIktDestination

fun EntryProviderScope<NavKey>.hallwayDistanceScreen(
    stateProvider: @Composable () -> HallwayDistanceScreenState,
    onValueChange: (String) -> Unit,
    onContinue: () -> Unit,
    onContinueWithoutLength: () -> Unit,
) {
    entry<HallwayDistanceDestination> {
        val state = stateProvider()
        HallwayDistanceScreen(
            state = state,
            onValueChange = onValueChange,
            onContinue = onContinue,
            onContinueWithoutLength = onContinueWithoutLength,
        )
    }
}
