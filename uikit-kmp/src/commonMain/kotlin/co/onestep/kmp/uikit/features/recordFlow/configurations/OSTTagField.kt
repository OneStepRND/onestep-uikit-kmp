package co.onestep.kmp.uikit.features.recordFlow.configurations

import androidx.compose.runtime.Immutable
import co.onestep.kmp.uikit.models.OSTActivityType
import kotlinx.serialization.Serializable

/**
 * One field of the backend's perception tag catalog: a question asked before or after a recording.
 *
 * The host fetches the catalog (`GET /api/camel/perception/tag_catalog/`) and hands its `fields` to
 * the UIKit through [OSTRecordingConfiguration.tagFields]; the UIKit never calls the endpoint. The
 * class mirrors the endpoint's field shape and is [Serializable], so a host can decode `fields`
 * straight into it with a `Json { ignoreUnknownKeys = true }`. Mirrors the Android SDK's
 * `OSTTagField` (OS-17545).
 *
 * [type] and [stage] are kept as the server's strings rather than enums so that a value this
 * version does not know — a new stage the backend adds — decodes cleanly and is simply not shown,
 * instead of failing the whole catalog. For the same reason every property has a default: a field
 * missing a key decodes, and is then not shown, rather than failing its neighbours. The endpoint's
 * `activities` is not modelled: the host filters the catalog by activity before passing it in.
 *
 * @param name The `tag_map` key the answer is submitted under, e.g. `"$footwear"`.
 * @param label Localized question text, shown verbatim.
 * @param type [TYPE_SELECT] (one answer) or [TYPE_CHECKBOX] (any number); anything else is not shown.
 * @param stage [STAGE_PRE_RECORD] or [STAGE_POST_RECORD]; anything else is not shown.
 * @param required When true, Continue waits for an answer. UI only: the server never rejects a
 *        recording for a missing tag.
 * @param options The answers offered, in display order.
 */
@Immutable
@Serializable
data class OSTTagField(
    val name: String = "",
    val label: String = "",
    val type: String = "",
    val stage: String = "",
    val required: Boolean = false,
    val options: List<Option> = emptyList(),
) {
    /**
     * One answer to an [OSTTagField].
     *
     * @param label Localized display text. Never stored — it differs per language.
     * @param value The option code submitted in `tag_map`, verbatim.
     * @param requiresAny When set, the option only fits a recording whose other answers include at
     *        least one of these codes (e.g. "opened eyes" only after an eyes-closed condition).
     */
    @Immutable
    @Serializable
    data class Option(
        val label: String = "",
        val value: String = "",
        val requiresAny: List<String>? = null,
    )

    companion object {
        const val TYPE_SELECT = "select"
        const val TYPE_CHECKBOX = "checkbox"
        const val STAGE_PRE_RECORD = "pre_record"
        const val STAGE_POST_RECORD = "post_record"
    }
}

private const val MAX_RENDERED_OPTIONS = Int.SIZE_BITS

/**
 * The fields of [stage] this UIKit can render, in catalog order.
 *
 * An unknown [OSTTagField.type] or [OSTTagField.stage] is skipped rather than treated as an error —
 * the backend may add either, and clients must tolerate it. So is a field with no `name`, which has
 * no `tag_map` key to submit under, and an option with no `value`, which has no code to submit. A
 * field left with no options is skipped too: there would be nothing to pick, and a required one
 * would block Continue forever.
 */
internal fun List<OSTTagField>.renderable(stage: String): List<OSTTagField> =
    // shortcut: the option picker keeps one 32-bit selection mask per section, so options past
    // MAX_RENDERED_OPTIONS are not offered (sending a wrong code would be worse). The live catalog's
    // largest field has 8. Upgrade path: a list-based selection Saver in SelectableSectionsScreen.
    map { field -> field.copy(options = field.options.filter { it.value.isNotBlank() }.take(MAX_RENDERED_OPTIONS)) }
        .filter {
            it.name.isNotBlank() &&
                it.stage == stage &&
                (it.type == OSTTagField.TYPE_SELECT || it.type == OSTTagField.TYPE_CHECKBOX) &&
                it.options.isNotEmpty()
        }

/**
 * [this] with each field offering only the options that fit a recording already answered with
 * [answeredCodes]: an option with a [OSTTagField.Option.requiresAny] is kept when any of those codes
 * was answered, and an option without one (or with an empty one) is always kept. Filter before
 * [renderable], so a field left with no fitting option is not shown at all.
 */
internal fun List<OSTTagField>.fitting(answeredCodes: Set<String>): List<OSTTagField> =
    map { field ->
        field.copy(
            options = field.options.filter { option ->
                val requires = option.requiresAny
                requires.isNullOrEmpty() || requires.any { it in answeredCodes }
            },
        )
    }

/**
 * The [OSTRecordingConfiguration.tagFields] of [stage] to render, or null when this recording does
 * not use the tag catalog and the legacy tagging configuration applies instead.
 *
 * [answeredCodes] are the option codes already given for this recording — the pre-recording
 * answers, when rendering the post-recording fields — and narrow each field to the options that fit
 * them (see [fitting]). Null means they are unknown, and nothing is narrowed: offering an option that
 * may not apply is recoverable, while hiding one that does is not.
 *
 * For Static Balance the `pre_record` fields are rendered by its Condition Setup screen rather
 * than a screen of their own — see [catalogConditionSetupFields].
 */
internal fun OSTRecordingConfiguration.tagFieldsFor(
    stage: String,
    answeredCodes: Set<String>? = emptySet(),
): List<OSTTagField>? =
    tagFields
        ?.let { fields -> answeredCodes?.let { fields.fitting(it) } ?: fields }
        ?.renderable(stage)

/**
 * The fields a catalog-driven Static Balance Condition Setup screen renders, or null when this
 * recording keeps the legacy [OSTRecordingConfiguration.balance] screen: not Static Balance, no
 * catalog, or a catalog with no condition field to set up — a recording is never made without a
 * condition.
 */
internal fun OSTRecordingConfiguration.catalogConditionSetupFields(): List<OSTTagField>? =
    tagFieldsFor(OSTTagField.STAGE_PRE_RECORD)
        ?.takeIf { activityType == OSTActivityType.STATIC_BALANCE && it.hasBalanceCondition() }

/**
 * The outcome chips on the Static Balance "Recording saved" screen: every option the catalog serves
 * for `$balance_result_states`, narrowed only by the served `requiresAny` rules to the condition
 * just recorded ([answeredCodes]), or null when this recording uses the legacy Condition Setup or
 * nothing fits. Which outcomes are offered is the server's call; the UIKit adds no rule of its own.
 */
internal fun OSTRecordingConfiguration.staticBalanceOutcomes(answeredCodes: Set<String>): OSTTagField? =
    catalogConditionSetupFields()
        ?.let { tagFieldsFor(OSTTagField.STAGE_POST_RECORD, answeredCodes) }
        ?.firstOrNull { it.name == BALANCE_RESULT_STATES_FIELD }
