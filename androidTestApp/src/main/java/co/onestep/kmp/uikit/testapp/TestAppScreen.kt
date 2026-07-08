package co.onestep.kmp.uikit.testapp

import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingConfiguration
import co.onestep.kmp.uikit.models.OSTMotionMeasurement

sealed class TestAppScreen {
    data object Home : TestAppScreen()
    data object Settings : TestAppScreen()
    data object CareLog : TestAppScreen()
    data object ConfigureFlow : TestAppScreen()
    data object MeasurementPicker : TestAppScreen()
    data class Recording(val config: OSTRecordingConfiguration) : TestAppScreen()
    data class SummaryLoading(val measurementId: String) : TestAppScreen()
    data class Summary(val measurement: OSTMotionMeasurement) : TestAppScreen()
    data object PermissionInApp : TestAppScreen()
    data object PermissionBackground : TestAppScreen()
    data object PushPopDemo : TestAppScreen()
}
