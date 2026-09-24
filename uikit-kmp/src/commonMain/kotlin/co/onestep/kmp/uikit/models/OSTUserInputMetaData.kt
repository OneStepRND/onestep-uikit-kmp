package co.onestep.kmp.uikit.models

data class OSTUserInputMetaData(
    var note: String? = null,
    var tags: List<String>? = null,
    var assistiveDevice: OSTAssistiveDevice? = null,
    var levelOfAssistance: OSTLevelOfAssistance? = null,
    var walkCourseLength: OSTWalkCourseLength? = null,
    /**
     * Answers to tag-catalog fields, keyed by each field's `name` (e.g. `"$footwear"`) and
     * submitted as the recording's `tag_map`.
     *
     * Values are option codes, never display labels — see [OSTTagValue]. Leave an unanswered field
     * out: the bridges drop a blank [OSTTagValue.Single] or an empty [OSTTagValue.Multiple] rather
     * than send it. On an update the map is a per-key patch: keys not present keep their stored
     * answer, so an absent map changes nothing.
     */
    var tagMap: Map<String, OSTTagValue>? = null,
)
