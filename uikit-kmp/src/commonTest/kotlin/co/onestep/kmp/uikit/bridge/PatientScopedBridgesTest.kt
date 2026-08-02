package co.onestep.kmp.uikit.bridge

import co.onestep.kmp.uikit.models.OSTActivityType
import co.onestep.kmp.uikit.models.OSTAnalyserState
import co.onestep.kmp.uikit.models.OSTDiscreteColor
import co.onestep.kmp.uikit.models.OSTInsights
import co.onestep.kmp.uikit.models.OSTMotionMeasurement
import co.onestep.kmp.uikit.models.OSTNorm
import co.onestep.kmp.uikit.models.OSTParamName
import co.onestep.kmp.uikit.models.OSTParameterMetadata
import co.onestep.kmp.uikit.models.OSTRecorderState
import co.onestep.kmp.uikit.models.OSTRecordingWindow
import co.onestep.kmp.uikit.models.OSTTimeRangedDataRequest
import co.onestep.kmp.uikit.models.OSTUserInputMetaData
import co.onestep.kmp.uikit.models.OSTWalkCourseLength
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Covers the clinician-mode bridge resolution contract (patient-scope design, Block 4). The
 * resolution logic is [resolveSessionBridges] — a pure function so these run on both platforms with
 * no Compose / service-locator / SDK dependency.
 */
class PatientScopedBridgesTest {

    // --- Distinct bundles per patientId (stale-session guard) -----------------------------------

    @Test
    fun factoryReturnsDistinctBundlesForDistinctPatients() {
        val factory = RecordingFakePatientScopedBridgesFactory()

        val a = resolveSessionBridges("patient-A", { currentUserBundle() }, factory)
        val b = resolveSessionBridges("patient-B", { currentUserBundle() }, factory)

        // Two sequential clinician launches for different patients must not share a bridge bundle —
        // otherwise patient A's recorder could leak into patient B's recording.
        assertNotSame(a, b)
        assertNotSame(a.recorderBridge, b.recorderBridge)
        assertTrue(factory.requestedPatientIds == listOf("patient-A", "patient-B"))
    }

    // --- Fail-fast: patientId supplied but no factory configured --------------------------------

    @Test
    fun clinicianModeWithoutFactoryThrowsInsteadOfFallingBackToSingleton() {
        var currentUserResolved = false

        val error = assertFailsWith<IllegalStateException> {
            resolveSessionBridges(
                patientId = "patient-A",
                currentUserBridges = { currentUserResolved = true; currentUserBundle() },
                patientScopedBridgesFactory = null,
            )
        }

        // Must NOT silently attribute the recording to the auth-bound singleton.
        assertTrue(!currentUserResolved)
        assertTrue(error.message?.contains("PatientScopedBridgesFactory") == true)
    }

    // --- Current-user mode uses the auth-bound bundle -------------------------------------------

    @Test
    fun currentUserModeUsesTheAuthBoundBundleAndNeverTheFactory() {
        val factory = RecordingFakePatientScopedBridgesFactory()
        val expected = currentUserBundle()

        val resolved = resolveSessionBridges(patientId = null, currentUserBridges = { expected }, patientScopedBridgesFactory = factory)

        assertSame(expected, resolved)
        // The factory is never consulted in current-user mode.
        assertTrue(factory.requestedPatientIds.isEmpty())
    }

    // --- helpers --------------------------------------------------------------------------------

    private fun currentUserBundle() = PatientScopedBridges(
        recorderBridge = NoopRecorderBridge(),
        insightsBridge = NoopInsightsBridge(),
        motionDataBridge = NoopMotionDataBridge(),
    )

    /** Records the ids it was asked for and hands back a fresh bundle each time. */
    private class RecordingFakePatientScopedBridgesFactory : PatientScopedBridgesFactory {
        val requestedPatientIds = mutableListOf<String>()
        override fun create(patientId: String): PatientScopedBridges {
            requestedPatientIds += patientId
            return PatientScopedBridges(
                recorderBridge = NoopRecorderBridge(),
                insightsBridge = NoopInsightsBridge(),
                motionDataBridge = NoopMotionDataBridge(),
            )
        }
    }
}

// --- minimal no-op bridge fakes (identity is all these tests assert on) -------------------------

private class NoopRecorderBridge : RecorderBridge {
    override val recorderState: StateFlow<OSTRecorderState> = MutableStateFlow(OSTRecorderState.INITIALIZED)
    override val stepsCount: StateFlow<Int> = MutableStateFlow(0)
    override val analyserState: StateFlow<OSTAnalyserState> = MutableStateFlow(OSTAnalyserState.Idle)
    override val currentRecordingWindow: StateFlow<OSTRecordingWindow?> = MutableStateFlow(null)
    override suspend fun prepareForRecording(activityType: OSTActivityType): Boolean = false
    override suspend fun start(
        activityType: OSTActivityType,
        duration: Long?,
        sensorEnhancedMode: Boolean,
        userInputMetadata: OSTUserInputMetaData?,
        customMetadata: Map<String, Any>?,
    ) = Unit
    override suspend fun stop() = Unit
    override fun reset() = Unit
    override suspend fun analyze(timeout: Long): OSTMotionMeasurement? = null
    override suspend fun analyze(uuid: String, timeout: Long, interval: Long): OSTMotionMeasurement? = null
    override suspend fun updateSixMinuteWalkCourseLength(uuid: String, requestBody: OSTWalkCourseLength) = Unit
    override fun currentRecordingLimit(): Long = 0L
    override fun addMarker(marker: String) = Unit
    override suspend fun readSingleMotionMeasurement(uuid: String): OSTMotionMeasurement? = null
    override suspend fun readMotionMeasurements(request: OSTTimeRangedDataRequest): List<OSTMotionMeasurement> = emptyList()
    override suspend fun deleteMotionMeasurement(uuid: String) = Unit
    override suspend fun updateMotionMeasurement(uuid: String, metadata: OSTUserInputMetaData) = Unit
    override suspend fun updateBalanceConditionMetadata(uuid: String, conditions: Map<String, String>) = Unit
    override suspend fun selfReportMotionMeasurement(uuid: String, stsRepetitions: Int): SelfReportResult =
        SelfReportResult.ServerFailure
}

private class NoopInsightsBridge : InsightsBridge {
    override suspend fun getInsights(measurement: OSTMotionMeasurement): OSTInsights? = null
    override suspend fun getInsightsByUuid(uuid: String): OSTInsights? = null
}

private class NoopMotionDataBridge : MotionDataBridge {
    override fun mainParam(motionMeasurement: OSTMotionMeasurement): Pair<OSTParamName, Float>? = null
    override fun getAllParametersMetadata(): Map<OSTParamName, OSTParameterMetadata> = emptyMap()
    override fun getNormByName(name: OSTParamName?): OSTNorm? = null
    override fun getParameterMetadata(paramName: OSTParamName): OSTParameterMetadata =
        throw UnsupportedOperationException("not needed for these tests")
    override fun isWithinNorms(param: OSTParamName, value: Float): Boolean? = null
    override fun discreteScore(motionMeasurement: OSTMotionMeasurement, value: Float): OSTDiscreteColor? = null
    override fun discreteScore(param: OSTParamName, value: Float): OSTDiscreteColor? = null
}
