package co.onestep.kmp.uikit.models

import kotlinx.serialization.Serializable

@Serializable
enum class OSTLevelOfAssistance(
    val value: Int,
    val displayNameKey: String,
) {
    INDEPENDENT(1, "independent"),
    MODIFIED_INDEPENDENT(2, "modified_independent"),
    STANDBY_ASSISTANCE(3, "standby_assistance"),
    MIN_ASSISTANCE(4, "minimal_assistance"),
    MODERATE_ASSISTANCE(5, "moderate_assistance"),
    MAX_ASSISTANCE(6, "maximum_assistance"),
    TOTAL_ASSISTANCE(7, "total_assistance"),
    UNABLE_TO_PERFORM(8, "unable_to_perform_at_this_time"),
    ;

    override fun toString(): String = "LevelOfAssistance(value=$value)"

    companion object {
        fun Int.toLevelOfAssistance(): OSTLevelOfAssistance =
            when (this) {
                INDEPENDENT.value -> INDEPENDENT
                MODIFIED_INDEPENDENT.value -> MODIFIED_INDEPENDENT
                STANDBY_ASSISTANCE.value -> STANDBY_ASSISTANCE
                MIN_ASSISTANCE.value -> MIN_ASSISTANCE
                MODERATE_ASSISTANCE.value -> MODERATE_ASSISTANCE
                MAX_ASSISTANCE.value -> MAX_ASSISTANCE
                TOTAL_ASSISTANCE.value -> TOTAL_ASSISTANCE
                UNABLE_TO_PERFORM.value -> UNABLE_TO_PERFORM
                else -> INDEPENDENT
            }

        fun Int.fromIndex(levels: List<OSTLevelOfAssistance>): OSTLevelOfAssistance =
            if (this in levels.indices) levels[this] else INDEPENDENT
    }
}
