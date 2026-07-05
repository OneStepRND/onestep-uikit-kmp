package co.onestep.kmp.uikit.features.summary.screens.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import co.onestep.kmp.uikit.features.recordFlow.components.ToolBarHeight
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTPostTaggingData
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingQuestionData
import co.onestep.kmp.uikit.features.tagging.PostSummaryTagScreen
import co.onestep.kmp.uikit.features.tagging.models.Footwear
import co.onestep.kmp.uikit.models.OSTAssistiveDevice
import co.onestep.kmp.uikit.models.OSTLevelOfAssistance
import co.onestep.kmp.uikit.models.OSTUserInputMetaData
import co.onestep.kmp.uikit.utils.UIktDestination
import kotlinx.serialization.Serializable

@Serializable
internal data object TaggingScreenDestination : UIktDestination

internal fun EntryProviderScope<NavKey>.taggingScreen(
    getAssistiveDevice: () -> OSTAssistiveDevice?,
    getLevelOfAssistance: () -> OSTLevelOfAssistance?,
    getFootwear: () -> Footwear?,
    postTaggingData: OSTPostTaggingData.OSTPostTaggingScreen?,
    getNote: () -> String?,
    onEditAssistiveDeviceClicked: (String?) -> Unit,
    onEditLevelOfAssistanceClicked: (String?) -> Unit,
    onEditFootwearClicked: (String?) -> Unit,
    onGoToQuestionsClicked: (String?, OSTRecordingQuestionData) -> Unit,
    action: (OSTUserInputMetaData) -> Unit,
) {
    entry<TaggingScreenDestination> {
        PostSummaryTagScreen(
            // The Toolbar overlay occupies (statusBars inset + ToolBarHeight); reserve the same
            // so the title clears it. Reserving only ToolBarHeight let the toolbar cover the
            // title on iOS, where the top safe-area inset is large. Mirrors RecordFlowNavGraph.
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = ToolBarHeight.dp),
            postTaggingData = postTaggingData,
            assistiveDevice = getAssistiveDevice(),
            levelOfAssistance = getLevelOfAssistance(),
            footwear = getFootwear(),
            note = getNote(),
            onEditAssistiveDeviceClicked = onEditAssistiveDeviceClicked,
            onEditLevelOfAssistanceClicked = onEditLevelOfAssistanceClicked,
            onEditFootwearClicked = onEditFootwearClicked,
            onGoToQuestionsClicked = onGoToQuestionsClicked,
            action = action,
        )
    }
}
