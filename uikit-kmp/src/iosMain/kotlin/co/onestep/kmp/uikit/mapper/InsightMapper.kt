package co.onestep.kmp.uikit.mapper

import co.onestep.kmp.uikit.models.OSTDiscreteColor
import co.onestep.kmp.uikit.models.OSTInsight
import co.onestep.kmp.uikit.models.OSTInsightType
import co.onestep.kmp.uikit.models.OSTInsights
import co.onestep.kmp.uikit.models.OSTIntent
import co.onestep.kmp.uikit.models.OSTNorm
import co.onestep.kmp.uikit.models.OSTNormPart
import co.onestep.kmp.uikit.models.OSTParamName
import co.onestep.kmp.uikit.models.OSTParameterMetadata

/**
 * iOS SDK insight mappers.
 *
 * These factory functions create KMP insight types from raw data.
 * When the iOS OneStep SDK is integrated via cinterop, add direct
 * type mapping functions: `fun IosInsights.toKmp(): OSTInsights`
 */
fun createKmpInsights(
    uuid: String,
    insights: List<OSTInsight>,
): OSTInsights = OSTInsights(uuid = uuid, insights = insights)

fun createKmpInsight(
    paramName: OSTParamName?,
    textMarkdown: String,
    intent: OSTIntent?,
    insightType: OSTInsightType?,
    rank: Float,
): OSTInsight = OSTInsight(
    paramName = paramName,
    textMarkdown = textMarkdown,
    intent = intent,
    insightType = insightType,
    rank = rank,
)

fun String.toKmpInsightType(): OSTInsightType =
    when (this.uppercase()) {
        "TREND" -> OSTInsightType.TREND
        "COMPARISON" -> OSTInsightType.COMPARISON
        "PARAMETER" -> OSTInsightType.PARAMETER
        "FALL_RISK" -> OSTInsightType.FALL_RISK
        "EDUCATION" -> OSTInsightType.EDUCATION
        "INFO" -> OSTInsightType.INFO
        else -> OSTInsightType.INFO
    }

fun String.toKmpIntent(): OSTIntent =
    when (this.uppercase()) {
        "GOOD" -> OSTIntent.GOOD
        "NEUTRAL" -> OSTIntent.NEUTRAL
        "BAD" -> OSTIntent.BAD
        else -> OSTIntent.NEUTRAL
    }

fun String.toKmpDiscreteColor(): OSTDiscreteColor =
    when (this.uppercase()) {
        "RED" -> OSTDiscreteColor.Red
        "GREEN" -> OSTDiscreteColor.Green
        "YELLOW" -> OSTDiscreteColor.Yellow
        "DARK_RED", "DARKRED" -> OSTDiscreteColor.DarkRed
        else -> OSTDiscreteColor.Green
    }

fun createKmpNorm(
    units: String?,
    parts: List<OSTNormPart>?,
): OSTNorm = OSTNorm(units = units, parts = parts)

fun createKmpNormPart(
    start: Float,
    end: Float,
    color: String,
    includeStart: Boolean,
    includeEnd: Boolean,
): OSTNormPart = OSTNormPart(
    start = start,
    end = end,
    color = color,
    includeStart = includeStart,
    includeEnd = includeEnd,
)

fun createKmpParameterMetadata(
    activity: String,
    displayName: String,
    units: String?,
    imperialUnits: String?,
    category: String,
    lowRange: Float?,
    sortKey: Float?,
    isMainParam: Boolean?,
    highRange: Float?,
    roundDigits: Float?,
    imperialRoundDigits: Float?,
): OSTParameterMetadata = OSTParameterMetadata(
    activity = activity.toKmpActivityType(),
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
