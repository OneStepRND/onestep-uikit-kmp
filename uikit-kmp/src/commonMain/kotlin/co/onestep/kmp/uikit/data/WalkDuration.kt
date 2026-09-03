package co.onestep.kmp.uikit.data

import co.onestep.kmp.uikit.models.OSTActivityType

/**
 * The selectable recording lengths offered by the duration-selection screen.
 *
 * The screen is shared: the walk tests pick from [values], the Static Balance Test from
 * [staticBalanceValues] (OS-17175). The screen reports the tapped option as an *index*, so
 * every index-to-duration lookup must go through [optionsFor] with the same activity type the
 * screen was rendered with — see [durationByIndex].
 */
internal sealed class WalkDuration(
    open val duration: Int,
) {
    data class OneMinute(
        override val duration: Int = 60,
    ) : WalkDuration(duration)

    data class ThreeMinute(
        override val duration: Int = 180,
    ) : WalkDuration(duration)

    data class FiveMinute(
        override val duration: Int = 300,
    ) : WalkDuration(duration)

    data class Unrestricted(
        override val duration: Int = -1,
    ) : WalkDuration(duration)

    /** A fixed number of seconds. Used for the Static Balance per-condition options. */
    data class Seconds(
        override val duration: Int,
    ) : WalkDuration(duration)

    companion object {
        val values = listOf(OneMinute(), ThreeMinute(), FiveMinute(), Unrestricted())

        /**
         * Static Balance per-condition lengths. No "unrestricted" option: a sway measurement
         * is only comparable against a known window.
         */
        val staticBalanceValues = listOf(Seconds(10), Seconds(20), Seconds(30))

        fun optionsFor(activityType: OSTActivityType): List<WalkDuration> =
            when (activityType) {
                OSTActivityType.STATIC_BALANCE -> staticBalanceValues
                else -> values
            }

        /**
         * Resolves the tapped option index against the list that activity actually shows.
         * Returns null for an index outside that list rather than throwing — the caller keeps
         * the configured default instead of losing the recording to an IndexOutOfBounds.
         */
        fun durationByIndex(
            index: Int,
            activityType: OSTActivityType,
        ): WalkDuration? = optionsFor(activityType).getOrNull(index)
    }
}
