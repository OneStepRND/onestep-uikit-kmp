package co.onestep.kmp.uikit.models

data class OSTUserInputMetaData(
    var note: String? = null,
    var tags: List<String>? = null,
    var assistiveDevice: OSTAssistiveDevice? = null,
    var levelOfAssistance: OSTLevelOfAssistance? = null,
    var walkCourseLength: OSTWalkCourseLength? = null,
)
