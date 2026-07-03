package co.onestep.kmp.uikit.features.recordFlow.configurations

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface OSTPostTaggingData {
    val questions: List<OSTRecordingQuestionData>?

    @Serializable
    @SerialName("screen")
    data class OSTPostTaggingScreen(
        override val questions: List<OSTRecordingQuestionData>?,
        val assistiveDeviceTag: Boolean? = true,
        val levelOfAssistanceTag: Boolean? = false,
        val footwearTag: Boolean? = true,
        val note: Boolean? = true,
    ) : OSTPostTaggingData

    @Serializable
    @SerialName("questions_flow")
    data class OSTPostTaggingQuestionsFlow(
        override val questions: List<OSTRecordingQuestionData>,
    ) : OSTPostTaggingData

    @Serializable
    @SerialName("none")
    data class None(
        override val questions: List<OSTRecordingQuestionData>,
    ) : OSTPostTaggingData

    companion object {
        fun default() = OSTPostTaggingScreen(questions = null)
    }
}
