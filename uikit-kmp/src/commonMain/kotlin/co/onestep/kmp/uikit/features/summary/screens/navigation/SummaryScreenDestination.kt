package co.onestep.kmp.uikit.features.summary.screens.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import co.onestep.kmp.uikit.features.recordFlow.screensData.ToolBarData
import co.onestep.kmp.uikit.features.summary.models.MainParamItem
import co.onestep.kmp.uikit.features.summary.models.SummaryListState
import co.onestep.kmp.uikit.features.summary.screens.Summary
import co.onestep.kmp.uikit.features.summary.screens.SummaryAction
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.utils.UIktDestination
import kotlinx.serialization.Serializable

@Serializable
internal data object SummaryScreenDestination : UIktDestination

internal fun NavGraphBuilder.summaryScreen(
    insightsScreenState: SummaryListState,
    gaitLabScreenState: SummaryListState,
    partialScreenState: SummaryListState? = null,
    toolBarData: ToolBarData,
    action: SummaryAction? = null,
    secondaryAction: (() -> Unit)? = null,
    mainParamItem: MainParamItem?,
    isLoading: Boolean,
    hallwayLengthText: String?,
    hallwayWarningText: String?,
    onHallwayEdit: () -> Unit,
    onEditSts: (() -> Unit)? = null,
    onTabSelected: (Int) -> Unit = { },
) {
    composable<SummaryScreenDestination> {
        Summary(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(LocalOSColors.current.neutral_m4),
            insightsScreenState = insightsScreenState,
            gaitLabScreenState = gaitLabScreenState,
            partialScreenState = partialScreenState,
            toolBarData = toolBarData,
            continueAction = action,
            secondaryAction = secondaryAction,
            mainParamItem = mainParamItem,
            isLoading = isLoading,
            hallwayLengthText = hallwayLengthText,
            hallwayWarningText = hallwayWarningText,
            onHallwayEdit = onHallwayEdit,
            onEditSts = onEditSts,
            onTabSelected = onTabSelected,
        )
    }
}
