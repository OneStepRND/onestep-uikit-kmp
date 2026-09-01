package co.onestep.kmp.uikit.features.summary.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.recordFlow.components.TOOLBAR
import co.onestep.kmp.uikit.features.recordFlow.components.ToolBarHeight
import co.onestep.kmp.uikit.features.recordFlow.components.Toolbar
import co.onestep.kmp.uikit.features.recordFlow.screensData.PrimaryButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData
import co.onestep.kmp.uikit.features.recordFlow.screensData.ToolBarData
import co.onestep.kmp.uikit.features.summary.collapsibleUtils.FixedScrollFlagState
import co.onestep.kmp.uikit.features.summary.collapsibleUtils.ScrollState
import co.onestep.kmp.uikit.features.summary.collapsibleUtils.ToolbarState
import co.onestep.kmp.uikit.features.summary.components.CollapsingLayout
import co.onestep.kmp.uikit.features.summary.components.ElevatedDivider
import co.onestep.kmp.uikit.features.summary.components.InfoBottomSheetContent
import co.onestep.kmp.uikit.features.summary.components.MainParamCircle
import co.onestep.kmp.uikit.features.summary.components.MainParamToolBar
import co.onestep.kmp.uikit.features.summary.components.OSTTabsRow
import co.onestep.kmp.uikit.features.summary.components.SummaryItemsList
import co.onestep.kmp.uikit.features.summary.components.SummaryShimmer
import co.onestep.kmp.uikit.features.summary.models.EmptyStateData
import co.onestep.kmp.uikit.features.summary.models.MainParamItem
import co.onestep.kmp.uikit.features.summary.models.SummaryListState
import co.onestep.kmp.uikit.features.summary.models.SummaryScreenItem
import co.onestep.kmp.uikit.ui.components.BottomSheet
import co.onestep.kmp.uikit.ui.components.BottomSheetData
import co.onestep.kmp.uikit.ui.components.FadingSurfaceToTransparent
import co.onestep.kmp.uikit.ui.components.PrimaryBrandButton
import co.onestep.designsystem.components.SecondaryButton
import co.onestep.designsystem.components.OSButtonSize
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit.testing.OSTTestTags
import co.onestep.kmp.uikit.utils.test
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.discard
import co.onestep.kmp.uikit_kmp.generated.resources.system_error_analysis_message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

val MinToolbarHeight = 108.dp
val MaxToolbarHeight = 355.dp
val MetaDataHeight = 50.dp
const val ListTopOffset = 120f
val MaxToolBarHeightNoTabs = MaxToolbarHeight - MetaDataHeight
val MinToolBarHeightNoTabs = MinToolbarHeight - MetaDataHeight
const val HIGHLIGHTS = 0
const val GAIT_LAB = 1
val BRAND_BUTTON_PADDING = 90.dp
@Deprecated("Moved to the OSTTestTags catalog", ReplaceWith("OSTTestTags.Summary.CONTINUE_BUTTON"))
const val SUMMARY_CONTINUE_BUTTON_TEST_TAG = OSTTestTags.Summary.CONTINUE_BUTTON
private val CollapsedCircleSize = 40.dp

@Composable
private fun rememberToolbarState(toolbarHeightRange: IntRange): ToolbarState =
    rememberSaveable(saver = ScrollState.Saver) {
        ScrollState(toolbarHeightRange)
    }

// Preview skipped: requires ViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Summary(
    modifier: Modifier = Modifier,
    insightsScreenState: SummaryListState,
    gaitLabScreenState: SummaryListState,
    partialScreenState: SummaryListState? = null,
    mainParamItem: MainParamItem?,
    toolBarData: ToolBarData,
    continueAction: SummaryAction? = null,
    secondaryAction: (() -> Unit)? = null,
    previewProgress: Float? = null,
    isLoading: Boolean,
    hallwayLengthText: String? = null,
    hallwayWarningText: String? = null,
    onHallwayEdit: () -> Unit = { },
    // STS manual-report entry: invoked when the pen icon is tapped. Null (or a non-editable
    // item) hides the pen. Gated upstream by the STS_MANUAL_REPORT flag.
    onEditSts: (() -> Unit)? = null,
    // Side-effect-only analytics hook: invoked with the tapped tab index when the user
    // switches tabs. Does not affect navigation/behavior.
    onTabSelected: (Int) -> Unit = { },
) {
    // Bottom Sheet state
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val showInfo = rememberSaveable { mutableStateOf(false) }
    val infoToShow = remember { mutableStateOf<@Composable () -> Unit>({}) }
    val sheetData = remember { BottomSheetData(showInfo, infoToShow, sheetState) }

    // Determine whether to show the horizontal pager and tabs.
    val showPagerAndTabs = mainParamItem?.showTabs == true

    val (maxToolbarHeight, minToolbarHeight) = calculateToolbarRange(showPagerAndTabs)

    val toolbarHeightRange =
        with(LocalDensity.current) {
            minToolbarHeight.roundToPx()..maxToolbarHeight.roundToPx()
        }
    val toolbarState = rememberToolbarState(toolbarHeightRange)
    val highlightsListState = rememberLazyListState()
    val gaitLabListState = rememberLazyListState()
    val pagerState =
        rememberPagerState(pageCount = {
            if (mainParamItem?.showTabs == true && mainParamItem.tabs.size > 1) 2 else 1
        })
    val scope = rememberCoroutineScope()
    val tabSwitchScope = rememberCoroutineScope()
    val currentDensity = LocalDensity.current
    // Bottom navigation-bar inset. The content region is edge-to-edge (see Scaffold's zeroed
    // contentWindowInsets below); the sticky Continue bar applies this inset itself, and the
    // scroll content reserves it so the last item can clear the floating bar + system nav bar.
    val navigationBarsBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // Track collapse progress for the small circle in toolbar (0 = expanded, 1 = collapsed)
    var collapseProgress by remember { mutableFloatStateOf(0f) }

    Scaffold(
        modifier = Modifier.test(OSTTestTags.Summary.SCREEN),
        // Edge-to-edge: the content draws behind the system nav bar (the fading sticky bar hides
        // it) so the nav-bar inset is applied exactly once — on the Continue bar — instead of
        // being reserved here as well, which previously pushed the button a nav-bar height too
        // high. The top inset still comes from the measured topBar.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompositionLocalProvider(LocalDensity provides currentDensity) {
                Crossfade(
                    targetState = toolBarData,
                    label = "toolbar",
                ) { data ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(LocalOSColors.current.neutral_m4)
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .height(ToolBarHeight.dp)
                            .test(OSTTestTags.Summary.TOOLBAR)
                            .then(modifier),
                    ) {
                        Toolbar(toolbarData = data)

                        mainParamItem?.takeIf { it.mainParamValue != null }?.let { paramItem ->
                            CompositionLocalProvider(
                                LocalDensity provides Density(
                                    density = LocalDensity.current.density,
                                    fontScale = 1f,
                                ),
                            ) {
                                SmallMainParamCircle(
                                    modifier =
                                        Modifier
                                            .align(Alignment.CenterStart)
                                            .padding(start = 40.dp)
                                            .alpha(collapseProgress),
                                    mainParamItem = paramItem,
                                )
                            }
                        }
                    }
                }
            }
        },
        content = { padding ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(LocalOSColors.current.neutral_m5)
                    .padding(padding),
            ) {
                CollapsingLayout(
                    modifier =
                        Modifier
                            .fillMaxWidth(),
                    onCollapseProgressChanged = { progress ->
                        collapseProgress = progress
                    },
                    collapsingTop = {
                        MainParamToolBar(
                            Modifier,
                            mainParamItem,
                            isLoading = isLoading,
                            progress = previewProgress ?: (1f - collapseProgress),
                            hallwayLengthText = hallwayLengthText,
                            hallwayWarningText = hallwayWarningText,
                            onHallwayEdit = onHallwayEdit,
                            onEditSts = onEditSts,
                            onLearnMore = { infoData ->
                                sheetData.show {
                                    InfoBottomSheetContent(
                                        data = infoData,
                                        onDismiss = { sheetData.hide() },
                                    )
                                }
                            }
                        )
                    },
                    bodyContent = {
                        Column(
                            modifier = modifier.fillMaxWidth(),
                        ) {
                            if (mainParamItem?.showTabs == true) {
                                ElevatedDivider()
                                CompositionLocalProvider(LocalDensity provides currentDensity) {
                                    OSTTabsRow(
                                        selectedTabIndex = pagerState.currentPage,
                                        tabs = mainParamItem.tabs,
                                        rowTestTag = OSTTestTags.Summary.TABS_ROW,
                                        tabTestTagPrefix = OSTTestTags.Summary.TAB_TAG_PREFIX,
                                        onTabSelected = {
                                            onTabSelected(it)
                                            tabSwitchScope.launch {
                                                pagerState.animateScrollToPage(it)
                                            }
                                        },
                                    )
                                }

                                HorizontalPager(
                                    state = pagerState,
                                    modifier =
                                        Modifier
                                            .fillMaxHeight()
                                            .background(LocalOSColors.current.neutral_m5)
                                            .padding(
                                                // Reserve the nav-bar inset (content is edge-to-edge) plus the
                                                // sticky Continue bar height so the last item clears both.
                                                bottom = navigationBarsBottom +
                                                    if (continueAction == null) 0.dp else BRAND_BUTTON_PADDING,
                                            ),
                                ) { page ->
                                    // In single-tab mode (showTabs = false), map page 0 to Gait Lab content
                                    val effectivePage =
                                        if (mainParamItem.tabs.size == 1) GAIT_LAB else page

                                    when (effectivePage) {
                                        HIGHLIGHTS -> {
                                            ItemsList(
                                                insightsScreenState,
                                                toolbarState,
                                                scope,
                                                highlightsListState,
                                                OSTTestTags.Summary.HIGHLIGHTS_LIST,
                                            ) {
                                                sheetData.show(it)
                                            }
                                        }

                                        GAIT_LAB ->
                                            ItemsList(
                                                gaitLabScreenState,
                                                toolbarState,
                                                scope,
                                                gaitLabListState,
                                                OSTTestTags.Summary.GAIT_LAB_LIST,
                                            ) {
                                                sheetData.show(it)
                                            }
                                    }
                                }
                            } else {
                                val noTabsState = when {
                                    mainParamItem == null -> insightsScreenState
                                    partialScreenState != null -> insightsScreenState
                                    insightsScreenState !is SummaryListState.Insights.Error -> insightsScreenState
                                    else -> SummaryListState.Insights.Error(
                                        EmptyStateData(
                                            subtitle = TextData(
                                                text = stringResource(Res.string.system_error_analysis_message),
                                                textSize = 18.sp,
                                                fontWeight = FontWeight.W400,
                                            ),
                                        )
                                    )
                                }
                                ItemsList(
                                    noTabsState,
                                    toolbarState,
                                    scope,
                                    highlightsListState,
                                    OSTTestTags.Summary.HIGHLIGHTS_LIST,
                                ) {
                                    sheetData.show(it)
                                }
                            }
                        }
                    },
                )

                BottomActionsSection(continueAction, secondaryAction, partialScreenState)

                if (sheetData.showBottomSheet()) {
                    BottomSheet(
                        sheetData = sheetData,
                        dragHandle = null,
                        testTag = OSTTestTags.Summary.INFO_SHEET,
                    )
                }
            }
        },
    )
}

@Composable
private fun BoxScope.BottomActionsSection(
    continueAction: SummaryAction?,
    secondaryAction: (() -> Unit)?,
    partialScreenState: SummaryListState?,
) {
    if (continueAction != null || secondaryAction != null) {
        Box(
            modifier =
                Modifier
                    .align(BottomCenter)
                    .background(Color.Transparent),
        ) {
            FadingSurfaceToTransparent(Modifier.height(100.dp))
            Column(
                modifier =
                    Modifier
                        .align(BottomCenter)
                        .background(Color.Transparent)
                        .windowInsetsPadding(WindowInsets.navigationBars),
            ) {
                secondaryAction?.let {
                    SecondaryButton(
                        text = stringResource(Res.string.discard),
                        onClick = secondaryAction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Variables.GapL)
                            .test(OSTTestTags.Summary.DISCARD_BUTTON),
                        size = OSButtonSize.Big,
                    )
                }
                continueAction?.let {
                    PrimaryBrandButton(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(Variables.GapL)
                                .test(OSTTestTags.Summary.CONTINUE_BUTTON),
                        data =
                            PrimaryButtonData(
                                text =
                                    TextData(
                                        continueAction.text,
                                        20.sp,
                                        FontWeight.W700,
                                    ),
                                action = continueAction.action,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemsList(
    screenState: SummaryListState,
    toolbarState: ToolbarState,
    scope: CoroutineScope,
    listState: LazyListState,
    listTestTag: String,
    onTugInfoClick: (@Composable () -> Unit) -> Unit,
) {
    AnimatedContent(
        targetState = screenState,
        label = "items list",
    ) { state ->
        when (state) {
            is SummaryListState.Insights.Success ->
                SummaryItems(
                    state.insightItems,
                    toolbarState,
                    scope,
                    listState,
                    listTestTag,
                    onTugInfoClick,
                )

            is SummaryListState.GaitLab.Success ->
                SummaryItems(
                    state.gaitLabItems,
                    toolbarState,
                    scope,
                    listState,
                    listTestTag,
                    onTugInfoClick,
                )

            is SummaryListState.Partial.Success -> {
                PartialSummaryScreen(
                    Modifier,
                    state,
                )
            }

            is SummaryListState.Insights.Error -> EmptyState(state.emptyStateData, listState)
            is SummaryListState.GaitLab.Error -> EmptyState(state.emptyStateData, listState)
            is SummaryListState.Insights.Loading,
            SummaryListState.GaitLab.Loading,
            SummaryListState.Partial.Loading,
            -> SummaryShimmer()
        }
    }
}

@Composable
private fun SummaryItems(
    items: List<SummaryScreenItem>,
    toolbarState: ToolbarState,
    scope: CoroutineScope,
    listState: LazyListState,
    listTestTag: String,
    onTugInfoClick: (@Composable () -> Unit) -> Unit,
) {
    SummaryItemsList(
        summaryItems = items,
        modifier =
            Modifier
                .fillMaxSize()
                .test(listTestTag)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { scope.coroutineContext.cancelChildren() },
                    )
                },
        listState = listState,
        contentPadding = PaddingValues(bottom = if (toolbarState is FixedScrollFlagState) MinToolbarHeight else 0.dp),
        onTugInfoClick = onTugInfoClick,
    )
}

@Composable
private fun SmallMainParamCircle(
    modifier: Modifier,
    mainParamItem: MainParamItem,
) {
    MainParamCircle(
        modifier = modifier,
        circleOffsetY = 0.dp,
        scoreOffsetY = 0.dp,
        circleOffsetX = 0.dp,
        circleSize = CollapsedCircleSize,
        circleStrokeWidth = 4.dp,
        position = 1f,
        scoreFontSize = 15.sp,
        animateMainParam = true,
        mainParam = mainParamItem.mainParamValue,
        mainParamText = "", // No text in collapsed state
        mainParamColor = mainParamItem.mainParamColor,
    )
}

private fun calculateToolbarRange(showPagerAndTabs: Boolean): Pair<Dp, Dp> {
    val maxToolbarHeight =
        if (showPagerAndTabs) {
            MaxToolbarHeight
        } else {
            MaxToolBarHeightNoTabs
        }

    val minToolbarHeight =
        if (showPagerAndTabs) {
            MinToolbarHeight
        } else {
            MinToolBarHeightNoTabs
        }
    return Pair(maxToolbarHeight, minToolbarHeight)
}
