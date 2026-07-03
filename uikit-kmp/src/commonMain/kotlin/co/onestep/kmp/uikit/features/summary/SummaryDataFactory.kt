package co.onestep.kmp.uikit.features.summary

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.recordFlow.screensData.AnalysisBannerData
import co.onestep.kmp.uikit.features.recordFlow.screensData.InfoBottomSheetData
import co.onestep.kmp.uikit.features.recordFlow.screensData.SecondaryButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.SelectionItemData
import co.onestep.kmp.uikit.features.recordFlow.screensData.SelectionListData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData
import co.onestep.kmp.uikit.features.recordFlow.screensData.UiKitScreenData
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.minimal_analysis_banner_subtitle
import co.onestep.kmp.uikit_kmp.generated.resources.minimal_analysis_banner_title
import co.onestep.kmp.uikit_kmp.generated.resources.minimal_analysis_sheet_body
import co.onestep.kmp.uikit_kmp.generated.resources.minimal_analysis_sheet_title
import co.onestep.kmp.uikit_kmp.generated.resources.no_score_server_error_text
import co.onestep.kmp.uikit_kmp.generated.resources.no_score_system_error_text
import co.onestep.kmp.uikit_kmp.generated.resources.partial_analysis_banner_subtitle
import co.onestep.kmp.uikit_kmp.generated.resources.partial_analysis_banner_title
import co.onestep.kmp.uikit_kmp.generated.resources.partial_analysis_learn_more
import co.onestep.kmp.uikit_kmp.generated.resources.partial_analysis_sheet_body
import co.onestep.kmp.uikit_kmp.generated.resources.partial_analysis_sheet_title
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingQuestionData
import co.onestep.kmp.uikit.features.recordFlow.screensData.IconData
import co.onestep.kmp.uikit.features.tagging.models.Footwear
import co.onestep.kmp.uikit.models.OSTAssistiveDevice
import co.onestep.kmp.uikit.models.OSTLevelOfAssistance
import co.onestep.kmp.uikit.models.displayName
import co.onestep.kmp.uikit.models.icon
import co.onestep.kmp.uikit_kmp.generated.resources.what_assistive_device_did_you_use
import co.onestep.kmp.uikit_kmp.generated.resources.what_level_of_assistance_was_required
import co.onestep.kmp.uikit_kmp.generated.resources.which_type_of_footwear_was_worn_during_the_test
import org.jetbrains.compose.resources.stringResource

/**
 * Slim factory for building summary screen data.
 * Extracted from the original DataFactory (97KB) to only include summary-related methods.
 */
internal object SummaryDataFactory {

    @Composable
    fun partialAnalysisBannerData(
        onLearnMore: (() -> Unit)? = null,
    ) = AnalysisBannerData(
        title = TextData(
            stringResource(Res.string.partial_analysis_banner_title),
            14.sp,
            FontWeight.Bold,
        ),
        subtitle = TextData(
            stringResource(Res.string.partial_analysis_banner_subtitle),
            14.sp,
            FontWeight.Normal,
        ),
        infoBottomSheetData = partialAnalysisInfoSheetData(),
        button = SecondaryButtonData(
            text = TextData(
                stringResource(Res.string.partial_analysis_learn_more),
                14.sp,
                FontWeight.SemiBold,
            ),
            action = { onLearnMore?.invoke() },
        ),
    )

    @Composable
    fun minimalAnalysisBannerData(
        onLearnMore: (() -> Unit)? = null,
    ) = AnalysisBannerData(
        title = TextData(
            stringResource(Res.string.minimal_analysis_banner_title),
            14.sp,
            FontWeight.Bold,
        ),
        subtitle = TextData(
            stringResource(Res.string.minimal_analysis_banner_subtitle),
            14.sp,
            FontWeight.Normal,
        ),
        infoBottomSheetData = minimalAnalysisInfoSheetData(),
        button = SecondaryButtonData(
            text = TextData(
                stringResource(Res.string.partial_analysis_learn_more),
                14.sp,
                FontWeight.SemiBold,
            ),
            action = { onLearnMore?.invoke() },
        ),
    )

    @Composable
    fun noScoreSystemErrorBannerData() = AnalysisBannerData(
        subtitle = TextData(
            stringResource(Res.string.no_score_system_error_text),
            14.sp,
            FontWeight.Normal,
        ),
    )

    @Composable
    fun noScoreConnectionErrorBannerData() = AnalysisBannerData(
        subtitle = TextData(
            stringResource(Res.string.no_score_server_error_text),
            14.sp,
            FontWeight.Normal,
        ),
    )

    @Composable
    private fun partialAnalysisInfoSheetData() = InfoBottomSheetData(
        title = TextData(
            stringResource(Res.string.partial_analysis_sheet_title),
            24.sp,
            FontWeight.Bold,
        ),
        body = TextData(
            stringResource(Res.string.partial_analysis_sheet_body),
            18.sp,
            FontWeight.Normal,
        ),
    )

    @Composable
    private fun minimalAnalysisInfoSheetData() = InfoBottomSheetData(
        title = TextData(
            stringResource(Res.string.minimal_analysis_sheet_title),
            24.sp,
            FontWeight.Bold,
        ),
        body = TextData(
            stringResource(Res.string.minimal_analysis_sheet_body),
            18.sp,
            FontWeight.Normal,
        ),
    )

    @Composable
    fun customQuestionScreenData(
        onItemSelected: (List<String>) -> Unit,
        question: OSTRecordingQuestionData,
    ) = UiKitScreenData(
        title = TextData(
            text = question.title,
            textSize = 28.sp,
            fontWeight = FontWeight.W700,
        ),
        selectionList = SelectionListData(
            items = question.tagsValues.map { tag ->
                SelectionItemData(
                    text = TextData(
                        text = tag,
                        textSize = 20.sp,
                        fontWeight = FontWeight.W400,
                    ),
                )
            },
            onItemSelected = { selectedIndexes ->
                val selectedTags = question.tagsValues.filterIndexed { index, _ ->
                    selectedIndexes.contains(index)
                }
                onItemSelected(selectedTags)
            },
        ),
    )

    @Composable
    fun selectAssistiveDevice(
        onSelection: (OSTAssistiveDevice) -> Unit,
    ) = UiKitScreenData(
        title = TextData(
            text = stringResource(Res.string.what_assistive_device_did_you_use),
            textSize = 28.sp,
            fontWeight = FontWeight.W700,
        ),
        selectionList = SelectionListData(
            items = OSTAssistiveDevice.entries.map { device ->
                SelectionItemData(
                    text = TextData(
                        text = device.displayName(),
                        textSize = 20.sp,
                        fontWeight = FontWeight.W400,
                    ),
                    icon = IconData(icon = device.icon),
                )
            },
            onItemSelected = { selectedIndexes ->
                selectedIndexes.firstOrNull()?.let { index ->
                    onSelection(OSTAssistiveDevice.entries[index])
                }
            },
        ),
    )

    @Composable
    fun selectLevelOfAssistance(
        onSelection: (OSTLevelOfAssistance) -> Unit,
    ) = UiKitScreenData(
        title = TextData(
            text = stringResource(Res.string.what_level_of_assistance_was_required),
            textSize = 28.sp,
            fontWeight = FontWeight.W700,
        ),
        selectionList = SelectionListData(
            items = OSTLevelOfAssistance.entries.map { level ->
                SelectionItemData(
                    text = TextData(
                        text = level.displayName(),
                        textSize = 20.sp,
                        fontWeight = FontWeight.W400,
                    ),
                )
            },
            onItemSelected = { selectedIndexes ->
                selectedIndexes.firstOrNull()?.let { index ->
                    onSelection(OSTLevelOfAssistance.entries[index])
                }
            },
        ),
    )

    @Composable
    fun selectFootwear(
        onSelection: (Footwear) -> Unit,
    ) = UiKitScreenData(
        title = TextData(
            text = stringResource(Res.string.which_type_of_footwear_was_worn_during_the_test),
            textSize = 28.sp,
            fontWeight = FontWeight.W700,
        ),
        selectionList = SelectionListData(
            items = Footwear.entries.map { fw ->
                SelectionItemData(
                    text = TextData(
                        text = fw.displayName(),
                        textSize = 20.sp,
                        fontWeight = FontWeight.W400,
                    ),
                    icon = IconData(icon = fw.icon),
                )
            },
            onItemSelected = { selectedIndexes ->
                selectedIndexes.firstOrNull()?.let { index ->
                    onSelection(Footwear.entries[index])
                }
            },
        ),
    )
}
