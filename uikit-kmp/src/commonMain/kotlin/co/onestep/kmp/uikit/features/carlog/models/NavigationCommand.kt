package co.onestep.kmp.uikit.features.carlog.models

import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingConfiguration

internal sealed class NavigationCommand {
    object ToPermissionFlow : NavigationCommand()

    data class ToRecordingFlow(
        val recordingConfiguration: OSTRecordingConfiguration? = null,
    ) : NavigationCommand()

    data class ToSummary(
        val motionMeasurementId: String,
    ) : NavigationCommand()
}
