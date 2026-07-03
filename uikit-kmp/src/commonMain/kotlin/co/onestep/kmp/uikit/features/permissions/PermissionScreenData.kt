package co.onestep.kmp.uikit.features.permissions

import androidx.compose.runtime.Composable
import co.onestep.kmp.uikit.features.recordFlow.screensData.PrimaryButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.IconData
import co.onestep.kmp.uikit.features.recordFlow.screensData.SecondaryButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.SelectionListData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TertiaryButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData
import co.onestep.kmp.uikit.features.recordFlow.screensData.ToolBarData
import co.onestep.kmp.uikit.ui.components.InstructionContent

internal data class PermissionScreenData(
    val toolBarData: ToolBarData? = null,
    val mainIcon: IconData? = null,
    val title: TextData? = null,
    val content: InstructionContent? = null,
    val customContent: (@Composable () -> Unit)? = null,
    val selectionList: SelectionListData? = null,
    val outlineBrandButton: SecondaryButtonData? = null,
    val tertiaryButton: TertiaryButtonData? = null,
    val brandButton: PrimaryButtonData? = null,
)
