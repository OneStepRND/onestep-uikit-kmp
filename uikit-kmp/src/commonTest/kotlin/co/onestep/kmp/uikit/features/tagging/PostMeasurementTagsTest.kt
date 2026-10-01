package co.onestep.kmp.uikit.features.tagging

import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingQuestionData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * OS-17656: the post-measurement submit saved `existing + submitted`, which duplicated the
 * pre-recording footwear, kept a replaced footwear, dropped custom answers that read like a
 * footwear title and never persisted a changed or removed custom answer.
 */
class PostMeasurementTagsTest {

    private val footwearTitles = listOf("With shoes", "Barefoot", "Slippers")

    private fun question(vararg values: String, answers: List<String>? = null) =
        OSTRecordingQuestionData(title = "Q", tagsValues = values.toList()).apply { selectedAnswers = answers }

    @Test
    fun preRecordingFootwear_isSavedOnce() {
        val tags = PostMeasurementTags.merged(
            existing = listOf("With shoes"),
            seededFootwear = "With shoes",
            footwear = "With shoes",
            questions = null,
        )

        assertEquals(listOf("With shoes"), tags)
    }

    @Test
    fun resubmitting_isIdempotent() {
        val surface = question("Carpet", "Tile", answers = listOf("Carpet"))
        val once = PostMeasurementTags.merged(listOf("With shoes"), "With shoes", "With shoes", listOf(surface))
        val twice = PostMeasurementTags.merged(once, "With shoes", "With shoes", listOf(surface))

        assertEquals(listOf("With shoes", "Carpet"), once)
        assertEquals(once, twice)
    }

    @Test
    fun changedFootwear_replacesTheSeededOne() {
        val tags = PostMeasurementTags.merged(
            existing = listOf("With shoes", "Carpet"),
            seededFootwear = "With shoes",
            footwear = "Barefoot",
            questions = null,
        )

        assertEquals(listOf("Carpet", "Barefoot"), tags)
    }

    @Test
    fun footwearChangedToNone_removesIt() {
        val tags = PostMeasurementTags.merged(
            existing = listOf("With shoes", "Carpet"),
            seededFootwear = "With shoes",
            footwear = null,
            questions = null,
        )

        assertEquals(listOf("Carpet"), tags)
    }

    @Test
    fun customAnswerEqualToAFootwearTitle_isKept() {
        val existing = listOf("Slippers", "Barefoot")
        val seeded = PostMeasurementTags.seededFootwearTag(
            existing = existing,
            footwearTitles = footwearTitles,
            questionTagValues = setOf("Slippers", "Socks"),
        )
        assertEquals("Barefoot", seeded)

        val tags = PostMeasurementTags.merged(existing, seeded, footwear = "With shoes", questions = null)

        assertEquals(listOf("Slippers", "With shoes"), tags)
    }

    @Test
    fun questionsFlow_keepsTheFootwearTag() {
        val tags = PostMeasurementTags.merged(
            existing = listOf("Barefoot"),
            seededFootwear = null,
            footwear = null,
            questions = listOf(question("Carpet", "Tile", answers = emptyList())),
        )

        assertEquals(listOf("Barefoot"), tags)
    }

    @Test
    fun removedAnswer_persists_andUnansweredQuestionKeepsItsTags() {
        val tags = PostMeasurementTags.merged(
            existing = listOf("Carpet", "Dim light"),
            seededFootwear = null,
            footwear = null,
            questions = listOf(
                question("Carpet", "Tile", answers = emptyList()),
                question("Dim light", "Bright light"),
            ),
        )

        assertEquals(listOf("Dim light"), tags)
    }

    @Test
    fun changedAnswer_replacesTheEarlierOne() {
        val tags = PostMeasurementTags.merged(
            existing = listOf("Carpet", "Barefoot"),
            seededFootwear = "Barefoot",
            footwear = "Barefoot",
            questions = listOf(question("Carpet", "Tile", answers = listOf("Tile"))),
        )

        assertEquals(listOf("Barefoot", "Tile"), tags)
    }

    @Test
    fun seededFootwear_fallsBackToTheFirstMatch_andIsNullWithoutOne() {
        assertEquals(
            "Slippers",
            PostMeasurementTags.seededFootwearTag(listOf("Carpet", "Slippers"), footwearTitles, setOf("Slippers")),
        )
        assertNull(PostMeasurementTags.seededFootwearTag(listOf("Carpet", "None"), footwearTitles, emptySet()))
    }
}
