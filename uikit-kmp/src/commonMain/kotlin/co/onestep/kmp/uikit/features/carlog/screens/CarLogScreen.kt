package co.onestep.kmp.uikit.features.carlog.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import co.onestep.kmp.uikit.testing.OSTTestTags
import co.onestep.kmp.uikit.utils.test
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.carlog.models.CarLogScreenState
import co.onestep.kmp.uikit.features.carlog.models.InAppScreenState
import co.onestep.kmp.uikit.features.carlog.models.MeasurementItemData
import co.onestep.kmp.uikit.features.recordFlow.components.Toolbar
import co.onestep.kmp.uikit.features.recordFlow.screensData.ToolBarData
import co.onestep.kmp.uikit.features.summary.components.OSTTabData
import co.onestep.kmp.uikit.features.summary.components.OSTTabsRow
import co.onestep.kmp.uikit.models.OSTActivityType
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.background
import co.onestep.kmp.uikit_kmp.generated.resources.ic_walks
import co.onestep.kmp.uikit_kmp.generated.resources.in_app
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

const val IN_APP = 0
const val BACKGROUND = 1

@Preview
@Composable
private fun CarLogScreenPreview() {
    PreviewTheme {
        CarLogScreen(
            carLogScreenState = InAppScreenState.Content(
                carLogItems = listOf(
                    MeasurementItemData(
                        id = "1",
                        day = "Monday",
                        type = OSTActivityType.WALK,
                        title = "Walk",
                        time = "10:30 AM",
                        icon = Res.drawable.ic_walks,
                        mainParam = "Score: 85",
                        duration = "2 min 30 sec",
                    ),
                ),
                noticeCards = mutableListOf(),
            ),
            toolBarData = null,
        )
    }
}

@Composable
internal fun CarLogScreen(
    modifier: Modifier = Modifier,
    carLogScreenState: CarLogScreenState,
    backgroundScreenState: CarLogScreenState? = null,
    toolBarData: ToolBarData?,
    onClickItem: (String) -> Unit = {},
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    Box(modifier = modifier.test(OSTTestTags.CareLog.SCREEN)) {
        Column {
            toolBarData?.let {
                Toolbar(
                    modifier = Modifier.wrapContentHeight(),
                    toolbarData = toolBarData,
                )
            }
            Spacer(modifier = Modifier.fillMaxHeight(0.02f))
            AnimatedContent(
                targetState = backgroundScreenState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "",
            ) { bgTargetState ->
                when (bgTargetState) {
                    is CarLogScreenState.Loading -> Loading()
                    null ->
                        CareLogContentScreen(
                            modifier = Modifier.weight(1f),
                            carLogScreenState = carLogScreenState,
                            onClickItem = onClickItem,
                        )

                    else ->
                        TabbedCarLog(
                            Modifier.weight(1f),
                            pagerState,
                            scope,
                            carLogScreenState,
                            onClickItem,
                            bgTargetState,
                        )
                }
            }
        }
    }
}

@Composable
private fun Loading() {
    Box(Modifier.fillMaxSize()) {
        CarLogShimmer()
    }
}

@Composable
private fun TabbedCarLog(
    modifier: Modifier,
    pagerState: PagerState,
    scope: CoroutineScope,
    carLogScreenState: CarLogScreenState,
    onClickItem: (String) -> Unit,
    backgroundScreenState: CarLogScreenState,
) {
    Column {
        OSTTabsRow(
            rowTestTag = OSTTestTags.CareLog.TABS_ROW,
            tabTestTagPrefix = OSTTestTags.CareLog.TAB_TAG_PREFIX,
            // Default container (neutral_m4) + the row's own neutral_p3/neutral_p2 label colors,
            // matching the Summary tabs. The previous neutral_p3 container equalled the selected
            // label colour, so the active tab was invisible (worst in dark mode).
            selectedTabIndex = pagerState.currentPage,
            tabs =
                listOf(
                    OSTTabData(stringResource(Res.string.in_app), 0),
                    OSTTabData(stringResource(Res.string.background), 1),
                ),
            onTabSelected = {
                scope.launch {
                    pagerState.animateScrollToPage(it)
                }
            },
            textSize = 16.sp,
        )
        HorizontalPager(
            state = pagerState,
            modifier =
                Modifier
                    .fillMaxHeight(),
        ) { page ->
            when (page) {
                IN_APP ->
                    CareLogContentScreen(
                        modifier,
                        carLogScreenState = carLogScreenState,
                        onClickItem = onClickItem,
                    )

                BACKGROUND ->
                    CareLogContentScreen(
                        modifier,
                        carLogScreenState = backgroundScreenState,
                        onClickItem = onClickItem,
                    )
            }
        }
    }
}
