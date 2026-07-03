package co.onestep.kmp.uikit.data

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

    companion object {
        val values = listOf(OneMinute(), ThreeMinute(), FiveMinute(), Unrestricted())

        fun durationByIndex(int: Int) = values[int]
    }
}
