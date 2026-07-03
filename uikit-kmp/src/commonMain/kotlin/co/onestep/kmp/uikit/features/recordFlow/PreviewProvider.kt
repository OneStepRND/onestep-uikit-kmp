package co.onestep.kmp.uikit.features.recordFlow

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.recordFlow.screensData.PrimaryButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.HallwayDistanceScreenState
import co.onestep.kmp.uikit.features.recordFlow.screensData.IconData
import co.onestep.kmp.uikit.features.recordFlow.screensData.MainButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.SecondaryButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.RecordingScreenData
import co.onestep.kmp.uikit.features.recordFlow.screensData.SelectionItemData
import co.onestep.kmp.uikit.features.recordFlow.screensData.SelectionListData
import co.onestep.kmp.uikit.features.recordFlow.screensData.SlideToStopButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TimerData
import co.onestep.kmp.uikit.features.recordFlow.screensData.ToolBarData
import co.onestep.kmp.uikit.features.recordFlow.screensData.UiKitScreenData
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.ic_back_arrow
import co.onestep.kmp.uikit_kmp.generated.resources.ic_barefoot
import co.onestep.kmp.uikit_kmp.generated.resources.ic_cane
import co.onestep.kmp.uikit_kmp.generated.resources.ic_clock_red_stars
import co.onestep.kmp.uikit_kmp.generated.resources.ic_close
import co.onestep.kmp.uikit_kmp.generated.resources.ic_connectivity_red_stars
import co.onestep.kmp.uikit_kmp.generated.resources.ic_info_black
import co.onestep.kmp.uikit_kmp.generated.resources.ic_info_circle
import co.onestep.kmp.uikit_kmp.generated.resources.ic_knee_red_stars
import co.onestep.kmp.uikit_kmp.generated.resources.ic_like_stars
import co.onestep.kmp.uikit_kmp.generated.resources.ic_play_button
import co.onestep.kmp.uikit_kmp.generated.resources.ic_routes_red_stars
import co.onestep.kmp.uikit_kmp.generated.resources.ic_server_red_stars
import co.onestep.kmp.uikit_kmp.generated.resources.ic_shoe_prints
import co.onestep.kmp.uikit_kmp.generated.resources.ic_tug_red_stars
import co.onestep.kmp.uikit_kmp.generated.resources.ic_volume_stars
import co.onestep.kmp.uikit_kmp.generated.resources.ic_warning_red_stars
import co.onestep.kmp.uikit_kmp.generated.resources.image_in_pocket
import co.onestep.kmp.uikit_kmp.generated.resources.image_in_thigh

// ── Toolbar mock data ────────────────────────────────────────────────────────

internal val toolbarData =
    ToolBarData(
        startIcon = IconData(Res.drawable.ic_back_arrow),
        title = TextData("Gait Lab Report", 16.sp, FontWeight.W400),
        endIcons = listOf(IconData(Res.drawable.ic_close)),
    )

internal val toolbarData1 =
    ToolBarData(
        startIcon = IconData(Res.drawable.ic_back_arrow),
        title = TextData("very very very long text in this line", 16.sp, FontWeight.W400),
        endIcons = listOf(IconData(Res.drawable.ic_close)),
    )

internal val toolbarData2 =
    ToolBarData(
        title = TextData("no left icon", 16.sp, FontWeight.W400),
        endIcons = listOf(IconData(Res.drawable.ic_close)),
    )

internal val toolbarData3 =
    ToolBarData(
        startIcon = IconData(Res.drawable.ic_back_arrow),
        title = TextData("no right icon", 16.sp, FontWeight.W400),
    )

internal val toolbarData4 =
    ToolBarData(
        startIcon = IconData(Res.drawable.ic_back_arrow),
        title = TextData("Many right icons", 16.sp, FontWeight.W400),
        endIcons = listOf(
            IconData(Res.drawable.ic_close),
            IconData(Res.drawable.ic_close),
            IconData(Res.drawable.ic_close),
        ),
    )

internal val toolbarDataList = listOf(toolbarData, toolbarData1, toolbarData2, toolbarData3, toolbarData4)

// ── Recording screen mock data ───────────────────────────────────────────────

internal val previewGetReadyRecordingScreen =
    RecordingScreenData(
        recordScreenStage = RecordingScreenData.RecordScreenStage.GET_READY,
        title = TextData("Get ready", 60.sp, FontWeight.Bold),
        instructions = TextData("Place the phone in the pocket, strap or against the thigh", 28.sp, FontWeight.Bold),
        timerValue = TimerData(
            text = TextData("10", 110.sp, FontWeight.ExtraBold),
            countdown = true,
        ),
        bottomButton = SecondaryButtonData(
            text = TextData("Start now", 18.sp, FontWeight.Bold),
            iconData = IconData(Res.drawable.ic_play_button) {},
        ) {},
    )

internal val previewRecordingScreen =
    RecordingScreenData(
        recordScreenStage = RecordingScreenData.RecordScreenStage.RECORDING,
        title = TextData("Go", 60.sp, FontWeight.W600),
        instructions = TextData("Recording in progress", 28.sp, FontWeight.W600),
        timerValue = TimerData(
            TextData("0:59", 115.sp, FontWeight.W600),
            countdown = true,
        ),
        slideToStopButton = SlideToStopButtonData(
            textData = TextData("Slide to stop", 18.sp, FontWeight.Normal),
        ) {},
    )

internal val previewAnalyzingScreen =
    RecordingScreenData(
        recordScreenStage = RecordingScreenData.RecordScreenStage.ANALYZING,
        title = TextData("Analyzing", 60.sp, FontWeight.W600),
        instructions = TextData("Analyzing in progress", 28.sp, FontWeight.W600),
    )

internal val recordingScreenDataList = listOf(previewGetReadyRecordingScreen, previewRecordingScreen, previewAnalyzingScreen)

// ── Hallway distance mock data ───────────────────────────────────────────────

internal val previewHallwayEmpty =
    HallwayDistanceScreenState(
        title = "Enter hallway length",
        subtitle = "Measure the length of your hallway or walking area.",
        unitText = "m",
        inputValue = "",
        errorText = null,
        canContinue = false,
        showShortHallwayDialog = false,
        recommendedValue = 30,
        suppressShortHallwayWarning = false,
    )

internal val previewHallwayError =
    HallwayDistanceScreenState(
        title = "Enter hallway length",
        subtitle = "Measure the length of your hallway or walking area.",
        unitText = "m",
        inputValue = "0",
        errorText = "Enter a value between 1 and 100 m.",
        canContinue = false,
        showShortHallwayDialog = false,
        recommendedValue = 30,
        suppressShortHallwayWarning = false,
    )

internal val previewHallwayValid =
    HallwayDistanceScreenState(
        title = "Last saved hallway length",
        subtitle = "Change if your testing area is different.",
        unitText = "ft",
        inputValue = "30",
        errorText = null,
        canContinue = true,
        showShortHallwayDialog = false,
        recommendedValue = 98,
        suppressShortHallwayWarning = false,
    )

// ── Button mock data ─────────────────────────────────────────────────────────

internal val previewPrimaryButtonData =
    PrimaryButtonData(
        text = TextData("Continue", 24.sp, FontWeight.W600),
        action = {},
    )

internal val previewPrimaryButtonData2 =
    PrimaryButtonData(
        text = TextData("View instructions", 24.sp, FontWeight.W600),
        action = {},
    )

internal val previewSecondaryButtonData =
    SecondaryButtonData(
        text = TextData("Continue", 24.sp, FontWeight.W600),
        action = {},
    )

internal val previewOutlineBrandButtonWithIcon =
    SecondaryButtonData(
        text = TextData("View instructions", 24.sp, FontWeight.W600),
        iconData = IconData(Res.drawable.ic_play_button),
        action = {},
    )

internal val previewOutlineBrandButtonInfo =
    SecondaryButtonData(
        text = TextData("View instructions", 24.sp, FontWeight.W600),
        iconData = IconData(Res.drawable.ic_info_black),
        action = {},
    )

internal val previewMainButtonData =
    MainButtonData(TextData("Start", 40.sp, FontWeight.Bold), buttonSize = 250.dp) {}

// ── Selection item mock data ─────────────────────────────────────────────────

internal val previewSelectionItemThigh =
    SelectionItemData(
        text = TextData("Against the thigh", 22.sp, FontWeight.W600),
        icon = IconData(Res.drawable.image_in_thigh),
    )

internal val previewSelectionItemPocket =
    SelectionItemData(
        text = TextData("in the pocket", 22.sp, FontWeight.W600),
        icon = IconData(Res.drawable.image_in_pocket),
    )

internal val previewSelectionItemDuration =
    SelectionItemData(
        itemHeight = 80.dp,
        text = TextData("1 minute", 24.sp, FontWeight.W600),
    )

// ── Screen data ──────────────────────────────────────────────────────────────

internal val recordFlowStartRecordScreenData =
    UiKitScreenData(
        mainButton = MainButtonData(
            text = TextData("Start", 40.sp, FontWeight.Bold),
        ) {},
        outlineBrandButton = SecondaryButtonData(
            text = TextData("View instructions", 24.sp, FontWeight.Normal),
            iconData = IconData(Res.drawable.ic_info_black),
        ) {},
    )

internal val noSummaryScreenData =
    UiKitScreenData(
        mainIcon = IconData(Res.drawable.ic_like_stars) {},
        title = TextData("Thank you for completing this measurement", 28.sp, FontWeight.Bold),
        brandButton = PrimaryButtonData(
            text = TextData("Next", 24.sp, FontWeight.Normal),
        ) {},
    )

internal val recordFlowPhonePositionScreenData =
    UiKitScreenData(
        title = TextData(
            "Where will the phone be placed?",
            28.sp,
            FontWeight.W700,
            textAlign = TextAlign.Start,
        ),
        selectionList = SelectionListData(
            items = listOf(
                SelectionItemData(
                    text = TextData("Against the thigh", 22.sp, FontWeight.W600),
                    icon = IconData(Res.drawable.image_in_thigh) {},
                ),
                SelectionItemData(
                    text = TextData("in the pocket", 22.sp, FontWeight.W600),
                    icon = IconData(Res.drawable.image_in_pocket) {},
                ),
            ),
            onItemSelected = {},
        ),
    )

internal val recordFlowWalkDurationScreenData =
    UiKitScreenData(
        title = TextData(
            "How long do you want to walk today?",
            28.sp,
            FontWeight.W700,
            textAlign = TextAlign.Start,
        ),
        selectionList = SelectionListData(
            items = listOf(
                SelectionItemData(text = TextData("1 minute", 22.sp, FontWeight.W600)),
                SelectionItemData(text = TextData("3 minute", 22.sp, FontWeight.W600)),
                SelectionItemData(text = TextData("5 minute", 22.sp, FontWeight.W600)),
            ),
            onItemSelected = {},
        ),
    )

internal val tooManyTurnsScreenData =
    UiKitScreenData(
        title = TextData("Your walk had too many turns.", 28.sp, FontWeight.Bold),
        subtitle = TextData(
            "For a successful analysis you need to walk in a straight line.",
            20.sp,
            FontWeight.Normal,
        ),
        mainIcon = IconData(Res.drawable.ic_routes_red_stars, tintColor = Color(0xFFB00404)),
        brandButton = PrimaryButtonData(
            text = TextData("Try again", 24.sp, FontWeight.W600),
            action = {},
        ),
        outlineBrandButton = SecondaryButtonData(
            text = TextData("View instructions", 24.sp, FontWeight.W600),
            iconData = IconData(Res.drawable.ic_info_circle),
            action = {},
        ),
    )

internal val errorConnectivityScreenData =
    UiKitScreenData(
        title = TextData("A connection issue", 28.sp, FontWeight.Bold),
        subtitle = TextData(
            "It seems you have internet connection issues. Check your internet connection and press reload.",
            20.sp,
            FontWeight.Normal,
        ),
        mainIcon = IconData(Res.drawable.ic_connectivity_red_stars),
        brandButton = PrimaryButtonData(
            text = TextData("Reload", 24.sp, FontWeight.W600),
            action = {},
        ),
    )

internal val serverIssueScreenData =
    UiKitScreenData(
        title = TextData("Oops...", 28.sp, FontWeight.Bold),
        subtitle = TextData(
            "There was a problem connecting to the server - please try again later.",
            20.sp,
            FontWeight.Normal,
        ),
        mainIcon = IconData(Res.drawable.ic_server_red_stars),
        brandButton = PrimaryButtonData(
            text = TextData("Try again", 24.sp, FontWeight.W600),
            action = {},
        ),
    )

internal val generalErrorScreenData =
    UiKitScreenData(
        title = TextData(
            "We need additional data in order to offer you meaningful insights",
            28.sp,
            FontWeight.Bold,
        ),
        subtitle = TextData(
            "Please make sure your phone is placed snugly against your thigh and follow the measurement instructions.",
            20.sp,
            FontWeight.Normal,
        ),
        brandButton = PrimaryButtonData(
            text = TextData("Try again", 24.sp, FontWeight.W600),
            action = {},
        ),
        mainIcon = IconData(Res.drawable.ic_warning_red_stars),
        outlineBrandButton = SecondaryButtonData(
            iconData = IconData(Res.drawable.ic_info_circle) {},
            text = TextData("View instructions", 24.sp, FontWeight.W600),
            action = {},
        ),
    )

internal val soundInstructionData =
    UiKitScreenData(
        mainIcon = IconData(Res.drawable.ic_volume_stars) {},
        title = TextData(
            "Make sure to increase your phone's volume so you can hear the instructions.",
            28.sp,
            FontWeight.Bold,
        ),
        brandButton = PrimaryButtonData(
            text = TextData("Continue", 24.sp, FontWeight.W600),
            action = {},
        ),
    )

internal val timeoutErrorScreenData =
    UiKitScreenData(
        title = TextData("This will take a while", 28.sp, FontWeight.Bold),
        subtitle = TextData(
            "Longer measurements take more time to analyze, so kick back and relax while we're crunching this.",
            20.sp,
            FontWeight.Normal,
        ),
        mainIcon = IconData(Res.drawable.ic_clock_red_stars),
        brandButton = PrimaryButtonData(
            text = TextData("Continue", 24.sp, FontWeight.W600),
            action = {},
        ),
    )

internal val minStepsScreenData =
    UiKitScreenData(
        title = TextData("Remember to try to walk at least 20 steps", 28.sp, FontWeight.Bold),
        mainIcon = IconData(Res.drawable.ic_shoe_prints),
        brandButton = PrimaryButtonData(
            text = TextData("Next", 24.sp, FontWeight.W600),
            action = {},
        ),
    )

private val dummyCustomTagsItems =
    listOf(
        SelectionItemData(text = TextData("Tag1", 22.sp, FontWeight.W600)),
        SelectionItemData(text = TextData("Tag2", 22.sp, FontWeight.W600)),
        SelectionItemData(text = TextData("Tag3", 22.sp, FontWeight.W600)),
    )

private val dummyAssistiveDeviceItems =
    listOf(
        SelectionItemData(
            text = TextData("Cane", 20.sp, FontWeight.W400),
            itemHeight = 80.dp,
            icon = IconData(Res.drawable.ic_cane, iconSize = 40.dp),
        ),
    )

private val dummyFootwearItems =
    listOf(
        SelectionItemData(
            text = TextData("Sneakers", 20.sp, FontWeight.W400),
            itemHeight = 80.dp,
            icon = IconData(Res.drawable.ic_barefoot, iconSize = 40.dp),
        ),
    )

internal val customTagsScreenData =
    UiKitScreenData(
        mainIcon = null,
        title = TextData("Custom Tags", 28.sp, FontWeight.W700, textAlign = TextAlign.Start),
        selectionList = SelectionListData(
            items = dummyCustomTagsItems,
            isMultiSelect = true,
        ) {},
        brandButton = PrimaryButtonData(
            text = TextData("Continue", 24.sp, FontWeight.W600),
            action = {},
        ),
    )

internal val selectAssistiveDeviceScreenData =
    UiKitScreenData(
        mainIcon = null,
        title = TextData(
            "What assistive device did you use?",
            28.sp,
            FontWeight.W700,
            textAlign = TextAlign.Start,
        ),
        selectionList = SelectionListData(
            items = dummyAssistiveDeviceItems,
        ) {},
    )

internal val selectFootwearScreenData =
    UiKitScreenData(
        mainIcon = null,
        title = TextData(
            "Which type of footwear was worn during the test?",
            28.sp,
            FontWeight.W700,
            textAlign = TextAlign.Start,
        ),
        selectionList = SelectionListData(
            items = dummyFootwearItems,
        ) {},
    )
