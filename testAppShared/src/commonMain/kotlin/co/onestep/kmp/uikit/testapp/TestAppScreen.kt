package co.onestep.kmp.uikit.testapp

import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingConfiguration
import co.onestep.kmp.uikit.models.OSTMotionMeasurement

sealed class TestAppScreen {
    data object Home : TestAppScreen()
    data object Settings : TestAppScreen()
    data object CareLog : TestAppScreen()
    data object ConfigureFlow : TestAppScreen()
    data object MeasurementPicker : TestAppScreen()
    /**
     * @param returnToCareLog where to land after the recording result: CareLog when launched from
     *   the Care Log, else Home (so the Home "Last Event" label reflects the outcome).
     */
    data class Recording(
        val config: OSTRecordingConfiguration,
        val returnToCareLog: Boolean = false,
    ) : TestAppScreen()
    data class SummaryLoading(val measurementId: String) : TestAppScreen()
    data class Summary(val measurement: OSTMotionMeasurement) : TestAppScreen()

    /**
     * uikit-hosted web summary, reached when a `WEB` flow finishes with a `summaryUrl`. This is what
     * exercises the real authenticated load: the cookie bridge is only observable against a live
     * patient-scoped URL, so a mock cannot stand in for it.
     *
     * @param returnToCareLog where to land on close, mirroring [Recording].
     */
    data class WebSummary(
        val url: String,
        val returnToCareLog: Boolean = false,
    ) : TestAppScreen()
    data object PermissionInApp : TestAppScreen()
    data object PermissionBackground : TestAppScreen()
    data object PushPopDemo : TestAppScreen()
}
