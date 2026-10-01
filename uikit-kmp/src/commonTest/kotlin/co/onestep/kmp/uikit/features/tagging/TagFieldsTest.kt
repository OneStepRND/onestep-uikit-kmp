package co.onestep.kmp.uikit.features.tagging

import co.onestep.kmp.uikit.bridge.PermissionStatus
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingConfiguration
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingQuestionData
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTTagField
import co.onestep.kmp.uikit.features.recordFlow.configurations.fitting
import co.onestep.kmp.uikit.features.recordFlow.configurations.renderable
import co.onestep.kmp.uikit.features.recordFlow.configurations.tagFieldsFor
import co.onestep.kmp.uikit.features.recordFlow.destinations.CustomTagsDestination
import co.onestep.kmp.uikit.features.recordFlow.destinations.PreAssistiveDeviceDestination
import co.onestep.kmp.uikit.features.recordFlow.destinations.PreFootwearDestination
import co.onestep.kmp.uikit.features.recordFlow.screens.buildPreRecordDestinations
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.staticBalance.ConditionSetupDestination
import co.onestep.kmp.uikit.models.OSTTagValue
import co.onestep.kmp.uikit.models.codes
import co.onestep.kmp.uikit.models.submittable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The tag-catalog path (OS-17546): which fields are shown, how answers become a `tag_map`, and how
 * the catalog replaces the legacy pre-recording screens. Mirrors the Android SDK's `TagFieldsTest`
 * (OS-17545) so both platforms submit byte-identical codes and keys.
 */
class TagFieldsTest {

    private fun field(
        name: String = "\$footwear",
        type: String = OSTTagField.TYPE_SELECT,
        stage: String = OSTTagField.STAGE_PRE_RECORD,
        required: Boolean = false,
        options: List<OSTTagField.Option> = listOf(OSTTagField.Option("Shoes", "shoes")),
    ) = OSTTagField(name = name, label = name, type = type, stage = stage, required = required, options = options)

    private fun walkWith(fields: List<OSTTagField>?) = OSTRecordingConfiguration.defaultWalk().copy(tagFields = fields)

    // --- decoding --------------------------------------------------------------------------------

    @Test
    fun aMalformedFieldDecodesInsteadOfFailingTheCatalog() {
        // A new stage, an unknown type and a field missing keys must not fail their neighbours:
        // they decode, and are then simply not shown.
        val fields = Json { ignoreUnknownKeys = true }.decodeFromString<List<OSTTagField>>(
            """
            [
              {"type":"select","name":"${'$'}footwear","label":"Footwear","stage":"pre_record",
               "valueType":"code","apps":["clinician"],"locked":false,"activities":null,
               "options":[{"label":"Shoes","value":"shoes","requiresAny":null}]},
              {"type":"slider","name":"${'$'}new","label":"New","stage":"pre_record","options":[]},
              {"name":"${'$'}later","stage":"during_record"},
              {}
            ]
            """.trimIndent(),
        )

        assertEquals(4, fields.size)
        assertEquals(listOf("\$footwear"), fields.renderable(OSTTagField.STAGE_PRE_RECORD).map { it.name })
    }

    @Test
    fun theRealCatalogDecodesForEveryActivityAndLocale() {
        val catalogs = TagCatalogFixture.catalogs
        assertTrue(catalogs.isNotEmpty())
        assertTrue(TagCatalogFixture.fields("walk").isNotEmpty())
        catalogs.forEach { (key, fields) ->
            // Some activities (e.g. Generic Recording) are served no fields at all. Every field the live catalog serves today is a select or a checkbox.
            assertTrue(fields.all { it.type == OSTTagField.TYPE_SELECT || it.type == OSTTagField.TYPE_CHECKBOX }, key)
        }
    }

    // --- what is shown ---------------------------------------------------------------------------

    @Test
    fun onlyRenderableFieldsOfTheStageAreShownInCatalogOrder() {
        val fields = listOf(
            field(name = "\$a"),
            field(name = "\$post", stage = OSTTagField.STAGE_POST_RECORD),
            field(name = "\$b", type = OSTTagField.TYPE_CHECKBOX),
            field(name = "\$text", type = "text"),
            field(name = "\$unknownStage", stage = "during_record"),
            field(name = ""),
            field(name = "\$noCodes", options = listOf(OSTTagField.Option("Blank", " "))),
            field(name = "\$noOptions", options = emptyList()),
        )

        assertEquals(listOf("\$a", "\$b"), fields.renderable(OSTTagField.STAGE_PRE_RECORD).map { it.name })
        assertEquals(listOf("\$post"), fields.renderable(OSTTagField.STAGE_POST_RECORD).map { it.name })
    }

    @Test
    fun anOptionWithNoCodeIsDroppedAndItsFieldKept() {
        val shown = listOf(
            field(options = listOf(OSTTagField.Option("Blank", ""), OSTTagField.Option("Shoes", "shoes"))),
        ).renderable(OSTTagField.STAGE_PRE_RECORD)

        assertEquals(listOf("shoes"), shown.single().options.map { it.value })
    }

    @Test
    fun noCatalogMeansTheLegacyPathAndAnEmptyOneMeansAskNothing() {
        assertNull(walkWith(null).tagFieldsFor(OSTTagField.STAGE_PRE_RECORD))
        assertEquals(emptyList(), walkWith(emptyList()).tagFieldsFor(OSTTagField.STAGE_PRE_RECORD))
    }

    // --- requiresAny -----------------------------------------------------------------------------

    private val outcomes = field(
        name = "\$outcomes",
        type = OSTTagField.TYPE_CHECKBOX,
        stage = OSTTagField.STAGE_POST_RECORD,
        options = listOf(
            OSTTagField.Option("Always", "always", requiresAny = null),
            OSTTagField.Option("Also always", "empty", requiresAny = emptyList()),
            OSTTagField.Option("Opened eyes", "opened_eyes", requiresAny = listOf("eyes_closed")),
        ),
    )

    @Test
    fun anOptionIsOfferedWhenItsRequiresAnyWasAnswered() {
        val fitting = listOf(outcomes).fitting(setOf("eyes_closed")).single()
        assertEquals(listOf("always", "empty", "opened_eyes"), fitting.options.map { it.value })
    }

    @Test
    fun anOptionIsHiddenWhenNoneOfItsRequiresAnyWasAnswered() {
        val fitting = listOf(outcomes).fitting(setOf("eyes_open")).single()
        assertEquals(listOf("always", "empty"), fitting.options.map { it.value })
    }

    @Test
    fun unknownAnswersNarrowNothing() {
        // Null = the summary was opened some other way: offering an option that may not apply is
        // recoverable, hiding one that does is not.
        val config = walkWith(listOf(outcomes))
        val shown = config.tagFieldsFor(OSTTagField.STAGE_POST_RECORD, answeredCodes = null).orEmpty().single()
        assertEquals(3, shown.options.size)
    }

    @Test
    fun aFieldLeftWithNoFittingOptionIsNotShown() {
        val onlyConditional = field(
            name = "\$x",
            stage = OSTTagField.STAGE_POST_RECORD,
            options = listOf(OSTTagField.Option("Opened eyes", "opened_eyes", requiresAny = listOf("eyes_closed"))),
        )
        assertEquals(emptyList(), walkWith(listOf(onlyConditional)).tagFieldsFor(OSTTagField.STAGE_POST_RECORD, emptySet()))
    }

    // --- screen mapping --------------------------------------------------------------------------

    @Test
    fun aCheckboxFieldIsMultiSelectAndRequiredGatesContinue() {
        val sections = TagFields(
            listOf(field(name = "\$a", required = true), field(name = "\$b", type = OSTTagField.TYPE_CHECKBOX)),
        ).toSelectableSections().sections

        assertEquals(listOf("\$a", "\$b"), sections.map { it.id })
        assertEquals(listOf(false, true), sections.map { it.allowsMultiSelect })
        assertEquals(listOf(true, false), sections.map { it.required })
        // Catalog options are text only.
        assertTrue(sections.flatMap { it.options }.all { it.icon == null })
    }

    @Test
    fun selectionsBecomeCodesShapedByFieldType() {
        val fields = TagFields(
            listOf(
                field(name = "\$footwear", options = listOf(OSTTagField.Option("Shoes", "shoes"), OSTTagField.Option("Barefoot", "barefoot"))),
                field(
                    name = "\$environment",
                    type = OSTTagField.TYPE_CHECKBOX,
                    options = listOf(
                        OSTTagField.Option("Indoor", "indoor"),
                        OSTTagField.Option("Stairs", "stairs"),
                        OSTTagField.Option("Outdoor", "outdoor"),
                    ),
                ),
                field(name = "\$unanswered"),
            ),
        )

        val tagMap = fields.toTagMap(mapOf("\$footwear" to listOf(1), "\$environment" to listOf(0, 2)))

        assertEquals(
            mapOf(
                "\$footwear" to OSTTagValue.Single("barefoot"),
                "\$environment" to OSTTagValue.Multiple(listOf("indoor", "outdoor")),
            ),
            tagMap,
        )
    }

    @Test
    fun useOfHandsCodesStayStrings() {
        // $use_of_hands is a select whose codes are "true"/"false": they travel as strings.
        val useOfHands = TagCatalogFixture.fields("sts").first { it.name == "\$use_of_hands" }
        val tagMap = TagFields(listOf(useOfHands)).toTagMap(mapOf(useOfHands.name to listOf(0)))
        val value = tagMap.getValue(useOfHands.name)
        assertTrue(value is OSTTagValue.Single && value.code in setOf("true", "false"))
    }

    // --- submission ------------------------------------------------------------------------------

    @Test
    fun blankAndEmptyAnswersAreDroppedAndNothingLeftIsNull() {
        assertEquals(
            mapOf("\$a" to OSTTagValue.Single("x"), "\$c" to OSTTagValue.Multiple(listOf("y"))),
            mapOf(
                "\$a" to OSTTagValue.Single("x"),
                "\$b" to OSTTagValue.Single(" "),
                "\$c" to OSTTagValue.Multiple(listOf("", "y")),
                "\$d" to OSTTagValue.Multiple(emptyList()),
            ).submittable(),
        )
        assertNull(mapOf("\$b" to OSTTagValue.Single("")).submittable())
        assertNull(emptyMap<String, OSTTagValue>().submittable())
    }

    @Test
    fun codesCollectsEveryAnsweredCode() {
        val codes = mapOf(
            "\$balance_stance" to OSTTagValue.Single("tandem"),
            "\$footwear_adjustment" to OSTTagValue.Multiple(listOf("a", "b")),
        ).codes()
        assertEquals(setOf("tandem", "a", "b"), codes)
    }

    // --- flow order ------------------------------------------------------------------------------

    private fun destinationsFor(config: OSTRecordingConfiguration) = buildPreRecordDestinations(
        config = config,
        micStatus = { PermissionStatus.GRANTED },
        showSoundInstructions = { false },
    )

    @Suppress("DEPRECATION")
    private val legacyWalk = OSTRecordingConfiguration.defaultWalk().copy(
        showPreRecordingAssistiveDeviceSelection = true,
        showPreRecordingFootwearSelection = true,
        preRecordingQuestions = listOf(OSTRecordingQuestionData(title = "Q", tagsValues = listOf("a"))),
    )

    @Test
    fun withoutACatalogTheLegacyPreRecordingScreensAreKept() {
        val destinations = destinationsFor(legacyWalk)
        assertTrue(PreAssistiveDeviceDestination in destinations)
        assertTrue(PreFootwearDestination in destinations)
        assertTrue(CustomTagsDestination in destinations)
        assertFalse(PreTagFieldsDestination in destinations)
    }

    @Test
    fun aCatalogReplacesTheLegacyScreensWithOneScreen() {
        val destinations = destinationsFor(legacyWalk.copy(tagFields = TagCatalogFixture.fields("walk")))
        assertEquals(1, destinations.count { it == PreTagFieldsDestination })
        assertFalse(PreAssistiveDeviceDestination in destinations)
        assertFalse(PreFootwearDestination in destinations)
        assertFalse(CustomTagsDestination in destinations)
    }

    @Test
    fun aCatalogWithNothingToAskBeforeAddsNoScreen() {
        val onlyPost = listOf(field(stage = OSTTagField.STAGE_POST_RECORD))
        val destinations = destinationsFor(legacyWalk.copy(tagFields = onlyPost))
        assertFalse(PreTagFieldsDestination in destinations)
        assertFalse(CustomTagsDestination in destinations)
    }

    @Test
    fun staticBalanceAsksItsPreRecordingFieldsOnConditionSetupNotAScreenOfTheirOwn() {
        val config = OSTRecordingConfiguration.staticBalance().copy(
            tagFields = TagCatalogFixture.fields("static_balance_test"),
        )
        val destinations = destinationsFor(config)
        assertEquals(ConditionSetupDestination, destinations.first())
        assertFalse(PreTagFieldsDestination in destinations)
    }

    @Test
    fun walkAsksTheServedQuestionsBeforeAndAfter() {
        val config = walkWith(TagCatalogFixture.fields("walk"))
        val pre = config.tagFieldsFor(OSTTagField.STAGE_PRE_RECORD).orEmpty().map { it.name }
        val post = config.tagFieldsFor(OSTTagField.STAGE_POST_RECORD, answeredCodes = emptySet()).orEmpty().map { it.name }

        assertTrue("\$footwear" in pre && "\$environment" in pre, "pre: $pre")
        assertTrue("\$level_of_assistance" in post && "\$recording_issues" in post, "post: $post")
        assertTrue(pre.none { it.startsWith("\$balance_") } && post.none { it.startsWith("\$balance_") })
    }
}
