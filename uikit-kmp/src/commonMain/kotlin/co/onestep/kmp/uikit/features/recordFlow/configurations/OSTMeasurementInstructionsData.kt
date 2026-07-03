package co.onestep.kmp.uikit.features.recordFlow.configurations

import kotlinx.serialization.Serializable

/**
 * Data class for the measurement instructions.
 * @param activityDisplayName The display name of the activity.
 * @param instructions The instructions to show to the user.
 * @param hints The hints to show to the user.
 * @param gifUrl The URL of the GIF to show to the user.
 * @param gifResourceKey The resource key for the GIF (platform-resolved).
 * @param videoUrl The URL of the video to show to the user.
 * @param videoResourceKey The resource key for the video (platform-resolved).
 * @param imageUrl The URL of the image to show to the user.
 * @param imageResourceKey The resource key for the image (platform-resolved).
 * @param recordingInstructions TTS instructions with timestamps.
 */
@Serializable
data class OSTMeasurementInstructionsData(
    val activityDisplayName: String,
    val instructions: List<String>,
    val hints: List<String> = emptyList(),
    val gifUrl: String? = null,
    val gifResourceKey: String? = null,
    val videoUrl: String? = null,
    val videoResourceKey: String? = null,
    val imageUrl: String? = null,
    val imageResourceKey: String? = null,
    var recordingInstructions: List<OSTRecordingInstruction>? = null,
)

@Serializable
data class OSTRecordingInstruction(
    val startTimeMillis: Long,
    val text: String,
    val marker: String? = null,
)
