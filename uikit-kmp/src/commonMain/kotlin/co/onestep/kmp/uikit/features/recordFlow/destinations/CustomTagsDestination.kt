package co.onestep.kmp.uikit.features.recordFlow.destinations

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingQuestionData
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.RecordingQuestionScreen
import co.onestep.kmp.uikit.utils.UIktDestination
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.Serializable

@Serializable
data object CustomTagsDestination : UIktDestination

fun EntryProviderScope<NavKey>.customTagsScreen(
    topBarPadding: Int = 0,
    preRecordingQuestions: List<OSTRecordingQuestionData>?,
    onAddTags: (List<String>) -> Unit,
    onRemoveTags: (List<String>) -> Unit,
    onToolbarBackRequest: SharedFlow<Unit>? = null,
    currentIndex: MutableState<Int>? = null,
    onBack: () -> Unit,
    onDone: (Int) -> Unit,
) {
    entry<CustomTagsDestination> {
        LaunchedEffect(currentIndex) {
            preRecordingQuestions?.get(currentIndex?.value ?: 0)?.selectedAnswers?.let {
                onRemoveTags(it)
            }
        }
        val currentScreenIndex = remember { mutableIntStateOf(currentIndex?.value ?: 0) }
        LaunchedEffect(onToolbarBackRequest) {
            onToolbarBackRequest?.collect {
                if (currentScreenIndex.intValue > 0) {
                    currentScreenIndex.intValue--
                } else {
                    onBack()
                }
                preRecordingQuestions?.get(currentScreenIndex.intValue)?.selectedAnswers?.let { answers ->
                    onRemoveTags(answers)
                }
            }
        }

        preRecordingQuestions?.let {
            RecordingQuestionScreen(
                modifier = Modifier.padding(top = topBarPadding.dp),
                preRecordingQuestionData = preRecordingQuestions,
                setTags = onAddTags,
                currentScreenIndex = currentScreenIndex.intValue,
                onScreenIndexChanged = {
                    if (it < currentScreenIndex.intValue) {
                        preRecordingQuestions[currentScreenIndex.intValue].selectedAnswers?.let { tagsToRemove ->
                            onRemoveTags(tagsToRemove)
                        }
                    }
                    currentScreenIndex.intValue = it
                },
                onBack = onBack,
                onDone = {
                    onDone(currentScreenIndex.intValue)
                },
            )
        }
    }
}
