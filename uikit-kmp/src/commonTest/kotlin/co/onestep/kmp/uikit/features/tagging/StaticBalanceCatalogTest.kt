package co.onestep.kmp.uikit.features.tagging

import co.onestep.kmp.uikit.features.recordFlow.configurations.BALANCE_CONDITION_CATEGORIES
import co.onestep.kmp.uikit.features.recordFlow.configurations.BALANCE_RESULT_STATES_FIELD
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTBalance
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingConfiguration
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTTagField
import co.onestep.kmp.uikit.features.recordFlow.configurations.balanceConditionOf
import co.onestep.kmp.uikit.features.recordFlow.configurations.catalogConditionSetupFields
import co.onestep.kmp.uikit.features.recordFlow.configurations.staticBalanceOutcomes
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.staticBalance.outcomeIcon
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.staticBalance.outcomesTagMap
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.staticBalance.recapLine
import co.onestep.kmp.uikit.models.OSTTagValue
import co.onestep.kmp.uikit.models.codes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Static Balance on the real tag catalog (OS-17546): the catalog-driven Condition Setup, the
 * condition derived from it, the outcomes offered per condition, and the legacy defaults the
 * fallback records. Mirrors the Android SDK's `StaticBalanceCatalogTest` (OS-17545).
 */
class StaticBalanceCatalogTest {

    private val served = TagCatalogFixture.fields("static_balance_test")

    private val catalogConfig = OSTRecordingConfiguration.staticBalance().copy(tagFields = served)

    private fun answer(vararg pairs: Pair<String, String>): Map<String, OSTTagValue> =
        pairs.associate { (field, code) -> field to OSTTagValue.Single(code) }

    private fun outcomeCodes(vararg conditionCodes: String): List<String> =
        catalogConfig.staticBalanceOutcomes(conditionCodes.toSet())?.options.orEmpty().map { it.value }

    @Test
    fun conditionSetupRendersEveryPreRecordingFieldInCatalogOrder() {
        val fields = assertNotNull(catalogConfig.catalogConditionSetupFields())
        val expected = served.filter { it.stage == OSTTagField.STAGE_PRE_RECORD }.map { it.name }

        assertEquals(expected, fields.map { it.name })
        // The condition fields are served last and are the required ones.
        assertEquals(BALANCE_CONDITION_CATEGORIES.keys.toList(), fields.takeLast(3).map { it.name })
        assertTrue(fields.takeLast(3).all { it.required })
    }

    @Test
    fun aCatalogWithoutAConditionFieldFallsBackToTheLegacyScreen() {
        val noCondition = served.filterNot { it.name in BALANCE_CONDITION_CATEGORIES }
        assertNull(catalogConfig.copy(tagFields = noCondition).catalogConditionSetupFields())
        assertNull(OSTRecordingConfiguration.staticBalance().catalogConditionSetupFields())
        // Not Static Balance at all: never a Condition Setup, even with a catalog that has one.
        assertNull(OSTRecordingConfiguration.defaultWalk().copy(tagFields = served).catalogConditionSetupFields())
    }

    @Test
    fun theConditionIsDerivedInEngineCategoriesWithTheCatalogLabels() {
        val fields = assertNotNull(catalogConfig.catalogConditionSetupFields())
        val condition = balanceConditionOf(
            fields,
            answer(
                "\$footwear" to "shoes",
                "\$balance_stance" to "tandem",
                "\$balance_vision" to "eyes_closed",
                "\$balance_surface" to "foam",
            ),
        )

        assertEquals(mapOf("stance" to "tandem", "vision" to "eyes_closed", "surface" to "foam"), condition.toMetadata())
        val stanceLabel = fields.first { it.name == "\$balance_stance" }.options.first { it.value == "tandem" }.label
        assertEquals(stanceLabel, condition.selections.first().displayName)
    }

    @Test
    fun tandemWithEyesClosedOffersEveryOutcome() {
        val all = served.first { it.name == BALANCE_RESULT_STATES_FIELD }.options.map { it.value }
        assertEquals(8, all.size)
        assertEquals(all, outcomeCodes("tandem", "eyes_closed", "firm"))
    }

    @Test
    fun seatedOffersOnlyTheUnconditionalOutcomes() {
        assertEquals(listOf("completed", "aborted_early"), outcomeCodes("seated", "eyes_open", "firm"))
    }

    @Test
    fun openedEyesNeedsEyesClosedAndTouchedDownNeedsARaisedFoot() {
        val feetTogetherOpen = outcomeCodes("feet_together", "eyes_open", "firm")
        assertTrue("opened_eyes" !in feetTogetherOpen && "touched_down" !in feetTogetherOpen)
        assertTrue("completed" in feetTogetherOpen, "completed is served and must be shown")
        assertTrue("touched_down" in outcomeCodes("single_leg_left", "eyes_open", "firm"))
    }

    @Test
    fun noOutcomesOnTheLegacyConditionSetup() {
        assertNull(OSTRecordingConfiguration.staticBalance().staticBalanceOutcomes(setOf("tandem")))
    }

    @Test
    fun chosenOutcomesGoUpInCatalogOrderAndNothingChosenSendsNoKey() {
        val outcomes = assertNotNull(catalogConfig.staticBalanceOutcomes(setOf("tandem", "eyes_closed")))

        assertEquals(
            mapOf(BALANCE_RESULT_STATES_FIELD to OSTTagValue.Multiple(listOf("stepped_out", "opened_eyes"))),
            outcomesTagMap(outcomes, chosen = listOf("opened_eyes", "stepped_out")),
        )
        assertEquals(emptyMap(), outcomesTagMap(outcomes, chosen = emptyList()))
        assertEquals(emptyMap(), outcomesTagMap(null, chosen = listOf("fell")))
    }

    @Test
    fun onlyTheDesignedOutcomesCarryAnIcon() {
        val withIcon = served.first { it.name == BALANCE_RESULT_STATES_FIELD }.options
            .map { it.value }
            .filter { outcomeIcon(it) != null }
        assertEquals(listOf("stepped_out", "fell", "opened_eyes", "aborted_early"), withIcon)
    }

    @Test
    fun theLegacyDefaultsRecordOnlyCodesTheCatalogServes() {
        // The fallback Condition Setup must record what the engine recognises; the locked
        // $balance_* fields serve exactly the engine's CONDITION_CATALOG.
        val defaults = OSTBalance.defaultCategories().associate { it.key to it.options.map { o -> o.code } }
        BALANCE_CONDITION_CATEGORIES.forEach { (fieldName, category) ->
            val servedCodes = served.first { it.name == fieldName }.options.map { it.value }
            assertEquals(servedCodes.toSet(), defaults.getValue(category).toSet(), category)
        }
    }

    @Test
    fun theConditionCodesCarryIntoTheOutcomeFilter() {
        // What the Condition Setup answers is exactly what narrows the outcomes.
        val answers = answer("\$balance_stance" to "seated", "\$balance_vision" to "eyes_closed")
        assertTrue("opened_eyes" in outcomeCodes(*answers.codes().toTypedArray()))
    }

    @Test
    fun recapLineAppendsTheLength() {
        assertEquals("Feet together | Eyes open | Firm | 10 sec", recapLine("Feet together | Eyes open | Firm", "10 sec"))
        assertEquals("10 sec", recapLine("", "10 sec"))
    }
}
