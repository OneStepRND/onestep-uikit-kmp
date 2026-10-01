package co.onestep.kmp.uikit.features.tagging

import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingQuestionData

/**
 * Builds the measurement's legacy `tags` when post-measurement tagging is submitted (OS-17656,
 * ported from the iOS UI kit's `PostMeasurementTags`).
 *
 * The summary used to save `existing + submitted`: the pre-recording footwear is already in
 * `existing` and the screen re-submits it, so it was saved twice; a changed footwear left the old
 * one behind; re-submitting duplicated every answer; an empty submit dropped every tag that read
 * like a footwear title, custom answers included; and a changed or removed custom answer never
 * replaced the earlier one. [merged] is idempotent instead: the screen's footwear replaces the one
 * it was seeded with, each answered question replaces its own earlier answers, and no tag appears
 * twice.
 *
 * Only the legacy `tags` list goes through here. The tag catalog's `tag_map` is a separate per-key
 * patch submitted on its own path, and these updates never carry one.
 */
internal object PostMeasurementTags {

    /**
     * The tag the post-measurement footwear row is seeded from, or null when there is none.
     *
     * @param footwearTitles the localized titles of every footwear except `Footwear.NONE`, which is
     *        never saved as a tag and whose title ("None") collides with ordinary custom answers.
     * @param questionTagValues every value a custom question can save. A tag that is both a
     *        footwear title and one of these is more likely a custom answer, so a footwear-only
     *        match wins; the first match is the fallback.
     */
    fun seededFootwearTag(
        existing: List<String>,
        footwearTitles: Collection<String>,
        questionTagValues: Collection<String>,
    ): String? {
        val matches = existing.filter { it in footwearTitles }
        return matches.firstOrNull { it !in questionTagValues } ?: matches.firstOrNull()
    }

    /**
     * @param existing the measurement's tags as the summary loaded them.
     * @param seededFootwear the tag the footwear row was seeded from ([seededFootwearTag]); only
     *        that value is replaced, so a custom answer that reads like a footwear title is kept.
     *        Null on the questions flow, which has no footwear row and keeps any footwear tag.
     * @param footwear the footwear title to save, or null for none (`Footwear.NONE` or no row).
     * @param questions the post-measurement questions. One whose `selectedAnswers` is non-null
     *        was answered on this visit and replaces all of its values in [existing] — an empty
     *        list therefore persists a removal. An unanswered (null) question keeps its tags.
     */
    fun merged(
        existing: List<String>,
        seededFootwear: String?,
        footwear: String?,
        questions: List<OSTRecordingQuestionData>?,
    ): List<String> {
        val answered = questions.orEmpty().filter { it.selectedAnswers != null }
        val replaced = buildSet {
            seededFootwear?.let(::add)
            answered.forEach { addAll(it.tagsValues) }
        }
        return buildList {
            addAll(existing.filterNot { it in replaced })
            footwear?.let(::add)
            answered.forEach { addAll(it.selectedAnswers.orEmpty()) }
        }.distinct()
    }
}
