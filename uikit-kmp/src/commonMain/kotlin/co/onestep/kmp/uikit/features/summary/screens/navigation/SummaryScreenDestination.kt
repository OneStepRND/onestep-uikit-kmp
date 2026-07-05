package co.onestep.kmp.uikit.features.summary.screens.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
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

/**
 * State is passed as provider lambdas (read INSIDE the entry content), not by value. NavDisplay
 * caches an entry's content closure per key, so values captured at entry-build time freeze on the
 * first (Loading) snapshot and never reflect the async summary load — the cause of the endless
 * shimmer. Reading the state inside the entry subscribes within the entry's own composition, so it
 * recomposes when the state changes (same convention as the record-flow `stateProvider` screens).
 */
internal fun EntryProviderScope<NavKey>.summaryScreen(
    insightsScreenState: () -> SummaryListState,
    gaitLabScreenState: () -> SummaryListState,
    partialScreenState: () -> SummaryListState? = { null },
    toolBarData: () -> ToolBarData,
    action: () -> SummaryAction? = { null },
    secondaryAction: (() -> Unit)? = null,
    mainParamItem: () -> MainParamItem?,
    isLoading: () -> Boolean,
    // Hallway strings are built with stringResource (composable) upstream, so they stay plain
    // values. They only feed the 6-min-walk hallway row (edge case) — no worse than before.
    hallwayLengthText: String?,
    hallwayWarningText: String?,
    onHallwayEdit: () -> Unit,
    onEditSts: (() -> Unit)? = null,
    onTabSelected: (Int) -> Unit = { },
) {
    entry<SummaryScreenDestination> {
        Summary(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(LocalOSColors.current.neutral_m4),
            insightsScreenState = insightsScreenState(),
            gaitLabScreenState = gaitLabScreenState(),
            partialScreenState = partialScreenState(),
            toolBarData = toolBarData(),
            continueAction = action(),
            secondaryAction = secondaryAction,
            mainParamItem = mainParamItem(),
            isLoading = isLoading(),
            hallwayLengthText = hallwayLengthText,
            hallwayWarningText = hallwayWarningText,
            onHallwayEdit = onHallwayEdit,
            onEditSts = onEditSts,
            onTabSelected = onTabSelected,
        )
    }
}
