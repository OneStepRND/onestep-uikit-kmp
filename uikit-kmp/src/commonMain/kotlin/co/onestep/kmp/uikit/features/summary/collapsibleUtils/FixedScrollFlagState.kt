package co.onestep.kmp.uikit.features.summary.collapsibleUtils

internal abstract class FixedScrollFlagState(
    heightRange: IntRange,
) : ScrollFlagState(heightRange) {
    final override val offset: Float = 0f
}
