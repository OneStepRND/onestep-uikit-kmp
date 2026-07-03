package co.onestep.kmp.uikit.mapper

import co.onestep.kmp.uikit.models.OSTDiscreteColor as KmpDiscreteColor
import co.onestep.kmp.uikit.models.OSTInsight as KmpInsight
import co.onestep.kmp.uikit.models.OSTInsightType as KmpInsightType
import co.onestep.kmp.uikit.models.OSTInsights as KmpInsights
import co.onestep.kmp.uikit.models.OSTIntent as KmpIntent
import co.onestep.kmp.uikit.models.OSTNorm as KmpNorm
import co.onestep.kmp.uikit.models.OSTNormPart as KmpNormPart
import co.onestep.kmp.uikit.models.OSTParameterMetadata as KmpParameterMetadata
import co.onestep.android.core.insights.OSTDiscreteColor as CoreDiscreteColor
import co.onestep.android.core.insights.OSTInsight as CoreInsight
import co.onestep.android.core.insights.OSTInsightType as CoreInsightType
import co.onestep.android.core.insights.OSTInsightsData as CoreInsights
import co.onestep.android.core.insights.OSTIntent as CoreIntent
import co.onestep.android.core.insights.OSTNorm as CoreNorm
import co.onestep.android.core.insights.OSTNormPart as CoreNormPart
import co.onestep.android.core.insights.OSTParameterMetadata as CoreParameterMetadata

fun CoreInsights.toKmp(): KmpInsights =
    KmpInsights(
        uuid = uuid,
        insights = insights.map { it.toKmp() },
    )

fun CoreInsight.toKmp(): KmpInsight =
    KmpInsight(
        paramName = paramName?.toKmp(),
        textMarkdown = textMarkdown,
        intent = intent?.toKmp(),
        insightType = insightType?.toKmp(),
        rank = rank,
    )

fun CoreInsightType.toKmp(): KmpInsightType =
    when (this) {
        CoreInsightType.TREND -> KmpInsightType.TREND
        CoreInsightType.COMPARISON -> KmpInsightType.COMPARISON
        CoreInsightType.PARAMETER -> KmpInsightType.PARAMETER
        CoreInsightType.FALL_RISK -> KmpInsightType.FALL_RISK
        CoreInsightType.EDUCATION -> KmpInsightType.EDUCATION
        CoreInsightType.INFO -> KmpInsightType.INFO
    }

fun CoreIntent.toKmp(): KmpIntent =
    when (this) {
        CoreIntent.GOOD -> KmpIntent.GOOD
        CoreIntent.NEUTRAL -> KmpIntent.NEUTRAL
        CoreIntent.BAD -> KmpIntent.BAD
    }

fun CoreDiscreteColor.toKmp(): KmpDiscreteColor =
    when (this) {
        CoreDiscreteColor.Red -> KmpDiscreteColor.Red
        CoreDiscreteColor.Green -> KmpDiscreteColor.Green
        CoreDiscreteColor.Yellow -> KmpDiscreteColor.Yellow
        CoreDiscreteColor.DarkRed -> KmpDiscreteColor.DarkRed
    }

fun CoreNorm.toKmp(): KmpNorm =
    KmpNorm(
        units = units,
        parts = parts?.map { it.toKmp() },
    )

fun CoreNormPart.toKmp(): KmpNormPart =
    KmpNormPart(
        start = start,
        end = end,
        color = color,
        includeStart = includeStart,
        includeEnd = includeEnd,
    )

fun CoreParameterMetadata.toKmp(): KmpParameterMetadata =
    KmpParameterMetadata(
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
