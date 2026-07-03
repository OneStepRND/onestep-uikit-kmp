package co.onestep.kmp.uikit.features.recordFlow.screensData

import co.onestep.kmp.uikit.models.OSTActivityType

internal const val HALLWAY_RECOMMENDED_METERS = 30
internal const val HALLWAY_RECOMMENDED_FEET = 98
internal const val HALLWAY_RECOMMENDED_METERS_2MIN = 15
internal const val HALLWAY_RECOMMENDED_FEET_2MIN = 49
internal const val HALLWAY_MIN_METERS = 1
internal const val HALLWAY_MAX_METERS = 100
internal const val HALLWAY_MIN_FEET = 3
internal const val HALLWAY_MAX_FEET = 350
internal const val HALLWAY_INPUT_MAX_DIGITS = 3

internal enum class WalkDurationType(val recommendedMeters: Int, val recommendedFeet: Int) {
    SIX_MINUTE(HALLWAY_RECOMMENDED_METERS, HALLWAY_RECOMMENDED_FEET),
    TWO_MINUTE(HALLWAY_RECOMMENDED_METERS_2MIN, HALLWAY_RECOMMENDED_FEET_2MIN);
    companion object {
        fun from(activityType: OSTActivityType?): WalkDurationType? = when (activityType) {
            OSTActivityType.SIX_MINUTE_WALK -> SIX_MINUTE
            OSTActivityType.TWO_MINUTE_WALK -> TWO_MINUTE
            else -> null
        }
    }
}

internal fun filterHallwayDigits(rawValue: String): String = rawValue.filter { it.isDigit() }.take(HALLWAY_INPUT_MAX_DIGITS)

internal fun hallwayRange(isImperial: Boolean): Pair<Int, Int> =
    if (isImperial) HALLWAY_MIN_FEET to HALLWAY_MAX_FEET
    else HALLWAY_MIN_METERS to HALLWAY_MAX_METERS

internal fun hallwayRecommended(isImperial: Boolean, activityType: OSTActivityType? = null): Int {
    val walkType = WalkDurationType.from(activityType) ?: WalkDurationType.SIX_MINUTE
    return if (isImperial) walkType.recommendedFeet else walkType.recommendedMeters
}

internal fun isSixOrTwoMinWalk(activityType: OSTActivityType?): Boolean =
    activityType == OSTActivityType.TWO_MINUTE_WALK || activityType == OSTActivityType.SIX_MINUTE_WALK
