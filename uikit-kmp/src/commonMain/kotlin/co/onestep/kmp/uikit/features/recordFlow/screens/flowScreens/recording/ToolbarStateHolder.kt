package co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.recording

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.recordFlow.screensData.IconData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData
import co.onestep.kmp.uikit.features.recordFlow.screensData.ToolBarData
import co.onestep.kmp.uikit.utils.ResourceProvider
import org.jetbrains.compose.resources.StringResource

/**
 * Owns the record-flow toolbar state (visibility, [ToolBarData], and the remembered start icon)
 * and the setters that mutate it. Extracted from [MotionRecorderViewModel] so the toolbar
 * concern is isolated from the recording lifecycle. The ViewModel exposes the same public
 * surface by delegating to this holder.
 */
internal class ToolbarStateHolder(
    private val resourceProvider: ResourceProvider,
) {
    val showToolbar: MutableState<Boolean> = mutableStateOf(true)
    val toolbarData: MutableState<ToolBarData> = mutableStateOf(ToolBarData())
    private val startIcon: MutableState<IconData?> = mutableStateOf(null)

    fun setToolBarData(data: ToolBarData) {
        toolbarData.value = data
        startIcon.value = data.startIcon
    }

    fun showToolbar(show: Boolean) {
        showToolbar.value = show
    }

    fun showBackButton(show: Boolean) {
        toolbarData.value =
            toolbarData.value.copy(
                startIcon = if (show) startIcon.value else null,
            )
    }

    fun setToolBarTitle(title: StringResource?) {
        toolbarData.value =
            toolbarData.value.copy(
                title =
                    title?.let {
                        TextData(
                            text = resourceProvider.getString(title),
                            textSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                        )
                    },
            )
    }
}
