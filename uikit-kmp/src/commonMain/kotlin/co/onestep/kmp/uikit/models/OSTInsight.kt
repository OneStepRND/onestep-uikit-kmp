package co.onestep.kmp.uikit.models

data class OSTInsights(
    val uuid: String,
    val insights: List<OSTInsight>,
)

data class OSTInsight(
    val paramName: OSTParamName?,
    val textMarkdown: String,
    val intent: OSTIntent?,
    val insightType: OSTInsightType?,
    val rank: Float,
)

enum class OSTInsightType(
    val value: String,
) {
    TREND("trend"),
    COMPARISON("comparison"),
    PARAMETER("parameter"),
    FALL_RISK("fall_risk"),
    EDUCATION("education"),
    INFO("info"),
}

fun String.toInsightType(): OSTInsightType? =
    OSTInsightType.entries.find { it.value == this }

enum class OSTIntent(
    val value: String,
) {
    GOOD("good"),
    NEUTRAL("neutral"),
    BAD("bad"),
}

fun String.toIntent(): OSTIntent? =
    OSTIntent.entries.find { it.value == this }

enum class OSTDiscreteColor(
    val value: String,
) {
    Red("red"),
    Green("green"),
    Yellow("yellow"),
    DarkRed("dark_red"),
}

fun String.toDiscreteColor(): OSTDiscreteColor? =
    OSTDiscreteColor.entries.find { it.value == this }
