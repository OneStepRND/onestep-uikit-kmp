package co.onestep.kmp.uikit.features.summary.models

import co.onestep.kmp.uikit.features.recordFlow.screensData.IconData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData

data class EmptyStateData(
    val icon: IconData? = null,
    val title: TextData? = null,
    val subtitle: TextData,
)
