package co.onestep.kmp.uikit.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OSTWalkCourseLength(
    @SerialName("value") val value: Int,
    @SerialName("unit") val unit: String,
) {
    companion object {
        const val FEET_UNIT = "feet"
        const val METERS_UNIT = "meters"

        fun getWalkCourseLength(
            hallwayLength: Int,
            isImperial: Boolean,
        ) = OSTWalkCourseLength(
            value = hallwayLength,
            unit = if (isImperial) FEET_UNIT else METERS_UNIT,
        )
    }
}
