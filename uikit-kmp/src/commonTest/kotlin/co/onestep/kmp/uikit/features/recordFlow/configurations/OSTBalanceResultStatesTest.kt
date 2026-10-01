package co.onestep.kmp.uikit.features.recordFlow.configurations

import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.staticBalance.legacyOutcomesField
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.staticBalance.outcomesSelfReportScore
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.staticBalance.outcomesTagMap
import co.onestep.kmp.uikit.models.OSTTagValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * OS-17571: the result states offered on "Recording saved", the metadata they send and the score
 * they self-report. Mirrors the Android SDK's `OSTBalanceResultStatesTest`, plus the legacy chips
 * field and the catalog-code score this kit adds.
 */
class OSTBalanceResultStatesTest {

    private val balance =
        OSTBalance(
            resultStates =
                listOf(
                    OSTBalance.ResultState("fell", "Fell"),
                    OSTBalance.ResultState("opened_eyes", "Opened eyes", requiresAny = listOf("eyes_closed")),
                    OSTBalance.ResultState(
                        "touched_down",
                        "Touched down",
                        requiresAny = listOf("single_leg_left", "tandem"),
                    ),
                    OSTBalance.ResultState("aborted_early", "Lost balance"),
                ),
        )

    private fun condition(vararg codes: String) =
        OSTBalanceCondition(
            selections = codes.mapIndexed { i, code -> OSTBalanceCondition.Selection("k$i", code, code) },
        )

    private fun offeredCodes(condition: OSTBalanceCondition?) =
        balance.resultStatesFor(condition).map { it.code }

    @Test
    fun statesWithNoRequiresAnyAreAlwaysOffered() {
        assertEquals(listOf("fell", "aborted_early"), offeredCodes(condition("seated", "eyes_open", "firm")))
    }

    @Test
    fun aStateIsOfferedWhenRequiresAnyNamesAChosenOptionInServerOrder() {
        assertEquals(
            listOf("fell", "opened_eyes", "touched_down", "aborted_early"),
            offeredCodes(condition("tandem", "eyes_closed", "foam")),
        )
    }

    @Test
    fun withoutAConditionOnlyTheUnconditionalStatesAreOffered() {
        assertEquals(listOf("fell", "aborted_early"), offeredCodes(null))
    }

    @Test
    fun noHostResultStatesOffersNothing() {
        assertTrue(OSTBalance().resultStatesFor(condition("tandem", "eyes_closed")).isEmpty())
    }

    @Test
    fun theDefaultKeyIsTheOneTheBackendReads() {
        assertEquals("onestep_balance_result_states", OSTBalance().resultStatesKey)
    }

    @Test
    fun chosenCodesGoAsAListUnderTheReservedKeyInOfferedOrder() {
        val offered = balance.resultStatesFor(condition("tandem", "eyes_closed"))

        assertEquals(
            mapOf("onestep_balance_result_states" to listOf("fell", "opened_eyes")),
            balance.resultStatesMetadata(offered, listOf("opened_eyes", "fell")),
        )
    }

    @Test
    fun nothingChosenSendsNoKeyAtAll() {
        assertTrue(balance.resultStatesMetadata(balance.resultStates, emptyList()).isEmpty())
    }

    @Test
    fun aChosenCodeThatIsNotOfferedIsDropped() {
        val offered = balance.resultStatesFor(condition("seated", "eyes_open"))

        assertTrue(balance.resultStatesMetadata(offered, listOf("opened_eyes")).isEmpty())
    }

    @Test
    fun aServerSuppliedKeyIsUsedVerbatim() {
        val custom = balance.copy(resultStatesKey = "server_result_states")

        assertEquals(
            mapOf("server_result_states" to listOf("fell")),
            custom.resultStatesMetadata(custom.resultStates, listOf("fell")),
        )
    }

    @Test
    fun anyChosenOutcomeOtherThanCompletedSelfReportsAScoreOfZero() {
        assertEquals(0, balanceSelfReportScore(listOf("fell")))
        assertEquals(0, balanceSelfReportScore(listOf("completed", "opened_eyes")))
    }

    @Test
    fun nothingChosenOrOnlyCompletedSelfReportsNoScore() {
        assertNull(balanceSelfReportScore(emptyList()))
        assertNull(balanceSelfReportScore(listOf("completed")))
    }

    @Test
    fun catalogOutcomeCodesScoreTheSameWay() {
        fun outcomes(vararg codes: String) =
            mapOf(BALANCE_RESULT_STATES_FIELD to OSTTagValue.Multiple(codes.toList()))

        assertEquals(0, outcomesSelfReportScore(outcomes("stepped_out")))
        assertEquals(0, outcomesSelfReportScore(outcomes("completed", "fell")))
        assertNull(outcomesSelfReportScore(outcomes("completed")))
        assertNull(outcomesSelfReportScore(emptyMap()))
    }

    @Test
    fun legacyChipsAreTheFittingStatesKeyedByTheResultStatesKey() {
        val field = assertNotNull(balance.legacyOutcomesField(condition("tandem", "eyes_open")))

        assertEquals("onestep_balance_result_states", field.name)
        assertEquals(OSTTagField.TYPE_CHECKBOX, field.type)
        assertEquals(listOf("fell", "touched_down", "aborted_early"), field.options.map { it.value })
        assertEquals("Touched down", field.options[1].label)
    }

    @Test
    fun noFittingLegacyStatesMeansNoChips() {
        assertNull(OSTBalance().legacyOutcomesField(condition("tandem")))
        val onlyConditional = OSTBalance(
            resultStates = listOf(OSTBalance.ResultState("opened_eyes", "Opened eyes", listOf("eyes_closed"))),
        )
        assertNull(onlyConditional.legacyOutcomesField(condition("tandem", "eyes_open")))
    }

    @Test
    fun legacyChipAnswersRoundTripToTheResultStatesMetadata() {
        val offered = balance.resultStatesFor(condition("tandem", "eyes_closed"))
        val field = assertNotNull(balance.legacyOutcomesField(condition("tandem", "eyes_closed")))
        val answers = outcomesTagMap(field, chosen = listOf("touched_down", "fell"))

        assertEquals(
            mapOf("onestep_balance_result_states" to listOf("fell", "touched_down")),
            balance.resultStatesMetadata(offered, answers.values.flatMap { (it as OSTTagValue.Multiple).codes }),
        )
        assertEquals(0, outcomesSelfReportScore(answers))
    }
}
