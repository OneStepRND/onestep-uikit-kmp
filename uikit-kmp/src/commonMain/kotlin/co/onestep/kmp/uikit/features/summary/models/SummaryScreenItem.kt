package co.onestep.kmp.uikit.features.summary.models

import co.onestep.kmp.uikit.features.recordFlow.screensData.IconData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData
import co.onestep.kmp.uikit.models.OSTNorm
import co.onestep.kmp.uikit.models.OSTParameterMetadata

internal interface SummaryScreenItem {
    sealed interface SimpleInsightItem : SummaryScreenItem {
        val text: TextData
        val icon: IconData
    }

    data class TugSummaryComponentItem(
        val tugComponentData: TugComponentData,
    ) : SummaryScreenItem

    data class TrendItem(
        override val text: TextData,
        override val icon: IconData,
    ) : SimpleInsightItem

    data class EducationalItem(
        override val text: TextData,
        override val icon: IconData,
        val action: () -> Unit = {},
    ) : SimpleInsightItem

    data class FallRiskItem(
        override val text: TextData,
        override val icon: IconData,
        val action: () -> Unit = {},
    ) : SimpleInsightItem

    data class InfoItem(
        override val text: TextData,
        override val icon: IconData,
        val action: () -> Unit = {},
    ) : SimpleInsightItem

    data class GaitLabItem(
        val value: Float,
        val previousValue: Float? = null,
        val previousLocalizedTime: String? = null,
        val norm: OSTNorm,
        val metaData: OSTParameterMetadata?,
        val units: String,
    ) : SummaryScreenItem

    data class GaitLabCategoryItem(
        val text: TextData,
    ) : SummaryScreenItem

    data class ParameterItem(
        override val text: TextData,
        override val icon: IconData,
        val value: Float,
        val norm: OSTNorm?,
        val metaData: OSTParameterMetadata?,
        val previousValue: Float? = null,
        val previousLocalizedTime: String? = null,
        val displayName: String,
        val units: String,
        val action: () -> Unit = {},
    ) : SimpleInsightItem
}
