package co.onestep.kmp.uikit.features.summary.screens.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
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

internal fun NavGraphBuilder.taggingScreen(
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
    composable<TaggingScreenDestination> {
        PostSummaryTagScreen(
            modifier = Modifier.padding(top = ToolBarHeight.dp),
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
