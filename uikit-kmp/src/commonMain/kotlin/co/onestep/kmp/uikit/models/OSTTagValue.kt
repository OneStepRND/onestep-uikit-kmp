package co.onestep.kmp.uikit.models

import androidx.compose.runtime.Immutable

/**
 * The answer to one tag-catalog field, submitted in a recording's `tag_map` under that field's
 * `name` (e.g. `"$footwear"`). Mirrors the native SDKs' `OSTTagValue` (Android core 2.3.0,
 * OS-17545), which the platform bridges map it to.
 *
 * Values are the option **codes** the catalog serves (`value`), never its display `label`: a label
 * is locale-dependent, so storing it would make the same answer a different string in every
 * language. Codes are submitted verbatim — the backend rejects an unknown code for a locked field
 * such as `$balance_stance`, so they must never be renamed or defaulted on the client.
 */
@Immutable
sealed interface OSTTagValue {
    /** The answer to a `select` field: exactly one option code. Goes on the wire as a string. */
    @Immutable
    data class Single(val code: String) : OSTTagValue

    /** The answer to a `checkbox` field: any number of option codes. Goes on the wire as a list. */
    @Immutable
    data class Multiple(val codes: List<String>) : OSTTagValue
}

/**
 * [this] reduced to what may be submitted, or `null` when nothing is left.
 *
 * An unanswered field must produce no key at all: the backend rejects an empty string, and an
 * empty list would record "answered with nothing" rather than "not answered".
 */
internal fun Map<String, OSTTagValue>.submittable(): Map<String, OSTTagValue>? =
    mapNotNull { (key, value) ->
        when (value) {
            is OSTTagValue.Single -> value.takeIf { it.code.isNotBlank() }
            is OSTTagValue.Multiple ->
                value.codes
                    .filter { it.isNotBlank() }
                    .takeIf { it.isNotEmpty() }
                    ?.let { OSTTagValue.Multiple(it) }
        }?.let { key to it }
    }.toMap().ifEmpty { null }

/** Every option code in [this] tag map, across all its fields. */
internal fun Map<String, OSTTagValue>.codes(): Set<String> =
    values.flatMapTo(mutableSetOf()) { value ->
        when (value) {
            is OSTTagValue.Single -> listOf(value.code)
            is OSTTagValue.Multiple -> value.codes
        }
    }
