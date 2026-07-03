package co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import co.onestep.kmp.uikit.features.recordFlow.RecordFlowDataFactory
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingQuestionData
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun RecordingQuestionScreen(
    modifier: Modifier = Modifier,
    preRecordingQuestionData: List<OSTRecordingQuestionData>,
    currentScreenIndex: Int,
    setTags: (List<String>) -> Unit,
    onScreenIndexChanged: (Int) -> Unit,
    onBack: () -> Unit,
    onDone: () -> Unit,
) {
    val canNavigateForward = currentScreenIndex < preRecordingQuestionData.size - 1
    AnimatedContent(
        targetState = currentScreenIndex,
        transitionSpec = {
            if (currentScreenIndex > initialState) {
                (slideInHorizontally { width -> width }).togetherWith(
                    slideOutHorizontally { width -> -width },
                )
            } else {
                (slideInHorizontally { width -> -width }).togetherWith(
                    slideOutHorizontally { width -> width },
                )
            }
        },
        label = "Custom tags navigation animation",
    ) { targetScreenIndex ->
        val screenData = RecordFlowDataFactory.customTagsScreenData(
            tagsData = preRecordingQuestionData[targetScreenIndex],
            onSelection = { tags ->
                setTags(tags.selectedAnswers ?: emptyList())
                if (canNavigateForward) {
                    onScreenIndexChanged(currentScreenIndex.plus(1))
                } else {
                    onDone()
                }
            },
        )
        UiKitScreen(
            modifier = modifier,
            screenData = screenData,
            onBackPress = {
                if (currentScreenIndex > 0) {
                    onScreenIndexChanged(currentScreenIndex.minus(1))
                } else {
                    onBack()
                }
            },
        )
    }
}

@Preview
@Composable
private fun RecordingQuestionScreenPreview() {
    PreviewTheme {
        RecordingQuestionScreen(
            preRecordingQuestionData = listOf(
                OSTRecordingQuestionData(
                    title = "How are you feeling?",
                    tagsValues = listOf("Good", "Fair", "Poor"),
                    isMultiSelect = false,
                ),
            ),
            currentScreenIndex = 0,
            setTags = {},
            onScreenIndexChanged = {},
            onBack = {},
            onDone = {},
        )
    }
}
