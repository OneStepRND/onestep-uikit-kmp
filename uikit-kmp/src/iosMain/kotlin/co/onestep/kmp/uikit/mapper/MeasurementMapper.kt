package co.onestep.kmp.uikit.mapper

import co.onestep.kmp.uikit.models.OSTError
import co.onestep.kmp.uikit.models.OSTMeasurementMetadata
import co.onestep.kmp.uikit.models.OSTMotionMeasurement
import co.onestep.kmp.uikit.models.OSTResultState
import co.onestep.kmp.uikit.models.OSTWalkCourseLength

/**
 * iOS SDK measurement mappers.
 *
 * Factory functions to create KMP measurement types from iOS SDK data.
 * When the iOS OneStep SDK is integrated via cinterop, add direct type
 * mapping: `fun IosMeasurement.toKmp(): OSTMotionMeasurement`
 */
fun createKmpMeasurement(
    id: String,
    timestamp: Long,
    activityType: String,
    customMetadata: Map<String, String>,
    metadata: OSTMeasurementMetadata,
    params: Map<String, Float>,
    parameterArrays: Map<String, List<Float>>,
    status: String,
    error: OSTError?,
    resultState: String?,
): OSTMotionMeasurement = OSTMotionMeasurement(
    id = id,
    timestamp = timestamp,
    type = activityType.toKmpActivityType(),
    customMetadata = customMetadata,
    metadata = metadata,
    params = params,
    parameterArrays = parameterArrays,
    status = status.toKmpMeasurementStatus(),
    error = error,
    resultState = resultState?.toKmpResultState(),
)

fun String.toKmpMeasurementStatus(): OSTMotionMeasurement.MotionMeasurementStatus =
    when (this.uppercase()) {
        "NOT_SYNCED" -> OSTMotionMeasurement.MotionMeasurementStatus.NOT_SYNCED
        "SYNCED" -> OSTMotionMeasurement.MotionMeasurementStatus.SYNCED
        "ANALYZED" -> OSTMotionMeasurement.MotionMeasurementStatus.ANALYZED
        else -> OSTMotionMeasurement.MotionMeasurementStatus.NOT_SYNCED
    }

fun String.toKmpResultState(): OSTResultState =
    when (this.uppercase()) {
        "FULL_ANALYSIS" -> OSTResultState.FULL_ANALYSIS
        "PARTIAL_ANALYSIS" -> OSTResultState.PARTIAL_ANALYSIS
        "EMPTY_ANALYSIS" -> OSTResultState.EMPTY_ANALYSIS
        else -> OSTResultState.EMPTY_ANALYSIS
    }

fun createKmpMetadata(
    locale: String?,
    seconds: Int?,
    steps: Int?,
    lastModified: String?,
    note: String?,
    tags: List<String>,
    assistiveDevice: Int?,
    levelOfAssistance: Int?,
    walkCourseLength: OSTWalkCourseLength?,
    geoLat: Double?,
    geoLng: Double?,
    dataPath: String?,
    audioDataPath: String?,
): OSTMeasurementMetadata = OSTMeasurementMetadata(
    locale = locale,
    seconds = seconds,
    steps = steps,
    lastModified = lastModified,
    note = note,
    tags = tags,
    assistiveDevice = assistiveDevice,
    levelOfAssistance = levelOfAssistance,
    walkCourseLength = walkCourseLength,
    geoLat = geoLat,
    geoLng = geoLng,
    dataPath = dataPath,
    audioDataPath = audioDataPath,
)

fun createKmpWalkCourseLength(
    value: Int,
    unit: String,
): OSTWalkCourseLength = OSTWalkCourseLength(value = value, unit = unit)
