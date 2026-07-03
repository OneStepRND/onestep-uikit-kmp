package co.onestep.kmp.uikit.features.recordFlow.configurations

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class OSTPrepareData {
    @Serializable
    @SerialName("duration")
    data class Duration(
        val prepareDuration: OSTPrepareDuration,
    ) : OSTPrepareData()

    @Serializable
    @SerialName("tts")
    data class Tts(
        val ttsSpeechText: String,
        val showInstructions: Boolean = false,
    ) : OSTPrepareData()

    companion object {
        fun default() = Duration(
            prepareDuration = OSTPrepareDuration.FIVE_SECONDS,
        )
    }
}

@Serializable
enum class OSTPrepareDuration(val seconds: Int) {
    NONE(0),
    FIVE_SECONDS(5),
    TEN_SECONDS(10),
}
