package co.onestep.kmp.uikit.features.summary.collapsibleUtils

import androidx.compose.runtime.Stable

@Stable
internal interface ToolbarState {
    val offset: Float
    val height: Float
    val progress: Float
    val consumed: Float
    var scrollTopLimitReached: Boolean
    var scrollOffset: Float
}
