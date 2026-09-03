package co.onestep.kmp.uikit.features.recordFlow.components

import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.ic_stance_tandem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The selection core behind [SelectableSectionsScreen] (OS-17191): one bitmask per
 * section, single-select replacing and multi-select toggling, plus the Continue gate
 * and the collapsed-header summary. Kept as pure functions so both cardinalities are
 * covered off-device.
 */
class SelectableSectionsSelectionTest {

    private fun section(
        id: String,
        vararg labels: String,
        required: Boolean = true,
        allowsMultiSelect: Boolean = false,
    ) = SelectableSection(
        id = id,
        title = id,
        // The icon is irrelevant to selection maths; any drawable keeps the fixture honest.
        options = labels.map { SelectableOption(it, Res.drawable.ic_stance_tandem) },
        required = required,
        allowsMultiSelect = allowsMultiSelect,
    )

    private val surface = section("surface", "Firm", "Foam", "Uneven", allowsMultiSelect = true)
    private val stance = section("stance", "Feet together", "Tandem")

    @Test
    fun selectedIndicesOfReportsCatalogOrderAndNothingForAnUnansweredSection() {
        assertEquals(emptyList(), selectedIndicesOf(mask = 0, optionCount = 3))
        assertEquals(listOf(1), selectedIndicesOf(mask = 0b010, optionCount = 3))
        assertEquals(listOf(0, 1, 2), selectedIndicesOf(mask = 0b111, optionCount = 3))
    }

    @Test
    fun selectedIndicesOfIgnoresBitsPastTheCatalog() {
        // A stale bitmask from a longer catalog must not index out of the option list.
        assertEquals(listOf(0), selectedIndicesOf(mask = 0b1001, optionCount = 2))
    }

    @Test
    fun selectedIndicesOfReadsTheTopBit() {
        assertEquals(listOf(31), selectedIndicesOf(mask = 1 shl 31, optionCount = 32))
    }

    @Test
    fun singleSelectReplacesThePreviousChoice() {
        val afterFirst = singleSelectionMask(0)
        val afterSecond = singleSelectionMask(2)
        assertEquals(listOf(0), selectedIndicesOf(afterFirst, 3))
        assertEquals(listOf(2), selectedIndicesOf(afterSecond, 3))
    }

    @Test
    fun multiSelectAccumulatesAndTogglesOffLeavingTheOtherOptionsAlone() {
        var mask = 0
        mask = toggleSelectionMask(mask, 0)
        mask = toggleSelectionMask(mask, 2)
        assertEquals(listOf(0, 2), selectedIndicesOf(mask, 3))

        mask = toggleSelectionMask(mask, 0)
        assertEquals(listOf(2), selectedIndicesOf(mask, 3))

        mask = toggleSelectionMask(mask, 2)
        assertEquals(emptyList(), selectedIndicesOf(mask, 3))
    }

    @Test
    fun nextUnansweredSectionSkipsTheSectionJustAnswered() {
        // First section answered; the two after it are still empty.
        val masks = intArrayOf(0b1, 0, 0)
        assertEquals(1, nextUnansweredSection(masks, fromIndex = 0))
    }

    @Test
    fun nextUnansweredSectionReturnsMinusOneOnceEverySectionHoldsASelection() {
        assertEquals(-1, nextUnansweredSection(intArrayOf(0b1, 0b101), fromIndex = 1))
    }

    @Test
    fun continueStaysDisabledWhileARequiredMultiSelectSectionIsEmpty() {
        val sections = listOf(stance, surface)
        assertFalse(isContinueEnabled(intArrayOf(singleSelectionMask(0), 0), sections))
    }

    @Test
    fun continueEnablesOnASingleSelectionInARequiredMultiSelectSection() {
        val sections = listOf(stance, surface)
        assertTrue(
            isContinueEnabled(
                intArrayOf(singleSelectionMask(0), toggleSelectionMask(0, 1)),
                sections,
            ),
        )
    }

    @Test
    fun continueIgnoresAnEmptyOptionalMultiSelectSection() {
        val optional =
            section("footwear", "Shoes", "Barefoot", required = false, allowsMultiSelect = true)
        assertTrue(
            isContinueEnabled(intArrayOf(singleSelectionMask(0), 0), listOf(stance, optional)),
        )
    }

    @Test
    fun clearAllEmptiesEverySectionAndReGatesContinue() {
        val sections = listOf(stance, surface)
        val cleared = IntArray(sections.size)
        assertFalse(isContinueEnabled(cleared, sections))
        assertEquals(emptyMap(), selectionsBySectionId(cleared, sections))
    }

    @Test
    fun selectionsBySectionIdEmitsCatalogOrderAndOmitsUnansweredSections() {
        val sections = listOf(stance, surface)
        var surfaceMask = 0
        // Tapped out of catalog order: Uneven first, then Firm.
        surfaceMask = toggleSelectionMask(surfaceMask, 2)
        surfaceMask = toggleSelectionMask(surfaceMask, 0)

        assertEquals(
            mapOf("surface" to listOf(0, 2)),
            selectionsBySectionId(intArrayOf(0, surfaceMask), sections),
        )
    }

    @Test
    fun joinSelectedLabelsRendersZeroOneAndManySelections() {
        assertEquals("", joinSelectedLabels(surface, emptyList(), ", "))
        assertEquals("Firm", joinSelectedLabels(surface, listOf(0), ", "))
        assertEquals("Firm, Foam, Uneven", joinSelectedLabels(surface, listOf(0, 1, 2), ", "))
    }
}
