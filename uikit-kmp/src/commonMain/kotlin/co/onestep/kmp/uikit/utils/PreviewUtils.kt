package co.onestep.kmp.uikit.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.recordFlow.screensData.AnalysisBannerData
import co.onestep.kmp.uikit.features.recordFlow.screensData.IconData
import co.onestep.kmp.uikit.features.recordFlow.screensData.InfoBottomSheetData
import co.onestep.kmp.uikit.features.recordFlow.screensData.SecondaryButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData
import co.onestep.kmp.uikit.features.summary.components.OSTTabData
import co.onestep.kmp.uikit.features.summary.models.MainParamItem
import co.onestep.kmp.uikit.features.summary.models.SummaryListState
import co.onestep.kmp.uikit.features.summary.models.SummaryScreenItem
import co.onestep.kmp.uikit.models.OSTActivityType
import co.onestep.kmp.uikit.models.OSTNorm
import co.onestep.kmp.uikit.models.OSTNormPart
import co.onestep.kmp.uikit.models.OSTParameterMetadata
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.ic_parameter_icon
import co.onestep.kmp.uikit_kmp.generated.resources.trend_up_green
import org.jetbrains.compose.ui.tooling.preview.Preview

internal object PreviewUtils {
    @Preview
    annotation class FontScalingPreviews

    @Preview
    annotation class DarkModePreviews

    @Preview
    annotation class LanguagePreviews
}

internal val trendItem =
    SummaryScreenItem.TrendItem(
        text = TextData(
            text = "Your step length this walk has improved",
            textSize = 20.sp,
            fontWeight = FontWeight.Normal,
        ),
        icon = IconData(
            icon = Res.drawable.trend_up_green,
        ),
    )

internal val educationalItem =
    SummaryScreenItem.EducationalItem(
        text = TextData(
            text = "Your walk was a bit slow",
            textSize = 20.sp,
            fontWeight = FontWeight.W400,
        ),
        icon = IconData(
            icon = Res.drawable.ic_parameter_icon,
        ),
    )

internal val infoItem =
    SummaryScreenItem.InfoItem(
        text = TextData(
            text = "Your walk was a bit slow",
            textSize = 20.sp,
            fontWeight = FontWeight.W400,
        ),
        icon = IconData(
            icon = Res.drawable.ic_parameter_icon,
        ),
    )

internal val mockNorm =
    OSTNorm(
        units = "steps/min",
        parts = listOf(
            OSTNormPart(
                start = 0f,
                end = 3f,
                color = "green",
                includeStart = true,
                includeEnd = true,
            ),
            OSTNormPart(
                start = 3f,
                end = 8f,
                color = "yellow",
                includeStart = false,
                includeEnd = false,
            ),
            OSTNormPart(
                start = 8f,
                end = 20f,
                color = "red",
                includeStart = false,
                includeEnd = false,
            ),
        ),
    )

internal val parameterItem =
    SummaryScreenItem.ParameterItem(
        text = TextData(
            text = "Your walk was a bit slow and the time you spend reading this is useless",
            textSize = 20.sp,
            fontWeight = FontWeight.W400,
        ),
        displayName = "Cadence",
        metaData = createMockParameterMetadata(),
        norm = mockNorm,
        value = 236.3f,
        icon = IconData(Res.drawable.ic_parameter_icon),
        units = mockNorm.units.orEmpty(),
    )

internal val summaryItems =
    listOf(
        trendItem,
        trendItem,
        parameterItem,
        educationalItem,
        infoItem,
    )

internal val gaitLabItem =
    SummaryScreenItem.GaitLabItem(
        metaData = createMockParameterMetadata(),
        norm = mockNorm,
        value = 70f,
        units = mockNorm.units.orEmpty(),
    )

internal val gaitLabItems =
    listOf(
        gaitLabItem,
        gaitLabItem,
    )

internal fun partialSuccessPreview() =
    SummaryListState.Partial.Success(
        title = "Partial title",
        subtitle = "Partial subtitle",
        steps = 53,
        durationText = "33s",
    )

internal val mainParamItem =
    MainParamItem(
        title = "Apr 12, 2023 | 2:12pm (GMT+3)",
        steps = 15,
        mainParamValue = 123.4f,
        duration = "00:30",
        durationUnits = "min",
        mainParamText = "long long word",
        animateMainParam = true,
        showTabs = true,
        tabs = listOf(
            OSTTabData("Highlights", 0),
            OSTTabData("Gait", 1),
        ),
        showMetadata = true,
        showValues = true,
        showTrashIcon = true,
        mainParamColor = Color.Red,
        analysisBannerData = minimalAnalysisBannerDataPreview(),
    )

internal val emptyMainParamItem =
    MainParamItem(
        title = "Apr 12, 2023 | 2:12pm (GMT+3)",
        steps = 0,
        mainParamValue = null,
        duration = "00:00",
        durationUnits = "min",
        animateMainParam = true,
        showTabs = true,
        tabs = listOf(
            OSTTabData("Gait", 1),
        ),
        showTrashIcon = true,
        showMetadata = true,
        showValues = true,
        mainParamColor = Color.Red,
    )

internal fun createMockParameterMetadata(
    displayName: String = "Default Parameter",
    units: String? = "steps/minute",
    category: String = "General",
    lowRange: Float? = 0.0f,
    highRange: Float? = 100.0f,
    roundDigits: Float? = 2.0f,
): OSTParameterMetadata =
    OSTParameterMetadata(
        activity = OSTActivityType.WALK,
        displayName = displayName,
        units = units,
        category = category,
        lowRange = lowRange,
        highRange = highRange,
        sortKey = 0f,
        roundDigits = roundDigits,
    )

internal fun partialAnalysisInfoSheetDataPreview() = InfoBottomSheetData(
    title = TextData("Partial Analysis", 24.sp, FontWeight.Bold),
    body = TextData(
        "Some metrics may be less accurate due to limited data captured during this walk.",
        18.sp,
        FontWeight.Normal,
    ),
)

internal fun minimalAnalysisInfoSheetDataPreview() = InfoBottomSheetData(
    title = TextData("Limited walk data captured", 24.sp, FontWeight.Bold),
    body = TextData(
        "Sometimes we're unable to capture enough gait data, especially during slow walks, short steps, or when the phone isn't positioned against your thigh.\n\n" +
            "To get full results, keep the phone snug in your pants pocket or hold it against the thigh, and walk at least 20 steps in a straight line.",
        18.sp,
        FontWeight.Normal,
    ),
)

internal fun partialAnalysisBannerDataPreview() = AnalysisBannerData(
    title = TextData("Partial Analysis", 14.sp, FontWeight.Bold),
    subtitle = TextData("Some metrics may be less accurate.", 14.sp, FontWeight.Normal),
    infoBottomSheetData = partialAnalysisInfoSheetDataPreview(),
    button = SecondaryButtonData(
        text = TextData("Learn more", 14.sp, FontWeight.SemiBold),
        action = {},
    ),
)

internal fun minimalAnalysisBannerDataPreview() = AnalysisBannerData(
    title = TextData("Limited walk data captured", 14.sp, FontWeight.Bold),
    subtitle = TextData(
        "We couldn't record enough data for a full analysis.",
        14.sp,
        FontWeight.Normal,
    ),
    infoBottomSheetData = minimalAnalysisInfoSheetDataPreview(),
    button = SecondaryButtonData(
        text = TextData("Learn more", 14.sp, FontWeight.SemiBold),
        action = {},
    ),
)

internal fun noScoreSystemErrorBannerDataPreview() = AnalysisBannerData(
    subtitle = TextData(
        "Score couldn't be generated due to a system issue.",
        14.sp,
        FontWeight.Normal,
    ),
)

internal fun noScoreConnectionErrorBannerDataPreview() = AnalysisBannerData(
    subtitle = TextData(
        "Score couldn't be generated due to a connection error, it will be available via your history tab soon.",
        14.sp,
        FontWeight.Normal,
    ),
)
