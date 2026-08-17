package co.onestep.kmp.sdk

import co.onestep.kmp.uikit.models.OSTActivityType
import co.onestep.kmp.uikit.models.OSTMotionMeasurement
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OSTErrorTest {

    /** Stands in for a consumer that brings its own [OSTError] implementation. */
    private data class HostError(override val message: String) : OSTError

    @Test
    fun hostSuppliedErrorFlowsThroughOSTResult() {
        val result: OSTResult<Int> = OSTResult.Error(HostError("host failure"))

        assertEquals("host failure", result.errorMessage)
        assertEquals(0, result getOr 0)
        assertEquals("HOST FAILURE", result.mapError { HostError(it.message.uppercase()) }.errorMessage)
    }

    @Test
    fun asErrorResultProducesTheSdkError() {
        val cause = "boom".asErrorResult(code = 42, details = "NetworkError").cause

        assertIs<OSTSDKError>(cause)
        assertEquals(42, cause.code)
        assertEquals("boom", cause.message)
        assertEquals("NetworkError", cause.details)
        assertEquals(42, cause.sdkCode)
    }

    @Test
    fun hostSuppliedErrorHasNoSdkCode() {
        assertNull(HostError("no code").sdkCode)
    }

    @Test
    fun measurementErrorKeepsTheSdkJsonShape() {
        val measurement = measurement(OSTSDKError(code = 7, message = "analysis failed", details = "ServerError"))

        val json = Json.encodeToString(OSTMotionMeasurement.serializer(), measurement)

        assertTrue(json.contains("\"error\":{\"code\":7,\"message\":\"analysis failed\",\"details\":\"ServerError\"}"), json)
        assertEquals(measurement, Json.decodeFromString(OSTMotionMeasurement.serializer(), json))
    }

    @Test
    fun hostSuppliedMeasurementErrorSerializesItsMessage() {
        val json = Json.encodeToString(OSTMotionMeasurement.serializer(), measurement(HostError("host failure")))

        val decoded = Json.decodeFromString(OSTMotionMeasurement.serializer(), json).error

        assertIs<OSTSDKError>(decoded)
        assertEquals(0, decoded.code)
        assertEquals("host failure", decoded.message)
    }

    private fun measurement(error: OSTError) = OSTMotionMeasurement(
        id = "measurement-1",
        timestamp = 0L,
        type = OSTActivityType.WALK,
        status = OSTMotionMeasurement.MotionMeasurementStatus.ANALYZED,
        error = error,
    )
}
