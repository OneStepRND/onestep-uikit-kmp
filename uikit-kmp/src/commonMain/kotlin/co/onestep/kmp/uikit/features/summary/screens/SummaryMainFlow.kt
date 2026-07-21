package co.onestep.kmp.uikit.features.summary.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import co.onestep.kmp.uikit.di.UIKitServiceLocator
import co.onestep.kmp.uikit.features.recordFlow.analytics.RecordFlowAnalyticsEvents
import co.onestep.kmp.uikit.features.recordFlow.analytics.RecordFlowAnalyticsTracker
import co.onestep.kmp.uikit.features.recordFlow.components.ToolBarHeight
import co.onestep.kmp.uikit.features.recordFlow.components.Toolbar
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTPostTaggingData
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingQuestionData
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingConfiguration
import co.onestep.kmp.uikit.features.recordFlow.configurations.addSelectedAnswers
import co.onestep.kmp.uikit.features.recordFlow.configurations.effectivePostTaggingData
import co.onestep.kmp.uikit.features.recordFlow.configurations.removeSelectedAnswers
import co.onestep.kmp.uikit.features.recordFlow.destinations.CustomTagsDestination
import co.onestep.kmp.uikit.features.recordFlow.destinations.HallwayDistanceDestination
import co.onestep.kmp.uikit.features.recordFlow.destinations.NoSummaryNoticeDestination
import co.onestep.kmp.uikit.features.recordFlow.destinations.customTagsScreen
import co.onestep.kmp.uikit.features.recordFlow.destinations.hallwayDistanceScreen
import co.onestep.kmp.uikit.features.recordFlow.destinations.noSummaryNoticeScreen
import co.onestep.kmp.uikit.features.recordFlow.screensData.HallwayDistanceScreenState
import co.onestep.kmp.uikit.features.recordFlow.screensData.IconData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData
import co.onestep.kmp.uikit.features.recordFlow.screensData.ToolBarData
import co.onestep.kmp.uikit.features.recordFlow.screensData.filterHallwayDigits
import co.onestep.kmp.uikit.features.recordFlow.screensData.hallwayRange
import co.onestep.kmp.uikit.features.recordFlow.screensData.hallwayRecommended
import co.onestep.kmp.uikit.features.summary.SummaryDataFactory
import co.onestep.kmp.uikit.features.summary.analytics.SummaryAnalyticsEvents
import co.onestep.kmp.uikit.features.summary.models.OSTSummaryOptions
import co.onestep.kmp.uikit.features.summary.models.OSTSummaryOrigin
import co.onestep.kmp.uikit.features.summary.presentation.SummaryViewModel
import co.onestep.kmp.uikit.features.summary.screens.navigation.CustomQuestionDestination
import co.onestep.kmp.uikit.features.summary.screens.navigation.EditAssistiveDeviceDestination
import co.onestep.kmp.uikit.features.summary.screens.navigation.EditFootwearDestination
import co.onestep.kmp.uikit.features.summary.screens.navigation.EditLevelOfAssistanceDestination
import co.onestep.kmp.uikit.features.summary.screens.navigation.StsManualReportDestination
import co.onestep.kmp.uikit.features.summary.screens.navigation.SummaryScreenDestination
import co.onestep.kmp.uikit.features.summary.screens.navigation.TaggingScreenDestination
import co.onestep.kmp.uikit.features.summary.screens.navigation.stsManualReportScreen
import co.onestep.kmp.uikit.features.summary.screens.navigation.customQuestionDestination
import co.onestep.kmp.uikit.features.summary.screens.navigation.editAssistiveDeviceScreen
import co.onestep.kmp.uikit.features.summary.screens.navigation.editFootwearDestination
import co.onestep.kmp.uikit.features.summary.screens.navigation.editLevelOfAssistanceScreen
import co.onestep.kmp.uikit.features.summary.screens.navigation.summaryScreen
import co.onestep.kmp.uikit.features.summary.screens.navigation.taggingScreen
import co.onestep.kmp.uikit.features.tagging.models.Footwear
import co.onestep.kmp.uikit.features.tagging.models.Footwear.Companion.isFootwear
import co.onestep.kmp.uikit.features.tagging.models.Footwear.Companion.toFootwear
import co.onestep.kmp.uikit.models.OSTAssistiveDevice
import co.onestep.kmp.uikit.models.OSTAssistiveDevice.Companion.toAssistiveDevice
import co.onestep.kmp.uikit.models.OSTLevelOfAssistance
import co.onestep.kmp.uikit.models.OSTLevelOfAssistance.Companion.toLevelOfAssistance
import co.onestep.kmp.uikit.models.OSTUserInputMetaData
import co.onestep.kmp.uikit.navigation.UIktNavDisplay
import co.onestep.kmp.uikit.navigation.UIktNavSavedStateConfiguration
import co.onestep.kmp.uikit.navigation.pop
import co.onestep.kmp.uikit.utils.PlatformBackHandler
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.continue_camel_case
import co.onestep.kmp.uikit_kmp.generated.resources.finish
import co.onestep.kmp.uikit_kmp.generated.resources.hallway_length_error_range
import co.onestep.kmp.uikit_kmp.generated.resources.hallway_length_label
import co.onestep.kmp.uikit_kmp.generated.resources.hallway_length_title
import co.onestep.kmp.uikit_kmp.generated.resources.hallway_warning_stride_length
import co.onestep.kmp.uikit_kmp.generated.resources.ic_chevron_left
import co.onestep.kmp.uikit_kmp.generated.resources.ic_close
import co.onestep.kmp.uikit_kmp.generated.resources.ic_trash
import co.onestep.kmp.uikit_kmp.generated.resources.save_result
import co.onestep.kmp.uikit_kmp.generated.resources.summary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun SummaryMainFlow(
    modifier: Modifier = Modifier,
    summaryViewModel: SummaryViewModel,
    recorderBridge: co.onestep.kmp.uikit.bridge.RecorderBridge,
    motionMeasurementId: String,
    configuration: OSTRecordingConfiguration? = null,
    origin: OSTSummaryOrigin,
    backAction: () -> Unit,
) {
    val resourceProvider = UIKitServiceLocator.resourceProvider
    // Analytics tracker (null when the host provided no analytics handler → all calls no-op).
    val summaryTracker = UIKitServiceLocator.summaryAnalyticsTracker
    // app_section: the recording flow lands here from the host "Activities" area; the care
    // log lands here from the host "History" area. Matches uikit's app_section attribution.
    val appSection =
        if (origin == OSTSummaryOrigin.Recording) {
            SummaryAnalyticsEvents.AppSection.ACTIVITIES
        } else {
            SummaryAnalyticsEvents.AppSection.HISTORY
        }
    // Summary tab/param state is read INSIDE the summary entry via provider lambdas (see
    // summaryScreen) — not hoisted here — so NavDisplay entry caching can't freeze it on the
    // initial Loading snapshot.
    val motionMeasurement = summaryViewModel.motionMeasurement.value
    val mainParamCardItem by summaryViewModel.mainParamCardItem
    val coroutineScope = rememberCoroutineScope()
    val showDeleteMeasurementConfirmation = remember { mutableStateOf(false) }

    val fullSummary =
        remember {
            configuration?.showSummaryScreen == null || configuration.showSummaryScreen == OSTSummaryOptions.Full
        }

    // Navigation 3 back stack owned by the summary flow; saved via the shared uikit
    // serializers module (iOS has no reflection-based fallback).
    val backStack = rememberNavBackStack(
        UIktNavSavedStateConfiguration,
        if (fullSummary) SummaryScreenDestination else NoSummaryNoticeDestination,
    )
    val currentKey: NavKey? = backStack.lastOrNull()
    val tagBackRequests = remember { MutableSharedFlow<Unit>() }
    var assistiveDevice by remember { mutableStateOf<OSTAssistiveDevice?>(null) }
    var levelOfAssistance by remember { mutableStateOf<OSTLevelOfAssistance?>(null) }
    var footwear by remember { mutableStateOf<Footwear?>(null) }
    val hallwayState by summaryViewModel.hallwayState
    val postTaggingData by remember {
        mutableStateOf<OSTPostTaggingData?>(configuration?.effectivePostTaggingData())
    }
    val postTaggingQuestions = postTaggingData?.questions
    var note by remember { mutableStateOf<String?>(null) }
    var currentQuestion by remember { mutableStateOf<OSTRecordingQuestionData?>(null) }
    val toolbarText = stringResource(Res.string.summary)
    // Provider (not a captured value): the summary entry reads this INSIDE its content so the
    // trash icon reflects mainParamCardItem as it loads (see summaryScreen's docs on NavDisplay
    // entry caching). Reads mainParamCardItem.value inside → subscribes within the entry.
    val toolBarDataProvider: () -> ToolBarData = {
        ToolBarData(
            startIcon =
                IconData(Res.drawable.ic_chevron_left) {
                    if (backStack.lastOrNull() == CustomTagsDestination) {
                        coroutineScope.launch {
                            tagBackRequests.emit(Unit)
                        }
                    } else {
                        backAction()
                    }
                },
            title =
                TextData(
                    toolbarText,
                    16.sp,
                    FontWeight.W400,
                ),
            endIcons =
                if (summaryViewModel.mainParamCardItem.value?.showTrashIcon == true) {
                    listOf(
                        IconData(Res.drawable.ic_trash) {
                            // Clicked: discard_measurement — the trash icon on the summary
                            // toolbar (before the confirmation dialog), matching uikit.
                            summaryViewModel.motionMeasurement.value?.let {
                                summaryTracker?.trackDiscardMeasurementClicked(it)
                            }
                            showDeleteMeasurementConfirmation.value = true
                        },
                    )
                } else {
                    emptyList()
                },
        )
    }

    // Hallway display values from ViewModel state
    val hallwayLengthText =
        if (hallwayState.isSixMinuteWalk) {
            hallwayState.hallwayLength?.let {
                stringResource(Res.string.hallway_length_label, it, hallwayState.hallwayUnitText)
            }
        } else {
            null
        }

    val hallwayWarningText =
        if (hallwayState.hallwayWarningActive) {
            stringResource(Res.string.hallway_warning_stride_length)
        } else {
            null
        }

    val isImperial = summaryViewModel.isImperialSystem()
    val editHallwayState =
        HallwayDistanceScreenState(
            title = stringResource(Res.string.hallway_length_title),
            unitText = hallwayState.hallwayUnitText,
            inputValue = hallwayState.editValue,
            errorText = hallwayState.editError,
            canContinue =
                hallwayState.editValue.toIntOrNull()?.let {
                    val (min, max) = hallwayRange(isImperial)
                    it in min..max
                } ?: false,
            showShortHallwayDialog = false,
            recommendedValue = hallwayRecommended(isImperial, motionMeasurement?.type),
            suppressShortHallwayWarning = false,
            fromSummary = true,
        )

    LaunchedEffect(summaryViewModel.motionMeasurement.value) {
        summaryViewModel.partialScreenState.value = null
        assistiveDevice = motionMeasurement?.metadata?.assistiveDevice?.toAssistiveDevice()
        levelOfAssistance = motionMeasurement?.metadata?.levelOfAssistance?.toLevelOfAssistance()
        footwear =
            motionMeasurement
                ?.metadata
                ?.tags
                ?.firstOrNull { it.isFootwear(resourceProvider) }
                ?.toFootwear(resourceProvider)
        note = motionMeasurement?.metadata?.note
        summaryViewModel.updateHallwayState()
        // screen: activity_summary — fires once the analyzed measurement is loaded (the
        // Highlights tab is the default landing tab), mirroring uikit's screen-view point.
        motionMeasurement?.let { summaryTracker?.trackActivitySummaryScreen(it, appSection) }
    }

    val recordFlowTracker = UIKitServiceLocator.recordFlowAnalyticsTracker

    // screen: measurement_add_tags — fired when the post-measurement tagging screen or the
    // post-tag questions flow becomes current, matching uikit's post-measurement tag screen.
    LaunchedEffect(currentKey) {
        if (currentKey == TaggingScreenDestination || currentKey == CustomTagsDestination) {
            motionMeasurement?.type?.let { type ->
                recordFlowTracker?.trackAddTagsScreen(type, motionMeasurement.id)
            }
        }
    }

    PlatformBackHandler {
        if (!backStack.pop()) {
            backAction()
        }
    }

    Box(
        modifier
            .fillMaxSize(),
    ) {
        UIktNavDisplay(
            backStack = backStack,
            onBack = { backStack.pop() },
            entryProvider = entryProvider {
            summaryScreen(
                insightsScreenState = { summaryViewModel.insightsScreenState.value },
                gaitLabScreenState = { summaryViewModel.gatLabScreenState.value },
                partialScreenState = { summaryViewModel.partialScreenState.value },
                toolBarData = toolBarDataProvider,
                action = {
                    when {
                        origin != OSTSummaryOrigin.Recording -> null
                        summaryViewModel.partialScreenState.value != null -> {
                            SummaryAction(
                                text = resourceProvider.getString(Res.string.save_result),
                                action = backAction,
                            )
                        }

                        configuration?.postTaggingData is OSTPostTaggingData.None -> {
                            SummaryAction(
                                text = resourceProvider.getString(Res.string.finish),
                                action = backAction,
                            )
                        }

                        configuration?.postTaggingData is OSTPostTaggingData.OSTPostTaggingQuestionsFlow -> {
                            SummaryAction(
                                text = resourceProvider.getString(Res.string.continue_camel_case),
                                action = { backStack.add(CustomTagsDestination) },
                            )
                        }

                        configuration?.postTaggingData is OSTPostTaggingData.OSTPostTaggingScreen -> {
                            SummaryAction(
                                text = resourceProvider.getString(Res.string.continue_camel_case),
                                action = { backStack.add(TaggingScreenDestination) },
                            )
                        }

                        else -> null
                    }
                },
                secondaryAction = null,
                mainParamItem = { summaryViewModel.mainParamCardItem.value },
                isLoading = { summaryViewModel.isLoading.value },
                hallwayLengthText = hallwayLengthText,
                hallwayWarningText = hallwayWarningText,
                onHallwayEdit = {
                    summaryViewModel.showHallwayEditDialog()
                    backStack.add(HallwayDistanceDestination)
                },
                // Clicked: activity_summary_tab + the gait_data screen-view. uikit fires these
                // ONLY for the Gait Lab tab (index != 0), never for the Highlights tab
                // (CollapsibleSummaryScreen.kt:239). Side-effect only; the pager still drives
                // navigation.
                onTabSelected = { index ->
                    if (index != 0) {
                        summaryViewModel.motionMeasurement.value?.let { m ->
                            summaryTracker?.trackActivitySummaryTabClicked(
                                m,
                                SummaryAnalyticsEvents.Tabs.GAIT_DATA,
                            )
                            summaryTracker?.trackGaitDataScreen(m)
                        }
                    }
                },
                // STS manual-report entry from the summary pen icon. Non-null only when the
                // main-param item is editable (STS + STS_MANUAL_REPORT flag on). Navigates to the
                // manual-report destination with the current reps pre-selected on the wheel.
                onEditSts = mainParamCardItem
                    ?.takeIf { it.editable }
                    ?.let {
                        {
                            val uuid = motionMeasurement?.id
                            if (uuid != null) {
                                val currentReps = mainParamCardItem?.mainParamValue?.toInt()
                                backStack.add(
                                    StsManualReportDestination(uuid = uuid, initialValue = currentReps),
                                )
                            }
                        }
                    },
            )
            stsManualReportScreen(
                onSubmitted = { _ ->
                    backStack.pop()
                    summaryViewModel.updateMotionMeasurement()
                },
                onClose = { backStack.pop() },
                onExitOnFailure = { backAction() },
                // Summary-path (pen) manual entry emits NO enter_results_manually analytics,
                // matching uikit (which leaves this path un-instrumented — its SCREEN_ORIGIN_SUMMARY
                // constant is defined but unused). The error-path events remain wired in RecordFlowNavGraph.
            )
            taggingScreen(
                getAssistiveDevice = { assistiveDevice },
                getLevelOfAssistance = { levelOfAssistance },
                getFootwear = { footwear },
                postTaggingData = postTaggingData as? OSTPostTaggingData.OSTPostTaggingScreen,
                getNote = { note },
                onEditAssistiveDeviceClicked = { newNote: String? ->
                    note = newNote
                    backStack.add(EditAssistiveDeviceDestination)
                },
                onEditLevelOfAssistanceClicked = { newNote: String? ->
                    note = newNote
                    backStack.add(EditLevelOfAssistanceDestination)
                },
                onEditFootwearClicked = { newNote: String? ->
                    note = newNote
                    backStack.add(EditFootwearDestination)
                },
                onGoToQuestionsClicked = { newNote: String?, question ->
                    note = newNote
                    currentQuestion = question
                    backStack.add(CustomQuestionDestination)
                },
                action = { kmpUserInputMetaData ->
                    // Clicked: measurement_submit_tags (post-measurement path). Emits the typed
                    // selections only — NEVER the free-text clinician note (HIPAA).
                    motionMeasurement?.type?.let { type ->
                        recordFlowTracker?.trackSubmitTagsClicked(
                            activity = type,
                            source = RecordFlowAnalyticsEvents.TagSource.POST_MEASUREMENT,
                            perceptionUuid = motionMeasurement.id,
                            assistiveDevice = kmpUserInputMetaData.assistiveDevice ?: assistiveDevice,
                            footwear = footwear,
                        )
                    }
                    updateMetaData(
                        resourceProvider,
                        recorderBridge,
                        motionMeasurementId,
                        summaryViewModel,
                        motionMeasurement?.metadata?.tags ?: emptyList(),
                        kmpUserInputMetaData,
                        coroutineScope,
                        backAction,
                    )
                },
            )
            editAssistiveDeviceScreen(
                screenData = {
                    SummaryDataFactory.selectAssistiveDevice {
                        assistiveDevice = it
                        backStack.pop()
                    }
                },
                onBackPress = { backStack.pop() },
            )
            editLevelOfAssistanceScreen(
                screenData = {
                    SummaryDataFactory.selectLevelOfAssistance {
                        levelOfAssistance = it
                        backStack.pop()
                    }
                },
                onBackPress = { backStack.pop() },
            )
            editFootwearDestination(
                screenData = {
                    SummaryDataFactory.selectFootwear {
                        footwear = it
                        backStack.pop()
                    }
                },
                onBackPress = { backStack.pop() },
            )
            customQuestionDestination(
                questionProvider = { currentQuestion },
                onItemSelected = { itemSelected ->
                    postTaggingQuestions?.addSelectedAnswers(itemSelected)
                    backStack.pop()
                },
                onBackPress = { backStack.pop() },
            )
            customTagsScreen(
                topBarPadding = ToolBarHeight,
                preRecordingQuestions = configuration?.postTaggingData?.questions,
                onAddTags = { tags ->
                    postTaggingQuestions?.addSelectedAnswers(tags)
                },
                onRemoveTags = { tagsToRemove ->
                    postTaggingQuestions?.removeSelectedAnswers(tagsToRemove)
                },
                onToolbarBackRequest = tagBackRequests,
                onBack = {
                    backStack.pop()
                },
                onDone = { _ ->
                    // Clicked: measurement_submit_tags (post-measurement questions-flow path).
                    // Typed selections only; the free-text note is never sent (HIPAA).
                    motionMeasurement?.type?.let { type ->
                        recordFlowTracker?.trackSubmitTagsClicked(
                            activity = type,
                            source = RecordFlowAnalyticsEvents.TagSource.POST_MEASUREMENT,
                            perceptionUuid = motionMeasurement.id,
                            assistiveDevice = assistiveDevice,
                            footwear = footwear,
                        )
                    }
                    updateMetaData(
                        resourceProvider,
                        recorderBridge,
                        motionMeasurementId,
                        summaryViewModel,
                        motionMeasurement?.metadata?.tags ?: emptyList(),
                        OSTUserInputMetaData(
                            tags =
                                postTaggingQuestions?.flatMap {
                                    it.selectedAnswers ?: emptyList()
                                },
                            levelOfAssistance = levelOfAssistance,
                        ),
                        coroutineScope,
                        backAction,
                    )
                },
            )
            noSummaryNoticeScreen {
                when {
                    configuration?.postTaggingData == null -> backAction()
                    configuration.postTaggingData is OSTPostTaggingData.None -> backAction()
                    configuration.postTaggingData is OSTPostTaggingData.OSTPostTaggingQuestionsFlow -> {
                        backStack.add(CustomTagsDestination)
                    }

                    configuration.postTaggingData is OSTPostTaggingData.OSTPostTaggingScreen -> {
                        backStack.add(TaggingScreenDestination)
                    }
                }
            }
            hallwayDistanceScreen(
                stateProvider = { editHallwayState },
                onValueChange = { rawValue ->
                    summaryViewModel.updateHallwayEditValue(filterHallwayDigits(rawValue))
                },
                onContinue = {
                    val value = hallwayState.editValue.toIntOrNull()
                    val (min, max) = hallwayRange(isImperial)
                    if (value == null || value !in min..max) {
                        summaryViewModel.setHallwayEditError(
                            resourceProvider.getString(
                                Res.string.hallway_length_error_range,
                                min,
                                max,
                                hallwayState.hallwayUnitText,
                            )
                        )
                    } else {
                        // summary_edit_hallway — the clinician saved a new hallway length on
                        // the summary. original = the value shown before the edit, updated =
                        // the newly entered value. Matches uikit's trackEditHallway.
                        motionMeasurement?.type?.let { type ->
                            summaryTracker?.trackEditHallway(
                                activity = type,
                                original = hallwayState.hallwayLength ?: 0,
                                updated = value,
                            )
                        }
                        summaryViewModel.updateSixMinuteWalkCourseLength(
                            uuid = motionMeasurementId,
                            value = value,
                        )
                        backStack.pop()
                    }
                },
                onContinueWithoutLength = {
                    summaryViewModel.hideHallwayEditDialog()
                    backStack.pop()
                },
            )
            },
        )
        if (currentKey != SummaryScreenDestination) {
            Toolbar(
                toolbarData =
                    generateToolBar(
                        currentKey,
                        onPop = { backStack.pop() },
                        onTagBackPress = {
                            coroutineScope.launch {
                                tagBackRequests.emit(Unit)
                            }
                        },
                        backAction = backAction,
                    ),
            )
        }
    }

    // Delete measurement confirmation dialog
    if (showDeleteMeasurementConfirmation.value) {
        BasicAlertDialog(
            onDismissRequest = { showDeleteMeasurementConfirmation.value = false },
            content = {
                DeleteMeasurementConfirmationDialog(
                    onDismissClicked = { showDeleteMeasurementConfirmation.value = false },
                    onDeleteClicked = {
                        motionMeasurement?.id?.let {
                            // screen: measurement_deleted — the SDK-owned point closest to the
                            // spec's "Measurement deleted" screen is deletion confirmation.
                            summaryTracker?.trackMeasurementDeleted(motionMeasurement)
                            summaryViewModel.deleteMotionMeasurement(it)
                            backAction()
                            showDeleteMeasurementConfirmation.value = false
                        }
                    },
                )
            },
        )
    }

}

private fun updateMetaData(
    resourceProvider: co.onestep.kmp.uikit.utils.ResourceProvider,
    recorderBridge: co.onestep.kmp.uikit.bridge.RecorderBridge,
    motionMeasurementId: String,
    summaryViewModel: SummaryViewModel,
    currentTags: List<String>,
    userInputMetaData: OSTUserInputMetaData,
    coroutineScope: CoroutineScope,
    backAction: () -> Unit,
) {
    val newTags = userInputMetaData.tags ?: emptyList()

    val mutableCurrentTags =
        when {
            // If user has footwear and removes it or chooses NONE, remove all footwear tags
            newTags.isEmpty() ->
                currentTags
                    .filterNot { it.isFootwear(resourceProvider) }
                    .toMutableList()

            else -> currentTags.toMutableList()
        }

    val metadata =
        OSTUserInputMetaData(
            note = userInputMetaData.note,
            tags = mutableCurrentTags + newTags,
            assistiveDevice = userInputMetaData.assistiveDevice,
            levelOfAssistance = userInputMetaData.levelOfAssistance,
        )

    coroutineScope.launch {
        recorderBridge.updateMotionMeasurement(
            motionMeasurementId,
            metadata,
        )
        summaryViewModel.updateMotionMeasurement()
        backAction()
    }
}

@Composable
private fun generateToolBar(
    currentKey: NavKey?,
    onPop: () -> Unit,
    onTagBackPress: () -> Unit,
    backAction: () -> Unit,
) = ToolBarData(
    startIcon =
        IconData(Res.drawable.ic_chevron_left) {
            if (currentKey == CustomTagsDestination) onTagBackPress() else onPop()
        },
    endIcons = listOf(IconData(Res.drawable.ic_close) { backAction() }),
)
