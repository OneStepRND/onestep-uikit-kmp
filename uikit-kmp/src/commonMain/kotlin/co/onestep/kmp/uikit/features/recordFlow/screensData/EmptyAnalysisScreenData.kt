package co.onestep.kmp.uikit.features.recordFlow.screensData

internal data class EmptyAnalysisScreenData(
    val timeStampMillis: Long,
    val title: String,
    val icon: IconData? = null,
    val subtitle: String,
    val steps: Int? = null,
    val brandButtonData: PrimaryButtonData,
)
