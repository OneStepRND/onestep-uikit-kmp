package co.onestep.kmp.uikit.features.tagging

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import co.onestep.kmp.uikit.features.recordFlow.components.SelectableOption
import co.onestep.kmp.uikit.features.recordFlow.components.SelectableSection
import co.onestep.kmp.uikit.features.recordFlow.components.SelectableSections
import co.onestep.kmp.uikit.features.recordFlow.components.SelectableSectionsScreen
import co.onestep.kmp.uikit.features.recordFlow.components.ToolBarHeight
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTTagField
import co.onestep.kmp.uikit.models.OSTTagValue
import co.onestep.kmp.uikit.testing.OSTTestTags
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.kmp.uikit.utils.UIktDestination
import co.onestep.kmp.uikit.utils.test
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.review_the_following_tags
import co.onestep.kmp.uikit_kmp.generated.resources.your_setup
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/** The tag-catalog questions asked before the recording starts. */
@Serializable
internal data object PreTagFieldsDestination : UIktDestination

/** The tag-catalog questions asked once the recording is analysed, after the native summary. */
@Serializable
internal data object PostTagFieldsDestination : UIktDestination

/** Stable wrapper around the field list (a bare List is unstable for the Compose compiler). */
@Immutable
internal data class TagFields(
    val fields: List<OSTTagField>,
)

/**
 * One section per field, keyed by the field's `name`, with a checkbox field as multi-select.
 * Options are text only unless [iconFor] supplies one — only the Static Balance Condition Setup
 * screen does, for its condition fields.
 */
internal fun TagFields.toSelectableSections(
    iconFor: (fieldName: String, value: String) -> DrawableResource? = { _, _ -> null },
): SelectableSections =
    SelectableSections(
        fields.map { field ->
            SelectableSection(
                id = field.name,
                title = field.label,
                required = field.required,
                allowsMultiSelect = field.type == OSTTagField.TYPE_CHECKBOX,
                options = field.options.map { SelectableOption(it.label, iconFor(field.name, it.value)) },
            )
        },
    )

/**
 * The screen's `section id -> selected indices` result as a `tag_map`: option codes under each
 * field's `name`, one code for a `select` field and a list for a `checkbox`. A field with no
 * selection gets no key. Indices resolve against the field's own options, so no label is ever
 * stored.
 */
internal fun TagFields.toTagMap(selections: Map<String, List<Int>>): Map<String, OSTTagValue> =
    buildMap {
        fields.forEach { field ->
            val codes = selections[field.name]
                .orEmpty()
                .mapNotNull { field.options.getOrNull(it)?.value }
            if (codes.isEmpty()) return@forEach
            put(
                field.name,
                if (field.type == OSTTagField.TYPE_CHECKBOX) {
                    OSTTagValue.Multiple(codes)
                } else {
                    OSTTagValue.Single(codes.first())
                },
            )
        }
    }

internal fun EntryProviderScope<NavKey>.preTagFieldsScreen(
    fields: List<OSTTagField>,
    onContinue: (Map<String, OSTTagValue>) -> Unit,
) {
    entry<PreTagFieldsDestination> {
        TagFieldsScreen(
            title = stringResource(Res.string.your_setup),
            fields = remember(fields) { TagFields(fields) },
            showNote = false,
            modifier = Modifier.test(OSTTestTags.TagCatalog.PRE_RECORD_SCREEN),
            continueButtonTestTag = OSTTestTags.TagCatalog.PRE_RECORD_CONTINUE_BUTTON,
            clearButtonTestTag = OSTTestTags.TagCatalog.PRE_RECORD_CLEAR_ALL_BUTTON,
            onContinue = { tagMap, _ -> onContinue(tagMap) },
        )
    }
}

/**
 * The post-recording questions with the free-text note. [initialNote] and [submitting] are read
 * inside the entry so the screen follows them as they change (NavDisplay caches entry content).
 */
internal fun EntryProviderScope<NavKey>.postTagFieldsScreen(
    fields: List<OSTTagField>,
    initialNote: () -> String?,
    submitting: () -> Boolean,
    onContinue: (tagMap: Map<String, OSTTagValue>, note: String?) -> Unit,
) {
    entry<PostTagFieldsDestination> {
        TagFieldsScreen(
            // The summary flow's toolbar overlays its destinations: reserve its full height.
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = ToolBarHeight.dp)
                .test(OSTTestTags.TagCatalog.POST_RECORD_SCREEN),
            title = stringResource(Res.string.review_the_following_tags),
            fields = remember(fields) { TagFields(fields) },
            // The catalog has no free-text field; the post-recording note stays alongside it, as it
            // was on the tagging screen this replaces.
            showNote = true,
            initialNote = initialNote(),
            submitting = submitting(),
            noteTestTag = OSTTestTags.TagCatalog.POST_RECORD_NOTE_FIELD,
            continueButtonTestTag = OSTTestTags.TagCatalog.POST_RECORD_CONTINUE_BUTTON,
            clearButtonTestTag = OSTTestTags.TagCatalog.POST_RECORD_CLEAR_ALL_BUTTON,
            onContinue = onContinue,
        )
    }
}

/**
 * Renders tag-catalog [fields] on the domain-agnostic [SelectableSectionsScreen] and hands the
 * answers out as a `tag_map`. `required` is the Continue gate; labels are the server's, verbatim.
 */
@Composable
internal fun TagFieldsScreen(
    title: String,
    fields: TagFields,
    showNote: Boolean,
    onContinue: (tagMap: Map<String, OSTTagValue>, note: String?) -> Unit,
    modifier: Modifier = Modifier,
    initialNote: String? = null,
    submitting: Boolean = false,
    noteTestTag: String = OSTTestTags.Tagging.NOTE_TEXT_FIELD,
    continueButtonTestTag: String? = null,
    clearButtonTestTag: String? = null,
    onScreenView: () -> Unit = {},
    iconFor: (fieldName: String, value: String) -> DrawableResource? = { _, _ -> null },
) {
    val sections = remember(fields) { fields.toSelectableSections(iconFor) }
    SelectableSectionsScreen(
        title = title,
        sections = sections,
        modifier = modifier,
        onScreenView = onScreenView,
        showNote = showNote,
        initialNote = initialNote,
        submitting = submitting,
        noteTestTag = noteTestTag,
        continueButtonTestTag = continueButtonTestTag,
        clearButtonTestTag = clearButtonTestTag,
        onContinue = { selections, note -> onContinue(fields.toTagMap(selections), note) },
    )
}

private val previewFields =
    TagFields(
        listOf(
            OSTTagField(
                name = "\$footwear",
                label = "Footwear",
                type = OSTTagField.TYPE_SELECT,
                stage = OSTTagField.STAGE_PRE_RECORD,
                options = listOf(
                    OSTTagField.Option("Shoes", "shoes"),
                    OSTTagField.Option("Barefoot", "barefoot"),
                ),
            ),
            OSTTagField(
                name = "\$environment",
                label = "Environment",
                type = OSTTagField.TYPE_CHECKBOX,
                stage = OSTTagField.STAGE_PRE_RECORD,
                required = true,
                options = listOf(
                    OSTTagField.Option("Indoor", "indoor"),
                    OSTTagField.Option("Stairs", "stairs"),
                ),
            ),
        ),
    )

@Preview
@Composable
private fun TagFieldsScreenPreview() {
    PreviewTheme {
        TagFieldsScreen(
            title = "Your setup",
            fields = previewFields,
            showNote = false,
            onContinue = { _, _ -> },
        )
    }
}

@Preview
@Composable
private fun TagFieldsScreenWithNotePreview() {
    PreviewTheme {
        TagFieldsScreen(
            title = "Review the following tags",
            fields = previewFields,
            showNote = true,
            initialNote = "Paused once at the turn",
            onContinue = { _, _ -> },
        )
    }
}
