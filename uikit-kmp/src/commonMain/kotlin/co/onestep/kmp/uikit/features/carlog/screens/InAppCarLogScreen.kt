package co.onestep.kmp.uikit.features.carlog.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import co.onestep.kmp.uikit.testing.OSTTestTags
import co.onestep.kmp.uikit.utils.test
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.carlog.components.MeasurementLogItem
import co.onestep.kmp.uikit.features.carlog.components.PendingMeasurementLogItem
import co.onestep.kmp.uikit.features.carlog.models.CarLogItemData
import co.onestep.kmp.uikit.features.carlog.models.MeasurementItemData
import co.onestep.kmp.uikit.features.carlog.models.PendingMeasurementItemData
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.designsystem.theme.Variables

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun InAppCarLogScreen(
    modifier: Modifier = Modifier,
    items: List<CarLogItemData>,
    onClickItem: (String) -> Unit = {},
) {
    val groupedItems = items.groupBy { it.day }
    Box {
        LazyColumn(
            modifier =
                modifier
                    .fillMaxSize()
                    .test(OSTTestTags.CareLog.IN_APP_LIST),
        ) {
            groupedItems.forEach { (day, items) ->
                stickyHeader {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(LocalOSColors.current.neutral_m4),
                    ) {
                        OSText(
                            modifier =
                                Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 32.dp, top = Variables.GapL),
                            text = day,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.W700,
                        )
                    }
                }
                items(items) { item ->
                    when (item) {
                        is MeasurementItemData ->
                            MeasurementLogItem(
                                Modifier,
                                item,
                                onClickItem,
                            )

                        is PendingMeasurementItemData ->
                            PendingMeasurementLogItem(
                                Modifier,
                                item,
                            )
                    }
                }
            }
        }
    }
}
