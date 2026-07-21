package co.onestep.kmp.uikit.bridge.android

import co.onestep.android.core.OSTActivityType as CoreActivityType
import co.onestep.android.core.OSTParamName as CoreParamName
import co.onestep.android.core.OSTIdentificationState as CoreState
import co.onestep.android.core.OSTEvent as CoreEvent
import co.onestep.android.core.insights.OSTDiscreteColor as CoreDiscreteColor
import co.onestep.android.core.insights.OSTInsight as CoreInsight
import co.onestep.android.core.insights.OSTInsightType as CoreInsightType
import co.onestep.android.core.insights.OSTInsightsData as CoreInsights
import co.onestep.android.core.insights.OSTIntent as CoreIntent
import co.onestep.android.core.insights.OSTNorm as CoreNorm
import co.onestep.android.core.insights.OSTNormPart as CoreNormPart
import co.onestep.android.core.insights.OSTParameterMetadata as CoreParameterMetadata
import co.onestep.android.core.monitoring.OSTDailyBackgroundMeasurement as CoreDailyMeasurement
import co.onestep.android.core.motionLab.OSTAnalyserError as CoreAnalyserError
import co.onestep.android.core.motionLab.OSTAnalyserState as CoreAnalyserState
import co.onestep.android.core.motionLab.OSTMeasurementError as CoreError
import co.onestep.android.core.motionLab.OSTMeasurementMetadata as CoreMetadata
import co.onestep.android.core.motionLab.OSTMotionMeasurement as CoreMeasurement
import co.onestep.android.core.motionLab.OSTOrder as CoreOrder
import co.onestep.android.core.motionLab.OSTRecorderState as CoreRecorderState
import co.onestep.android.core.motionLab.OSTResultState as CoreResultState
import co.onestep.android.core.motionLab.OSTTimeRangedDataRequest as CoreTimeRangedDataRequest
import co.onestep.android.core.motionLab.OSTUserInputMetaData as CoreUserInputMetaData
import co.onestep.android.core.motionLab.OSTWalkCourseLength as CoreWalkCourseLength
import co.onestep.kmp.uikit.models.OSTMotionMeasurement.MotionMeasurementStatus as KmpMeasurementStatus
import co.onestep.kmp.uikit.models.OSTActivityType as KmpActivityType
import co.onestep.kmp.uikit.models.OSTAnalyserError as KmpAnalyserError
import co.onestep.kmp.uikit.models.OSTAnalyserState as KmpAnalyserState
import co.onestep.kmp.uikit.models.OSTDailyBackgroundMeasurement as KmpDailyMeasurement
import co.onestep.kmp.uikit.models.OSTDiscreteColor as KmpDiscreteColor
import co.onestep.kmp.sdk.OSTError as KmpError
import co.onestep.kmp.sdk.OSTEvent as KmpEvent
import co.onestep.kmp.uikit.models.OSTInsight as KmpInsight
import co.onestep.kmp.uikit.models.OSTInsightType as KmpInsightType
import co.onestep.kmp.uikit.models.OSTInsights as KmpInsights
import co.onestep.kmp.uikit.models.OSTIntent as KmpIntent
import co.onestep.kmp.uikit.models.OSTMeasurementMetadata as KmpMetadata
import co.onestep.kmp.uikit.models.OSTMotionMeasurement as KmpMeasurement
import co.onestep.kmp.uikit.models.OSTNorm as KmpNorm
import co.onestep.kmp.uikit.models.OSTNormPart as KmpNormPart
import co.onestep.kmp.uikit.models.OSTOrder as KmpOrder
import co.onestep.kmp.uikit.models.OSTParamName as KmpParamName
import co.onestep.kmp.uikit.models.OSTParameterMetadata as KmpParameterMetadata
import co.onestep.kmp.uikit.models.OSTRecorderState as KmpRecorderState
import co.onestep.kmp.uikit.models.OSTResultState as KmpResultState
import co.onestep.kmp.uikit.models.OSTState as KmpState
import co.onestep.kmp.uikit.models.OSTTimeRangedDataRequest as KmpTimeRangedDataRequest
import co.onestep.kmp.uikit.models.OSTUserInputMetaData as KmpUserInputMetaData
import co.onestep.kmp.uikit.models.OSTWalkCourseLength as KmpWalkCourseLength
import java.util.Date

// ── State ────────────────────────────────────────────────────────────────────

fun CoreState.toKmp(): KmpState = when (this) {
    is CoreState.Unidentified -> KmpState.Ready
    is CoreState.Identified -> KmpState.Identified(patientId.value)
    // shortcut: OSTError has no numeric code in the new API; 0 is a sentinel.
    is CoreState.Lost -> KmpState.Error(code = 0, message = cause.message)
}

// ── Event ────────────────────────────────────────────────────────────────────

fun CoreEvent.toKmp(): KmpEvent = KmpEvent(
    name = name,
    properties = properties.mapValues { it.value.toString() },
    timestamp = timestamp,
)

fun KmpEvent.toCore(): CoreEvent = CoreEvent(
    name = name,
    properties = properties,
    timestamp = timestamp,
)

// ── ActivityType ─────────────────────────────────────────────────────────────

fun CoreActivityType.toKmp(): KmpActivityType =
    KmpActivityType.entries.firstOrNull { it.name == this.name } ?: KmpActivityType.WALK

fun KmpActivityType.toCore(): CoreActivityType =
    CoreActivityType.entries.firstOrNull { it.name == this.name } ?: CoreActivityType.WALK

// ── ParamName ────────────────────────────────────────────────────────────────

fun CoreParamName.toKmp(): KmpParamName? =
    KmpParamName.entries.firstOrNull { it.name == this.name }

fun KmpParamName.toCore(): CoreParamName? =
    CoreParamName.entries.firstOrNull { it.name == this.name }

// ── RecorderState ────────────────────────────────────────────────────────────

fun CoreRecorderState.toKmp(): KmpRecorderState =
    KmpRecorderState.entries.firstOrNull { it.name == this.name } ?: KmpRecorderState.INITIALIZED

// ── AnalyserState ────────────────────────────────────────────────────────────

fun CoreAnalyserState.toKmp(): KmpAnalyserState = when (this) {
    is CoreAnalyserState.Idle -> KmpAnalyserState.Idle
    is CoreAnalyserState.Uploading -> KmpAnalyserState.Uploading
    is CoreAnalyserState.Analyzing -> KmpAnalyserState.Analyzing
    is CoreAnalyserState.Analyzed -> KmpAnalyserState.Analyzed
    is CoreAnalyserState.Failed -> KmpAnalyserState.Failed(
        throwable = throwable,
        error = error.toKmp(),
    )
}

fun CoreAnalyserError.toKmp(): KmpAnalyserError = when (this) {
    is CoreAnalyserError.TooShort -> KmpAnalyserError.TooShort(error)
    is CoreAnalyserError.General -> KmpAnalyserError.General(throwable, error)
    is CoreAnalyserError.Timeout -> KmpAnalyserError.Timeout(throwable, error)
    is CoreAnalyserError.ServerError -> KmpAnalyserError.ServerError(throwable, error)
    is CoreAnalyserError.NetworkError -> KmpAnalyserError.NetworkError(throwable, error)
}

// ── ResultState ──────────────────────────────────────────────────────────────

fun CoreResultState.toKmp(): KmpResultState? =
    KmpResultState.entries.firstOrNull { it.value == this.value }

fun KmpResultState.toCore(): CoreResultState? =
    CoreResultState.entries.firstOrNull { it.value == this.value }

// ── Error ────────────────────────────────────────────────────────────────────

// shortcut: OSTMeasurementError exposes only code + message; KmpError.details has no source.
fun CoreError.toKmp(): KmpError = KmpError(code = code, message = message, details = null)

fun KmpError.toCore(): CoreError = CoreError(code = code, message = message)

// ── WalkCourseLength ─────────────────────────────────────────────────────────

fun CoreWalkCourseLength.toKmp(): KmpWalkCourseLength = KmpWalkCourseLength(value = value, unit = unit)

fun KmpWalkCourseLength.toCore(): CoreWalkCourseLength = CoreWalkCourseLength(value = value, unit = unit)

// ── MeasurementMetadata ──────────────────────────────────────────────────────

fun CoreMetadata.toKmp(): KmpMetadata = KmpMetadata(
    locale = locale,
    seconds = seconds,
    steps = steps,
    lastModified = lastModified,
    note = note,
    tags = tags,
    assistiveDevice = assistiveDevice,
    levelOfAssistance = levelOfAssistance,
    walkCourseLength = walkCourseLength?.toKmp(),
    selfReport = selfReport,
    geoLat = geoLat,
    geoLng = geoLng,
    dataPath = dataPath,
    audioDataPath = audioDataPath,
)

fun KmpMetadata.toCore(): CoreMetadata = CoreMetadata(
    locale = locale,
    seconds = seconds,
    steps = steps,
    lastModified = lastModified,
    note = note,
    tags = tags,
    assistiveDevice = assistiveDevice,
    levelOfAssistance = levelOfAssistance,
    walkCourseLength = walkCourseLength?.toCore(),
    selfReport = selfReport,
    geoLat = geoLat,
    geoLng = geoLng,
    dataPath = dataPath,
    audioDataPath = audioDataPath,
)

// ── OSTMotionMeasurement ─────────────────────────────────────────────────────

fun CoreMeasurement.toKmp(): KmpMeasurement = KmpMeasurement(
    id = id,
    timestamp = timestamp.time,
    type = type.toKmp(),
    customMetadata = customMetadata.mapValues { it.value.toString() },
    metadata = metadata.toKmp(),
    params = params.mapKeys { it.key.columnName },
    parameterArrays = parameterArrays,
    status = when (status) {
        CoreMeasurement.MotionMeasurementStatus.NOT_SYNCED -> KmpMeasurementStatus.NOT_SYNCED
        CoreMeasurement.MotionMeasurementStatus.SYNCED -> KmpMeasurementStatus.SYNCED
        CoreMeasurement.MotionMeasurementStatus.ANALYZED -> KmpMeasurementStatus.ANALYZED
    },
    error = error?.toKmp(),
    resultState = resultState?.toKmp(),
    summaryUrl = summaryUrl,
)

fun KmpMeasurement.toCore(): CoreMeasurement {
    val coreParams = params.mapNotNull { (key, value) ->
        val paramName = CoreParamName.entries.firstOrNull { it.columnName == key }
        paramName?.let { it to value }
    }.toMap()

    return CoreMeasurement(
        id = id,
        timestamp = Date(timestamp),
        type = type.toCore(),
        customMetadata = customMetadata.toMap(),
        metadata = metadata.toCore(),
        params = coreParams,
        parameterArrays = parameterArrays,
        status = when (status) {
            KmpMeasurementStatus.NOT_SYNCED -> CoreMeasurement.MotionMeasurementStatus.NOT_SYNCED
            KmpMeasurementStatus.SYNCED -> CoreMeasurement.MotionMeasurementStatus.SYNCED
            KmpMeasurementStatus.ANALYZED -> CoreMeasurement.MotionMeasurementStatus.ANALYZED
        },
        error = error?.toCore(),
        resultState = resultState?.toCore(),
    )
}

// ── UserInputMetaData ────────────────────────────────────────────────────────

fun KmpUserInputMetaData.toCore(): CoreUserInputMetaData = CoreUserInputMetaData(
    note = note,
    tags = tags,
    assistiveDevice = assistiveDevice?.let { device ->
        co.onestep.android.core.motionLab.OSTAssistiveDevice.entries
            .firstOrNull { it.value == device.value }
    },
    levelOfAssistance = levelOfAssistance?.let { level ->
        co.onestep.android.core.motionLab.OSTLevelOfAssistance.entries
            .firstOrNull { it.value == level.value }
    },
    walkCourseLength = walkCourseLength?.toCore(),
)

// ── TimeRangedDataRequest ────────────────────────────────────────────────────

fun KmpTimeRangedDataRequest.toCore(): CoreTimeRangedDataRequest = CoreTimeRangedDataRequest(
    limit = limit,
    order = order?.let {
        when (it) {
            KmpOrder.ASCENDING -> CoreOrder.ASCENDING
            KmpOrder.DESCENDING -> CoreOrder.DESCENDING
            else -> null
        }
    },
    activityType = activityType,
)

// ── DailyBackgroundMeasurement ───────────────────────────────────────────────

fun CoreDailyMeasurement.toKmp(): KmpDailyMeasurement = KmpDailyMeasurement(
    dateLocal = dateLocal,
    timestamp = timestamp,
    parameters = parameters.mapKeys { it.key.columnName },
    lastModified = lastModified,
)

// ── Insights ─────────────────────────────────────────────────────────────────

fun CoreInsights.toKmp(): KmpInsights = KmpInsights(
    uuid = uuid,
    insights = insights.map { it.toKmp() },
)

fun CoreInsight.toKmp(): KmpInsight = KmpInsight(
    paramName = paramName?.toKmp(),
    textMarkdown = textMarkdown,
    intent = intent?.toKmp(),
    insightType = insightType?.toKmp(),
    rank = rank,
)

fun CoreIntent.toKmp(): KmpIntent = when (this) {
    CoreIntent.GOOD -> KmpIntent.GOOD
    CoreIntent.NEUTRAL -> KmpIntent.NEUTRAL
    CoreIntent.BAD -> KmpIntent.BAD
}

fun CoreInsightType.toKmp(): KmpInsightType = when (this) {
    CoreInsightType.TREND -> KmpInsightType.TREND
    CoreInsightType.COMPARISON -> KmpInsightType.COMPARISON
    CoreInsightType.PARAMETER -> KmpInsightType.PARAMETER
    CoreInsightType.FALL_RISK -> KmpInsightType.FALL_RISK
    CoreInsightType.EDUCATION -> KmpInsightType.EDUCATION
    CoreInsightType.INFO -> KmpInsightType.INFO
}

// ── DiscreteColor ────────────────────────────────────────────────────────────

fun CoreDiscreteColor.toKmp(): KmpDiscreteColor = when (this) {
    CoreDiscreteColor.Red -> KmpDiscreteColor.Red
    CoreDiscreteColor.Green -> KmpDiscreteColor.Green
    CoreDiscreteColor.Yellow -> KmpDiscreteColor.Yellow
    CoreDiscreteColor.DarkRed -> KmpDiscreteColor.DarkRed
}

// ── Norm ─────────────────────────────────────────────────────────────────────

fun CoreNorm.toKmp(): KmpNorm = KmpNorm(
    units = units,
    parts = parts?.map { it.toKmp() },
)

fun CoreNormPart.toKmp(): KmpNormPart = KmpNormPart(
    start = start,
    end = end,
    color = color,
    includeStart = includeStart,
    includeEnd = includeEnd,
)

// ── ParameterMetadata ────────────────────────────────────────────────────────

fun CoreParameterMetadata.toKmp(): KmpParameterMetadata = KmpParameterMetadata(
    activity = activity.toKmp(),
    displayName = displayName,
    units = units,
    imperialUnits = imperialUnits,
    category = category,
    lowRange = lowRange,
    sortKey = sortKey,
    isMainParam = isMainParam,
    highRange = highRange,
    roundDigits = roundDigits,
    imperialRoundDigits = imperialRoundDigits,
)

