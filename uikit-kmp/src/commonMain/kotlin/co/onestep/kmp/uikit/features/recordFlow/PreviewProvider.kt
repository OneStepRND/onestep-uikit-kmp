package co.onestep.kmp.uikit.features.recordFlow

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.recordFlow.screensData.HallwayDistanceScreenState
import co.onestep.kmp.uikit.features.recordFlow.screensData.IconData
import co.onestep.kmp.uikit.features.recordFlow.screensData.MainButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.PrimaryButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.RecordingScreenData
import co.onestep.kmp.uikit.features.recordFlow.screensData.SecondaryButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.SelectionItemData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TimerData
import co.onestep.kmp.uikit.features.recordFlow.screensData.ToolBarData
import co.onestep.kmp.uikit.features.recordFlow.screensData.UiKitScreenData
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.ic_back_arrow
import co.onestep.kmp.uikit_kmp.generated.resources.ic_close
import co.onestep.kmp.uikit_kmp.generated.resources.ic_info_black
import co.onestep.kmp.uikit_kmp.generated.resources.ic_info_circle
import co.onestep.kmp.uikit_kmp.generated.resources.ic_play_button
import co.onestep.kmp.uikit_kmp.generated.resources.ic_warning_red_stars
import co.onestep.kmp.uikit_kmp.generated.resources.image_in_thigh

// ── Toolbar mock data ────────────────────────────────────────────────────────

internal val toolbarData =
    ToolBarData(
        startIcon = IconData(Res.drawable.ic_back_arrow),
        title = TextData("Gait Lab Report", 16.sp, FontWeight.W400),
        endIcons = listOf(IconData(Res.drawable.ic_close)),
    )

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

// ── Hallway distance mock data ───────────────────────────────────────────────

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

internal val previewMainButtonData =
    MainButtonData(TextData("Start", 40.sp, FontWeight.Bold), buttonSize = 250.dp) {}

// ── Selection item mock data ─────────────────────────────────────────────────

internal val previewSelectionItemThigh =
    SelectionItemData(
        text = TextData("Against the thigh", 22.sp, FontWeight.W600),
        icon = IconData(Res.drawable.image_in_thigh),
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
