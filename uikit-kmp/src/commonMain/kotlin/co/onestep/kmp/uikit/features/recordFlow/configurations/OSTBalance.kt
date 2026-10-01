package co.onestep.kmp.uikit.features.recordFlow.configurations

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * Static Balance Test configuration (OS-15960), delivered via
 * [OSTRecordingConfiguration.balance].
 *
 * The condition schema is **fully dynamic and server-driven**: the host app maps its
 * server payload into this typed model and passes it in. The SDK renders whatever
 * [categories] it is given, in order, so new server categories/options require no SDK
 * change. Labels ([Category.displayName], [Option.displayName]) are used verbatim from
 * the server; per-option icons are resolved by [Option.code] via [BalanceIcons].
 *
 * The server wire shape this mirrors:
 * ```
 * categories:   [ { key, displayName, options: [ { code, displayName } ] } ]
 * resultStates: [ { code, displayName, requiresAny: [code...] } ]
 * ```
 *
 * Note: at least one [Category] must be [Category.required] with a non-empty option
 * list, otherwise the Condition Setup screen can never enable Continue. When [categories]
 * is left at its default, the full known option set is offered (see [defaultCategories]).
 *
 * @param categories Ordered condition categories shown on the Condition Setup screen.
 * @param resultStates Per-recording outcome states (with their [ResultState.requiresAny]
 *        applicability rules), offered as multi-select chips on the "Recording saved" screen
 *        when no tag catalog drives the recording (see [resultStatesFor]). Empty (the default)
 *        hides the question.
 * @param notesKey Metadata key under which the Condition Setup free-text note is attached
 *        inside the nested `onestep_balance_conditions` object. This is itself one of the
 *        server-driven `balance_conditions` (the `"note"` condition): like the category
 *        [Category.key]s it is used verbatim and MUST match the iOS SDK
 *        exactly. Defaults to [DEFAULT_NOTES_KEY] when the server supplies none.
 * @param resultStatesKey Top-level custom-metadata key under which the outcomes chosen on the
 *        "Recording saved" screen are attached, next to `onestep_balance_conditions`, as a list of
 *        codes (see [resultStatesMetadata]). Defaults to [DEFAULT_RESULT_STATES_KEY], the key the
 *        backend reserves and reads for its pass/fail analysis; leave it at the default unless the
 *        backend changes it.
 */
@Immutable
@Serializable
data class OSTBalance(
    val categories: List<Category> = defaultCategories(),
    val resultStates: List<ResultState> = emptyList(),
    val notesKey: String = DEFAULT_NOTES_KEY,
    val resultStatesKey: String = DEFAULT_RESULT_STATES_KEY,
) {

    /**
     * One condition category, e.g. Stance or Vision.
     *
     * @param key Stable, locale-independent category key. Drives the perception
     *        metadata key (`<key>`) and the analytics property name; MUST match the
     *        iOS SDK exactly for shared categories (stance, vision, surface, footwear).
     * @param displayName Section title, used verbatim (server-provided / host-localized).
     * @param options Selectable options, in display order.
     * @param required When true, a selection is needed before Continue enables. The
     *        server payload carries no `required` flag, so the host applies the
     *        convention (stance/vision/surface required, footwear optional).
     */
    @Immutable
    @Serializable
    data class Category(
        val key: String,
        val displayName: String,
        val options: List<Option>,
        val required: Boolean = true,
    )

    /**
     * One selectable option within a [Category].
     *
     * @param code Canonical, locale-independent contract value (e.g. `feet_together`).
     *        Attached to the perception as the metadata value, used as the analytics
     *        value, and resolves the option icon. MUST match the iOS SDK exactly.
     * @param displayName Option label, used verbatim (server-provided / host-localized).
     */
    @Immutable
    @Serializable
    data class Option(
        val code: String,
        val displayName: String,
    )

    /**
     * A per-recording outcome the clinician can record after a condition (e.g. "Fell",
     * "Grabbed support"), offered on the "Recording saved" screen.
     *
     * @param code Canonical, locale-independent outcome value.
     * @param displayName Outcome label, used verbatim.
     * @param requiresAny If non-empty, the outcome is offered only when the chosen
     *        condition includes at least one of these option codes. Empty = always offered.
     */
    @Immutable
    @Serializable
    data class ResultState(
        val code: String,
        val displayName: String,
        val requiresAny: List<String> = emptyList(),
    )

    companion object {
        /**
         * Fallback key for the Condition Setup free-text note — the `"note"`
         * `balance_conditions` entry — when the server supplies no [notesKey].
         * Part of the cross-platform contract iOS must mirror.
         */
        const val DEFAULT_NOTES_KEY = "note"

        /**
         * Metadata key for the outcomes chosen on the "Recording saved" screen: the one the
         * backend reserves and parses (as a list of codes) for its pass/fail analysis. Matches
         * the Android SDK (OS-17571); part of the cross-platform contract iOS must mirror.
         */
        const val DEFAULT_RESULT_STATES_KEY = "onestep_balance_result_states"

        /**
         * The full known option set, used when no server config is supplied (SDK
         * defaults, previews, tests) — including when a host passes a tag catalog with no
         * Static Balance condition field, so it must record codes the engine accepts.
         *
         * The codes are exactly the engine's `CONDITION_CATALOG`
         * (static_balance_engine/catalog.py), which the tag catalog's locked `$balance_*`
         * fields also serve; `StaticBalanceCatalogTest` pins stance/vision/surface to the
         * served catalog so drift fails CI instead of reaching a recording. They are the
         * cross-platform contract baseline that iOS must mirror; display names are English
         * fallbacks.
         */
        fun defaultCategories(): List<Category> =
            listOf(
                Category(
                    key = "stance",
                    displayName = "Stance",
                    required = true,
                    options = listOf(
                        Option("shoulder_width", "Shoulder width"),
                        Option("feet_together", "Feet together"),
                        Option("semi_tandem", "Semi-tandem"),
                        Option("tandem", "Tandem"),
                        Option("single_leg_left", "Single-leg L"),
                        Option("single_leg_right", "Single-leg R"),
                        Option("seated", "Seated"),
                    ),
                ),
                Category(
                    key = "vision",
                    displayName = "Vision",
                    required = true,
                    options = listOf(
                        Option("eyes_open", "Eyes open"),
                        Option("eyes_closed", "Eyes closed"),
                    ),
                ),
                Category(
                    key = "surface",
                    displayName = "Surface",
                    required = true,
                    options = listOf(
                        Option("firm", "Firm"),
                        Option("foam", "Foam"),
                    ),
                ),
                Category(
                    key = "footwear",
                    displayName = "Footwear",
                    required = false,
                    options = listOf(
                        Option("shoes", "Shoes"),
                        Option("barefoot", "Barefoot"),
                    ),
                ),
            )
    }
}

/**
 * The [OSTBalance.resultStates] that apply to [condition], in server order: a state is offered
 * when its [OSTBalance.ResultState.requiresAny] is empty or names at least one option chosen in
 * [condition]. With no condition only the unconditional states apply. The SDK adds no rule of its
 * own. Mirrors the Android SDK's `resultStatesFor` (OS-17571).
 */
internal fun OSTBalance.resultStatesFor(condition: OSTBalanceCondition?): List<OSTBalance.ResultState> {
    val chosenCodes = condition?.selections?.map { it.code }?.toSet().orEmpty()
    return resultStates.filter { state ->
        state.requiresAny.isEmpty() || state.requiresAny.any { it in chosenCodes }
    }
}

/**
 * The custom-metadata entry for the outcomes chosen on the "Recording saved" screen: the codes of
 * [offered] that are in [chosen], in offered order, as a list under [OSTBalance.resultStatesKey]
 * (e.g. `["fell", "opened_eyes"]` — the backend ignores the key in any other shape). Nothing when
 * none were chosen — no key at all, never an empty list. Mirrors the Android SDK.
 */
internal fun OSTBalance.resultStatesMetadata(
    offered: List<OSTBalance.ResultState>,
    chosen: Collection<String>,
): Map<String, Any> {
    val codes = offered.map { it.code }.filter { it in chosen }
    return if (codes.isEmpty()) emptyMap() else mapOf(resultStatesKey to codes)
}

/** The result state meaning the trial went as planned; choosing it alone scores nothing. */
internal const val BALANCE_RESULT_COMPLETED = "completed"

/**
 * The self-reported Static Balance score for the outcomes chosen on "Recording saved": 0 as soon as
 * any outcome other than [BALANCE_RESULT_COMPLETED] is chosen ("tagging any of these events … sets
 * the score to 0"), or null — no self-report — when none, or only `completed`, was chosen. Mirrors
 * the Android SDK's `balanceSelfReportScore` (OS-17571).
 */
internal fun balanceSelfReportScore(chosen: Collection<String>): Int? =
    if (chosen.any { it != BALANCE_RESULT_COMPLETED }) 0 else null
