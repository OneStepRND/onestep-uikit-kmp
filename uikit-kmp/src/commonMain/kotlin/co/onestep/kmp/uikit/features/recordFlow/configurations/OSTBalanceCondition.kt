package co.onestep.kmp.uikit.features.recordFlow.configurations

import androidx.compose.runtime.Immutable
import co.onestep.kmp.uikit.models.OSTTagValue
import kotlinx.serialization.Serializable

/**
 * A single Static Balance Test condition — the option the clinician picked in each
 * [OSTBalance.Category], in category order.
 *
 * A Static Balance session consists of one or more conditions, each recorded as a
 * separate perception. The clinician may attach a free-text [notes] observation
 * (entered after recording, on the "Recording saved" screen), and (in a future
 * iteration) a per-recording [resultStateCode].
 *
 * Each [Selection.code] is the canonical cross-platform contract value: it is attached
 * to the uploaded perception as `<categoryKey>` metadata (consumed by the Engine)
 * and used as an analytics value, and MUST match the iOS SDK exactly for shared categories.
 *
 * @param selections The chosen option per category, ordered to match the offered categories.
 * @param notes Optional free-text note entered after recording on the "Recording saved"
 *        screen; travels inside the nested `onestep_balance_conditions` object under the
 *        `"note"` tag ([OSTBalance.notesKey]), never via the measurement's own `note` field.
 * @param resultStateCode Optional per-recording outcome ([OSTBalance.ResultState.code]).
 *        Reserved for the (deferred) outcome-capture UI; not populated yet.
 */
@Immutable
@Serializable
data class OSTBalanceCondition(
    val selections: List<Selection>,
    val notes: String? = null,
    val resultStateCode: String? = null,
) {

    /** The option chosen for one category. */
    @Immutable
    @Serializable
    data class Selection(
        val categoryKey: String,
        val code: String,
        val displayName: String,
    )

    /** The chosen option code for [categoryKey], or null if that category was not selected. */
    fun codeFor(categoryKey: String): String? =
        selections.firstOrNull { it.categoryKey == categoryKey }?.code

    /**
     * Human-readable one-line summary, e.g. "Feet together | Eyes open | Firm | Shoes",
     * built from the (server-provided) option labels in category order.
     */
    fun displayLine(): String =
        selections.joinToString(" | ") { it.displayName }

    /**
     * Canonical, locale-independent config string for analytics properties,
     * e.g. "feet_together | eyes_open | firm | shoes".
     */
    fun analyticsValue(): String =
        selections.joinToString(" | ") { it.code }

    /**
     * Serializes the condition as measurement custom-metadata entries (the Engine's
     * per-perception contract): one `<categoryKey>` entry per selection. For the
     * shared categories this yields the `stance`/`vision`/`surface`/`footwear` keys
     * exactly. Keys and values must match the iOS SDK exactly.
     * [notes] is not included here — it is added by [toConditionsMetadata] under the
     * `"note"` tag ([notesKey]).
     */
    fun toMetadata(): Map<String, String> =
        selections.associate { it.categoryKey to it.code }

    /**
     * Builds the nested [KEY_BALANCE_CONDITIONS] object attached to the perception
     * create call: the per-category `<categoryKey>` selections and the clinician note
     * under [notesKey] (only when set). Keys and values must match the iOS SDK exactly.
     *
     * All values are strings: the backend `custom_metadata` schema accepts this nested
     * object only as a `dict[str,str]`.
     *
     * @param notesKey The server-driven key for the free-text note
     *        ([OSTBalance.notesKey]); used verbatim.
     */
    fun toConditionsMetadata(
        notesKey: String = OSTBalance.DEFAULT_NOTES_KEY,
    ): Map<String, String> =
        buildMap {
            putAll(toMetadata())
            notes?.takeIf { it.isNotBlank() }?.let { put(notesKey, it) }
        }

    companion object {
        const val KEY_SESSION_UUID = "session_uuid"

        /** Custom-metadata key holding the nested per-condition object. */
        const val KEY_BALANCE_CONDITIONS = "onestep_balance_conditions"
    }
}

/**
 * The tag-catalog fields that carry a Static Balance condition, mapped to the engine's condition
 * category. Mirrors the backend's `BALANCE_CONDITION_CATEGORIES` (models/perception/tag_catalog.py):
 * the backend projects these `tag_map` keys onto the same `user_metadata.static_balance` object
 * that `onestep_balance_conditions` writes, under these category names.
 */
internal val BALANCE_CONDITION_CATEGORIES: Map<String, String> =
    mapOf(
        "\$balance_stance" to "stance",
        "\$balance_vision" to "vision",
        "\$balance_surface" to "surface",
    )

/** The catalog field holding a Static Balance trial's observed outcomes (a checkbox field). */
internal const val BALANCE_RESULT_STATES_FIELD = "\$balance_result_states"

/** Whether [this] catalog carries a Static Balance condition field at all. */
internal fun List<OSTTagField>.hasBalanceCondition(): Boolean = any { it.name in BALANCE_CONDITION_CATEGORIES }

/**
 * The condition answered on a catalog-driven Condition Setup screen, as an [OSTBalanceCondition].
 *
 * The same codes also travel in `tag_map`; this copy goes to `onestep_balance_conditions` too, so a
 * `tag_map` the backend rejects — and the SDK then resends without — cannot cost the recording its
 * condition. It also drives the "Recording saved" recap line and the analytics, as before.
 * Selections follow catalog order; the label shown is the catalog's own, verbatim.
 */
internal fun balanceConditionOf(
    fields: List<OSTTagField>,
    tagMap: Map<String, OSTTagValue>,
): OSTBalanceCondition =
    OSTBalanceCondition(
        selections = fields.mapNotNull { field ->
            val category = BALANCE_CONDITION_CATEGORIES[field.name] ?: return@mapNotNull null
            val code = (tagMap[field.name] as? OSTTagValue.Single)?.code ?: return@mapNotNull null
            OSTBalanceCondition.Selection(
                categoryKey = category,
                code = code,
                displayName = field.options.firstOrNull { it.value == code }?.label ?: code,
            )
        },
    )
