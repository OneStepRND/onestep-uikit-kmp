package co.onestep.kmp.uikit.features.summary.models

data class TugComponentData(
    val forward: Float,
    val turning: Float,
    val backward: Float,
    val turningToChair: Float,
    val tugChairData: TugChairData,
    val distance: Float,
    val duration: Float,
)

data class TugChairData(
    val standing: Float,
    val sitting: Float,
)
