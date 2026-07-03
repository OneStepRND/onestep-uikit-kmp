package co.onestep.kmp.uikit.features.recordFlow.screens.instructions

import kotlinx.serialization.Serializable

@Serializable
data class OSTRecordingInstruction(
    val startTimeMillis: Long, // Timestamp in milliseconds when the sentence should be spoken
    val text: String, // The text to be played using TextToSpeech
    val marker: String? = null, // Optional marker to be added to the recording data
)
