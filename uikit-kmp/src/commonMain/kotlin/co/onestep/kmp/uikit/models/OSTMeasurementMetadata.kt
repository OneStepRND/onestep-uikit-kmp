package co.onestep.kmp.uikit.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OSTMeasurementMetadata(
    @SerialName("locale") val locale: String? = null,
    @SerialName("seconds") val seconds: Int? = null,
    @SerialName("steps") val steps: Int? = null,
    @SerialName("last_modified") val lastModified: String? = null,
    @SerialName("note") val note: String? = null,
    @SerialName("tags") val tags: List<String> = emptyList(),
    @SerialName("assistive_device") val assistiveDevice: Int? = null,
    @SerialName("level_of_assistance") val levelOfAssistance: Int? = null,
    @SerialName("walk_course_length") val walkCourseLength: OSTWalkCourseLength? = null,
    @SerialName("self_report") val selfReport: Boolean? = null,
    val geoLat: Double? = null,
    val geoLng: Double? = null,
    val dataPath: String? = null,
    val audioDataPath: String? = null,
)
