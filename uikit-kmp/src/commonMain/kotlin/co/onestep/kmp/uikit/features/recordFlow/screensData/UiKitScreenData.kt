package co.onestep.kmp.uikit.features.recordFlow.screensData

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.ui.components.InstructionContent
import org.jetbrains.compose.resources.DrawableResource

data class RecordingScreenData(
    val recordScreenStage: RecordScreenStage,
    val title: TextData,
    val instructions: TextData,
    val timerValue: TimerData? = null,
    val value: TextData? = null,
    val slideToStopButton: SlideToStopButtonData? = null,
    val bottomButton: SecondaryButtonData? = null,
) {
    enum class RecordScreenStage {
        GET_READY,
        RECORDING,
        ANALYZING,
        ;

        fun isReady(): Boolean = this == GET_READY

        fun isRecording(): Boolean = this == RECORDING

        fun isAnalyzing(): Boolean = this == ANALYZING
    }

    val colorTheme: Color =
        when (recordScreenStage) {
            RecordScreenStage.GET_READY -> Color(0xFFF5960B)
            RecordScreenStage.RECORDING -> Color(0xFF3E3D3B)
            RecordScreenStage.ANALYZING -> Color(0xFF0D5097)
        }
}

data class NoteBannerData(
    val icon: DrawableResource,
    val title: String,
    val body: String,
)

data class UiKitScreenData(
    val noteBanner: NoteBannerData? = null,
    val mainIcon: IconData? = null,
    val title: TextData? = null,
    val subtitle: TextData? = null,
    val mainButton: MainButtonData? = null,
    val selectionList: SelectionListData? = null,
    val brandButton: PrimaryButtonData? = null,
    val outlineBrandButton: SecondaryButtonData? = null,
    val slideToStopButton: SlideToStopButtonData? = null,
    val playAudioKey: String? = null,
)

data class TimerData(
    val text: TextData,
    val countdown: Boolean,
)

data class SelectionListData(
    val items: List<SelectionItemData>,
    val isMultiSelect: Boolean = false,
    val onItemSelected: (List<Int>) -> Unit,
)

data class SelectionItemData(
    val text: TextData,
    val description: TextData? = null,
    val itemHeight: Dp? = null,
    val icon: IconData? = null,
)

data class PrimaryButtonData(
    val text: TextData,
    val enabled: Boolean = true,
    val action: () -> Unit,
)

data class SecondaryButtonData(
    val iconData: IconData? = null,
    val text: TextData,
    val action: () -> Unit,
)

data class TertiaryButtonData(
    val text: TextData,
    val action: () -> Unit,
)

data class SlideToStopButtonData(
    val iconData: IconData? = null,
    val textData: TextData? = TextData("Slide to stop", 18.sp, FontWeight.Normal),
    val action: () -> Unit,
)

data class MainButtonData(
    val text: TextData,
    val buttonSize: Dp = 260.dp,
    val topSpace: Dp = 56.dp,
    val action: () -> Unit,
)

data class ToolBarData(
    val startIcon: IconData? = null,
    val title: TextData? = null,
    val endIcons: List<IconData>? = null,
)

data class IconData(
    val icon: DrawableResource,
    val tintColor: Color? = null,
    val iconSize: Dp? = null,
    val action: (() -> Unit)? = null,
)

data class TextData(
    val text: String,
    val textSize: TextUnit,
    val fontWeight: FontWeight,
    val textAlign: TextAlign? = null,
    val color: Color? = null,
)

data class HallwayDistanceScreenState(
    val title: String,
    val subtitle: String? = null,
    val unitText: String,
    val inputValue: String,
    val errorText: String?,
    val canContinue: Boolean,
    val showShortHallwayDialog: Boolean,
    val recommendedValue: Int,
    val suppressShortHallwayWarning: Boolean,
    val fromSummary: Boolean = false,
)

internal data class InfoBottomSheetData(
    val title: TextData,
    val body: TextData,
)

internal data class AnalysisBannerData(
    val title: TextData? = null,
    val subtitle: TextData,
    val infoBottomSheetData: InfoBottomSheetData? = null,
    val button: SecondaryButtonData? = null,
)

data class InstructionData(
    val content: InstructionContent,
    val style: TextStyle,
)
