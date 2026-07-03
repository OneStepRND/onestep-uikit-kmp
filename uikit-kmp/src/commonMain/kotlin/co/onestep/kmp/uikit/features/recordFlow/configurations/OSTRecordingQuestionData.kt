package co.onestep.kmp.uikit.features.recordFlow.configurations

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class OSTRecordingQuestionData(
    val title: String,
    val description: String? = null,
    val tagsValues: List<String>,
    val isMultiSelect: Boolean = false,
) {
    @Transient
    var selectedAnswers: List<String>? = null
}

fun List<OSTRecordingQuestionData>.removeSelectedAnswers(tagsToRemove: List<String>) {
    this.forEach { question ->
        question.selectedAnswers =
            question.selectedAnswers
                ?.filterNot { it in tagsToRemove }
    }
}

fun List<OSTRecordingQuestionData>.addSelectedAnswers(tagsToAdd: List<String>) {
    this.forEach { question ->
        if (tagsToAdd.all { it in question.tagsValues }) {
            question.selectedAnswers = tagsToAdd
        }
    }
}
