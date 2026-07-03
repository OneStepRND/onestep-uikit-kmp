package co.onestep.kmp.uikit.features.summary.screens.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingQuestionData
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.UiKitScreen
import co.onestep.kmp.uikit.features.summary.SummaryDataFactory
import co.onestep.kmp.uikit.utils.UIktDestination
import kotlinx.serialization.Serializable

@Serializable
internal data object CustomQuestionDestination : UIktDestination

internal fun NavGraphBuilder.customQuestionDestination(
    questionProvider: () -> OSTRecordingQuestionData?,
    onItemSelected: (List<String>) -> Unit,
    onBackPress: () -> Unit,
) {
    composable<CustomQuestionDestination> {
        val question = questionProvider() ?: return@composable
        UiKitScreen(
            modifier = Modifier.padding(top = 40.dp),
            onBackPress = onBackPress,
            screenData = SummaryDataFactory.customQuestionScreenData(
                onItemSelected = onItemSelected,
                question = question,
            ),
        )
    }
}
