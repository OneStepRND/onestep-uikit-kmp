package co.onestep.kmp.uikit.features.summary.presentation

import co.onestep.kmp.uikit.models.OSTInsightType
import co.onestep.kmp.uikit.models.OSTIntent
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.default_insight
import co.onestep.kmp.uikit_kmp.generated.resources.ic_book
import co.onestep.kmp.uikit_kmp.generated.resources.ic_lightbulb
import co.onestep.kmp.uikit_kmp.generated.resources.trend_down_red
import co.onestep.kmp.uikit_kmp.generated.resources.trend_steady_red
import co.onestep.kmp.uikit_kmp.generated.resources.trend_up_green
import org.jetbrains.compose.resources.DrawableResource

fun OSTInsightType.toIcon(intent: OSTIntent?): DrawableResource =
    when (this) {
        OSTInsightType.TREND -> when (intent) {
            OSTIntent.GOOD -> Res.drawable.trend_up_green
            OSTIntent.NEUTRAL -> Res.drawable.trend_steady_red
            OSTIntent.BAD -> Res.drawable.trend_down_red
            else -> Res.drawable.default_insight
        }
        OSTInsightType.EDUCATION -> Res.drawable.ic_lightbulb
        OSTInsightType.FALL_RISK -> Res.drawable.trend_down_red
        OSTInsightType.COMPARISON -> Res.drawable.ic_book
        OSTInsightType.PARAMETER -> Res.drawable.default_insight
        OSTInsightType.INFO -> Res.drawable.default_insight
    }
