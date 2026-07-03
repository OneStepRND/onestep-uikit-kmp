package co.onestep.kmp.uikit.mapper

import co.onestep.kmp.uikit.models.OSTError as KmpError
import co.onestep.kmp.uikit.models.OSTMeasurementMetadata as KmpMetadata
import co.onestep.kmp.uikit.models.OSTMotionMeasurement as KmpMeasurement
import co.onestep.kmp.uikit.models.OSTResultState as KmpResultState
import co.onestep.kmp.uikit.models.OSTWalkCourseLength as KmpWalkCourseLength
import co.onestep.android.core.motionLab.OSTMeasurementError as CoreError
import co.onestep.android.core.motionLab.OSTMeasurementMetadata as CoreMetadata
import co.onestep.android.core.motionLab.OSTMotionMeasurement as CoreMeasurement
import co.onestep.android.core.motionLab.OSTResultState as CoreResultState
import co.onestep.android.core.motionLab.OSTWalkCourseLength as CoreWalkCourseLength

fun CoreMeasurement.toKmp(): KmpMeasurement =
    KmpMeasurement(
        id = id,
        timestamp = timestamp.time,
        type = type.toKmp(),
        customMetadata = customMetadata.mapValues { it.value.toString() },
        metadata = metadata.toKmp(),
        params = params.mapKeys { (key, _) -> key.columnName },
        parameterArrays = parameterArrays,
        status = status.toKmp(),
        error = error?.toKmp(),
        resultState = resultState?.toKmp(),
    )

fun CoreMeasurement.MotionMeasurementStatus.toKmp(): KmpMeasurement.MotionMeasurementStatus =
    when (this) {
        CoreMeasurement.MotionMeasurementStatus.NOT_SYNCED -> KmpMeasurement.MotionMeasurementStatus.NOT_SYNCED
        CoreMeasurement.MotionMeasurementStatus.SYNCED -> KmpMeasurement.MotionMeasurementStatus.SYNCED
        CoreMeasurement.MotionMeasurementStatus.ANALYZED -> KmpMeasurement.MotionMeasurementStatus.ANALYZED
    }

fun CoreResultState.toKmp(): KmpResultState =
    when (this) {
        CoreResultState.FULL_ANALYSIS -> KmpResultState.FULL_ANALYSIS
        CoreResultState.PARTIAL_ANALYSIS -> KmpResultState.PARTIAL_ANALYSIS
        CoreResultState.EMPTY_ANALYSIS -> KmpResultState.EMPTY_ANALYSIS
    }

// shortcut: OSTMeasurementError exposes only code + message; KmpError.details has no source.
fun CoreError.toKmp(): KmpError =
    KmpError(code = code, message = message, details = null)

fun CoreMetadata.toKmp(): KmpMetadata =
    KmpMetadata(
        locale = locale,
        seconds = seconds,
        steps = steps,
        lastModified = lastModified,
        note = note,
        tags = tags,
        assistiveDevice = assistiveDevice,
        levelOfAssistance = levelOfAssistance,
        walkCourseLength = walkCourseLength?.toKmp(),
        geoLat = geoLat,
        geoLng = geoLng,
        dataPath = dataPath,
        audioDataPath = audioDataPath,
    )

fun CoreWalkCourseLength.toKmp(): KmpWalkCourseLength =
    KmpWalkCourseLength(value = value, unit = unit)

fun KmpWalkCourseLength.toCore(): CoreWalkCourseLength =
    CoreWalkCourseLength(value = value, unit = unit)
