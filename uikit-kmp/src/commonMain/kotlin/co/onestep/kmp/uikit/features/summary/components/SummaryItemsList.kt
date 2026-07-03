package co.onestep.kmp.uikit.features.summary.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.summary.models.SummaryScreenItem
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit.utils.summaryItems
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun SummaryItemsList(
    summaryItems: List<SummaryScreenItem>,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    onTugInfoClick: (@Composable () -> Unit) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyColumn(
        state = listState,
        contentPadding = contentPadding,
        modifier = modifier.background(LocalOSColors.current.neutral_m4),
    ) {
        items(
            items = summaryItems,
        ) { summaryItem ->
            when (summaryItem) {
                is SummaryScreenItem.TrendItem -> InsightCard(trendItem = summaryItem)
                is SummaryScreenItem.FallRiskItem -> InsightCard(trendItem = summaryItem)
                is SummaryScreenItem.EducationalItem -> InsightCard(trendItem = summaryItem)
                is SummaryScreenItem.InfoItem -> InsightCard(trendItem = summaryItem)
                is SummaryScreenItem.ParameterItem -> ParameterItem(parameterItem = summaryItem)
                is SummaryScreenItem.GaitLabItem -> GaitLabCard(gaitLabItems = summaryItem)
                is SummaryScreenItem.GaitLabCategoryItem -> CategoryTitle(summaryItem)
                is SummaryScreenItem.TugSummaryComponentItem ->
                    TugSummaryComponent(
                        Modifier,
                        summaryItem.tugComponentData,
                        onTugInfoClick,
                    )
            }
        }
    }
}

@Preview
@Composable
private fun SummaryItemsListPreview() {
    PreviewTheme {
        SummaryItemsList(
            summaryItems = summaryItems,
            onTugInfoClick = {},
        )
    }
}

@Composable
private fun CategoryTitle(summaryItem: SummaryScreenItem.GaitLabCategoryItem) {
    OSText(
        modifier =
            Modifier
                .padding(horizontal = Variables.GapL)
                .padding(top = Variables.GapL),
        text = summaryItem.text.text,
        lineHeight = 28.sp,
        maxLines = 1,
        fontSize = summaryItem.text.textSize,
        fontWeight = summaryItem.text.fontWeight,
    )
}
