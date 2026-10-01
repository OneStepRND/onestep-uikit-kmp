package co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.recording

import co.onestep.kmp.sdk.OSTEvent
import co.onestep.kmp.uikit.OSTUIKitAnalyticsHandler
import co.onestep.kmp.uikit.features.recordFlow.analytics.AnalyticsProps
import co.onestep.kmp.uikit.features.recordFlow.analytics.RecordFlowAnalyticsTracker
import co.onestep.kmp.uikit.models.OSTActivityType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * What a UI timeout reports on `measurement_still_analyzing` once the recorder has been reset
 * (OS-17521; Android uikit `51f4553`).
 */
class RecordingSnapshotTest {

    @Test
    fun nothingIsKnown_beforeTheFirstRecording() {
        val snapshot = RecordingSnapshot()
        snapshot.onStepCount(12)

        assertNull(snapshot.perceptionUuid)
        assertNull(snapshot.steps)
        assertNull(snapshot.measurementSeconds)
    }

    @Test
    fun uuidStepsAndLength_surviveTheRecorderZeroingItsCounter() {
        val snapshot = RecordingSnapshot()
        snapshot.onRecordingStarted("p-uuid-1")
        snapshot.onStepCount(0)
        snapshot.onStepCount(18)
        snapshot.onStepCount(42)
        snapshot.onRecordingStopped(measurementSeconds = 31)
        // analyse() resets the recorder, then the UI timeout resets it again: the counter reads 0.
        snapshot.onStepCount(0)

        assertEquals("p-uuid-1", snapshot.perceptionUuid)
        assertEquals(42, snapshot.steps)
        assertEquals(31, snapshot.measurementSeconds)
    }

    @Test
    fun aRecordingWithNoSteps_reportsZero() {
        val snapshot = RecordingSnapshot()
        snapshot.onRecordingStarted("p-uuid-1")

        assertEquals(0, snapshot.steps)
    }

    @Test
    fun aNewRecording_replacesThePreviousOne() {
        val snapshot = RecordingSnapshot()
        snapshot.onRecordingStarted("p-uuid-1")
        snapshot.onStepCount(42)
        snapshot.onRecordingStopped(measurementSeconds = 60)

        snapshot.onRecordingStarted("p-uuid-2")
        snapshot.onStepCount(7)

        assertEquals("p-uuid-2", snapshot.perceptionUuid)
        assertEquals(7, snapshot.steps)
        assertNull(snapshot.measurementSeconds)
    }

    @Test
    fun clear_forgetsTheLastAttempt_soAStaleCountCannotLeakIn() {
        val snapshot = RecordingSnapshot()
        snapshot.onRecordingStarted("p-uuid-1")
        snapshot.onStepCount(42)
        snapshot.onRecordingStopped(measurementSeconds = 60)

        snapshot.clear()
        snapshot.onStepCount(42)

        assertNull(snapshot.perceptionUuid)
        assertNull(snapshot.steps)
        assertNull(snapshot.measurementSeconds)
    }

    @Test
    fun stillAnalyzing_reportsTheSnapshot_withTheIosUuidLowercased() {
        val events = mutableListOf<OSTEvent>()
        val tracker = RecordFlowAnalyticsTracker(
            object : OSTUIKitAnalyticsHandler {
                override fun onEvent(event: OSTEvent) {
                    events += event
                }
            },
        )
        val snapshot = RecordingSnapshot()
        // iOS publishes the native UUID's uuidString, which is uppercase.
        snapshot.onRecordingStarted("8F14E45F-CEEA-467A-9A36-DEDD4BEA2543")
        snapshot.onStepCount(56)
        snapshot.onRecordingStopped(measurementSeconds = 30)
        snapshot.onStepCount(0)

        tracker.trackStillAnalyzingScreen(
            activity = OSTActivityType.WALK,
            steps = snapshot.steps,
            seconds = snapshot.measurementSeconds,
            perceptionUuid = snapshot.perceptionUuid,
        )

        val props = events.single().properties
        assertEquals("8f14e45f-ceea-467a-9a36-dedd4bea2543", props[AnalyticsProps.PERCEPTION_UUID])
        assertEquals("56", props[AnalyticsProps.STEPS])
        assertEquals("30", props[AnalyticsProps.MEASUREMENT_SECONDS])
    }
}
