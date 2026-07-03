package co.onestep.kmp.uikit.features.summary.models

import androidx.compose.ui.graphics.Color
import co.onestep.kmp.uikit.features.recordFlow.screensData.AnalysisBannerData
import co.onestep.kmp.uikit.features.summary.components.OSTTabData
import co.onestep.kmp.uikit.models.OSTActivityType

internal data class MainParamItem(
    val title: String,
    val steps: Int,
    val duration: String,
    val durationUnits: String,
    val animateMainParam: Boolean,
    val showTabs: Boolean,
    val tabs: List<OSTTabData>,
    val showMetadata: Boolean,
    val showValues: Boolean = true,
    val showTrashIcon: Boolean,
    val mainParamValue: Float?,
    val mainParamText: String? = null,
    val mainParamColor: Color,
    val analysisBannerData: AnalysisBannerData? = null,
    val activityType: OSTActivityType? = null,
    /** Server flag: the main-param value was manually self-reported (drives the pill). */
    val selfReport: Boolean? = null,
    /**
     * Whether the user may manually edit/report this measurement's main parameter (STS only).
     * Drives the pen icon on the summary toolbar. Gated by the SDK feature flag
     * [co.onestep.kmp.uikit.models.FeatureFlag.STS_MANUAL_REPORT].
     */
    val editable: Boolean = false,
)
