package co.onestep.kmp.uikit.features.recordFlow.configurations

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RecordingQuestionIconsTest {

    /**
     * The nine HSL dual-task answers (OS-16296) are the reason this registry exists, and they
     * reach it as tag values off `config/measurement-overrides` — so a typo here is a screen that
     * renders text-only with no error anywhere.
     */
    @Test
    fun `every HSL dual task answer resolves to an illustration`() {
        val answers = listOf(
            "Pants",
            "Shorts",
            "Tight pants",
            "Medium pants",
            "Loose pants",
            "Left pocket",
            "Right pocket",
            "Indoors",
            "Outdoors",
        )

        answers.forEach { answer ->
            assertNotNull(RecordingQuestionIcons.iconFor(answer), "no illustration for '$answer'")
        }
    }

    /** "Pants" has no drawing of its own; legacy iOS reuses the medium-tightness one. */
    @Test
    fun `Pants reuses the medium tightness illustration`() {
        assertEquals(
            RecordingQuestionIcons.iconFor("Medium pants"),
            RecordingQuestionIcons.iconFor("Pants"),
        )
    }

    /** Distinct answers must not collide, or a clinician cannot tell two options apart. */
    @Test
    fun `pocket sides and tightness levels are each drawn differently`() {
        val distinct = listOf("Left pocket", "Right pocket").map { RecordingQuestionIcons.iconFor(it) }
        assertEquals(2, distinct.toSet().size)

        val tightness = listOf("Tight pants", "Medium pants", "Loose pants")
            .map { RecordingQuestionIcons.iconFor(it) }
        assertEquals(3, tightness.toSet().size)
    }

    /**
     * Unknown answers fall through to null, not to a placeholder: most custom questions a
     * workspace configures have no artwork and must keep rendering as plain text rows.
     */
    @Test
    fun `an answer with no artwork resolves to null`() {
        assertNull(RecordingQuestionIcons.iconFor("Apos shoes"))
        assertNull(RecordingQuestionIcons.iconFor(""))
    }

    /** The tag value is matched verbatim — it is a backend enum value, not free copy. */
    @Test
    fun `matching is case and whitespace sensitive`() {
        assertNull(RecordingQuestionIcons.iconFor("left pocket"))
        assertNull(RecordingQuestionIcons.iconFor("Left Pocket"))
        assertNull(RecordingQuestionIcons.iconFor(" Left pocket"))
    }
}
