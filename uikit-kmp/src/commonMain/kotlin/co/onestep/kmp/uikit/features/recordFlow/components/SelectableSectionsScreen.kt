package co.onestep.kmp.uikit.features.recordFlow.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.designsystem.components.OSButtonSize
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.components.PrimaryButton
import co.onestep.designsystem.components.SecondaryButton
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit.testing.OSTTestTags
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.kmp.uikit.utils.test
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.clear_all
import co.onestep.kmp.uikit_kmp.generated.resources.continue_camel_case
import co.onestep.kmp.uikit_kmp.generated.resources.ic_check_circle
import co.onestep.kmp.uikit_kmp.generated.resources.ic_chevron_down
import co.onestep.kmp.uikit_kmp.generated.resources.ic_stance_feet_together
import co.onestep.kmp.uikit_kmp.generated.resources.ic_stance_semi_tandem
import co.onestep.kmp.uikit_kmp.generated.resources.ic_stance_tandem
import co.onestep.kmp.uikit_kmp.generated.resources.ic_vision_eyes_closed
import co.onestep.kmp.uikit_kmp.generated.resources.ic_vision_eyes_open
import co.onestep.kmp.uikit_kmp.generated.resources.list_separator
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/** A selectable option row inside a [SelectableSection]: localized label + leading icon (SVG drawable). */
@Immutable
internal data class SelectableOption(
    val label: String,
    val icon: DrawableResource,
)

/**
 * One collapsible section of [options].
 *
 * @param id Stable, caller-defined key echoed back in the selection map so the
 *   caller can map a selection to its own domain type regardless of section order.
 * @param title Section header label (already localized).
 * @param options Selectable rows, in display order.
 * @param required When true, this section needs at least one selection before Continue enables.
 * @param allowsMultiSelect When true, options toggle independently and the section stays
 *   expanded after a tap. Defaults to false: exactly one option, and picking it advances
 *   to the next unanswered section.
 */
// `options` is a bare List (unstable for the compiler), but the class is never
// mutated after construction, so @Immutable promises deep immutability and keeps
// SelectableSectionCard skippable.
@Immutable
internal data class SelectableSection(
    val id: String,
    val title: String,
    val options: List<SelectableOption>,
    val required: Boolean = true,
    val allowsMultiSelect: Boolean = false,
)

/** Stable wrapper around the section list (a bare List is unstable for the Compose compiler). */
@Immutable
internal data class SelectableSections(
    val sections: List<SelectableSection>,
)

// Selection state is one bitmask per section (bit i set = option i selected, 0 = unanswered),
// held in an IntArray so `rememberSaveable` can save it with no custom Saver and the selections
// survive process death.
//
// shortcut: a bitmask caps a section at 32 options; every catalog in play is an order of
// magnitude smaller. Upgrade path if one is not: a custom Saver over a List<List<Int>>.
private const val MAX_OPTIONS_PER_SECTION = Int.SIZE_BITS

/** The option indices set in [mask], ascending (catalog order), bounded by [optionCount]. */
internal fun selectedIndicesOf(mask: Int, optionCount: Int): List<Int> =
    (0 until minOf(optionCount, MAX_OPTIONS_PER_SECTION)).filter { mask and (1 shl it) != 0 }

/** Single-select: [index] replaces whatever the section held. */
internal fun singleSelectionMask(index: Int): Int = 1 shl index

/** Multi-select: [index] joins or leaves the section's selection, leaving the rest alone. */
internal fun toggleSelectionMask(mask: Int, index: Int): Int = mask xor (1 shl index)

/** The next section other than [fromIndex] with nothing selected; -1 once all are answered. */
internal fun nextUnansweredSection(masks: IntArray, fromIndex: Int): Int =
    masks.indices.firstOrNull { it != fromIndex && masks[it] == 0 } ?: -1

/** Continue gates on every required section holding at least one selection. */
internal fun isContinueEnabled(masks: IntArray, sections: List<SelectableSection>): Boolean =
    sections.indices.all { masks[it] != 0 || !sections[it].required }

/** `sectionId -> selected indices in catalog order`, omitting sections with no selection. */
internal fun selectionsBySectionId(
    masks: IntArray,
    sections: List<SelectableSection>,
): Map<String, List<Int>> = buildMap {
    sections.forEachIndexed { index, section ->
        val indices = selectedIndicesOf(masks[index], section.options.size)
        if (indices.isNotEmpty()) put(section.id, indices)
    }
}

/** Collapsed-header summary: the selected labels in catalog order, joined by [separator]. */
internal fun joinSelectedLabels(
    section: SelectableSection,
    indices: List<Int>,
    separator: String,
): String = indices.mapNotNull { section.options.getOrNull(it)?.label }.joinToString(separator)

/**
 * Generic, domain-agnostic option picker: a vertical list of collapsible [sections], with
 * Clear-all / Continue actions pinned to the bottom.
 *
 * Behaviour mirrors the Figma progressive-disclosure flow: exactly one section is expanded at a
 * time, and picking a value in a single-select section advances to the next unanswered one. A
 * [SelectableSection.allowsMultiSelect] section instead toggles options on and off and stays
 * expanded — nothing auto-advances, the user moves on via the next header or Continue. Continue
 * enables once every [SelectableSection.required] section holds at least one selection. The
 * screen owns all selection state internally and hands the result out via [onContinue] as a
 * `sectionId -> selected option indices` map (catalog order, sections with no selection omitted)
 * plus the note (always null here — the flow's only caller collects its note post-recording).
 *
 * @param continueButtonTestTag Optional test tag applied to the Continue button.
 * @param clearButtonTestTag Optional test tag applied to the Clear-all button.
 */
@Composable
internal fun SelectableSectionsScreen(
    title: String,
    sections: SelectableSections,
    onContinue: (selections: Map<String, List<Int>>, note: String?) -> Unit,
    modifier: Modifier = Modifier,
    onScreenView: () -> Unit = {},
    continueButtonTestTag: String? = null,
    clearButtonTestTag: String? = null,
) {
    // Fires the screen-view event once per entry (including each loop back into
    // the screen, where the caller may bump its own counters).
    LaunchedEffect(Unit) { onScreenView() }

    val items = sections.sections

    // One selection bitmask per section, 0 = unanswered (see selectedIndicesOf). A fresh array is
    // assigned on each change so the structural-equality policy schedules a recomposition. Keyed
    // by section count so it resets when the config changes.
    var selected by rememberSaveable(items.size) {
        mutableStateOf(IntArray(items.size))
    }

    // One section expanded at a time; -1 = none. Defaults to the first section.
    var expandedSection by rememberSaveable(items.size) { mutableStateOf(0) }

    fun advanceFrom(sectionIndex: Int) {
        expandedSection = nextUnansweredSection(selected, sectionIndex)
    }

    val continueEnabled = isContinueEnabled(selected, items)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                // Clears the opaque base of the sticky bar; the last item can still
                // scroll up into the gradient's transparent top and fade out.
                .padding(horizontal = Variables.GapL)
                .padding(bottom = 120.dp),
        ) {
            OSText(
                text = title,
                fontSize = 28.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Bold,
                color = LocalOSColors.current.neutral_p3,
                modifier = Modifier.padding(vertical = Variables.GapL),
            )
            items.forEachIndexed { index, section ->
                val mask = selected[index]
                // Remembered per mask so the card keeps one list instance while the
                // selection is unchanged — a freshly built List would defeat skipping.
                val selectedIndices = remember(mask, section) {
                    selectedIndicesOf(mask, section.options.size)
                }
                SelectableSectionCard(
                    section = section,
                    selectedIndices = selectedIndices,
                    expanded = expandedSection == index,
                    onHeaderClick = {
                        expandedSection = if (expandedSection == index) -1 else index
                    },
                    onSelect = { optionIndex ->
                        selected = selected.copyOf().also {
                            it[index] = if (section.allowsMultiSelect) {
                                toggleSelectionMask(it[index], optionIndex)
                            } else {
                                singleSelectionMask(optionIndex)
                            }
                        }
                        // Multi-select sections stay expanded: the user keeps toggling.
                        if (!section.allowsMultiSelect) advanceFrom(index)
                    },
                )
                if (index < items.lastIndex) Spacer(Modifier.height(Variables.GapM))
            }
        }

        // Sticky action bar: a top-fading gradient (transparent → surface) so scrolled
        // options dissolve into the bar instead of a hard cut. Mirrors the Figma "Sticky"
        // gradient (surface from the bottom 3/4, fading upward).
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.2f to LocalOSColors.current.neutral_m4,
                        1f to LocalOSColors.current.neutral_m4,
                    ),
                )
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(
                    start = Variables.GapL,
                    end = Variables.GapL,
                    top = 56.dp,
                    bottom = Variables.GapXL,
                ),
            horizontalArrangement = Arrangement.spacedBy(Variables.GapM),
        ) {
            SecondaryButton(
                text = stringResource(Res.string.clear_all),
                onClick = {
                    selected = IntArray(items.size)
                    expandedSection = 0
                },
                size = OSButtonSize.Big,
                modifier = Modifier
                    .weight(1f)
                    .let { if (clearButtonTestTag != null) it.test(clearButtonTestTag) else it },
            )
            PrimaryButton(
                text = stringResource(Res.string.continue_camel_case),
                onClick = { onContinue(selectionsBySectionId(selected, items), null) },
                enabled = continueEnabled,
                size = OSButtonSize.Big,
                modifier = Modifier
                    .weight(1f)
                    .let { if (continueButtonTestTag != null) it.test(continueButtonTestTag) else it },
            )
        }
    }
}

/**
 * One section card: a header that summarises the current selection when collapsed, and
 * the option rows when [expanded]. [selectedIndices] is in catalog order and empty when
 * the section is unanswered; a multi-select section marks each selected row with a
 * trailing check, a single-select one keeps the fill alone.
 */
@Composable
private fun SelectableSectionCard(
    section: SelectableSection,
    selectedIndices: List<Int>,
    expanded: Boolean,
    onHeaderClick: () -> Unit,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalOSColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, colors.neutral_m1, RoundedCornerShape(12.dp))
            .background(colors.neutral_m4, RoundedCornerShape(12.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onHeaderClick)
                .padding(Variables.GapL)
                // Static Balance is this component's only caller; its tag helpers keep the
                // ids consistent with the rest of that flow.
                .test(OSTTestTags.StaticBalance.conditionSection(section.id)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OSText(
                text = section.title,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
                color = colors.neutral_p3,
            )

            // A weighted slot that always occupies the space between the title and the chevron,
            // pinning the chevron to the end whether or not a value is selected. When one is, the
            // summary fades in inside this slot; its label ellipsizes so the icon and chevron are
            // never pushed off-screen at large font scales.
            // Holds the last non-empty selection so the summary can fade *out* with its labels
            // intact after the selection clears.
            var lastSelection by remember(section) { mutableStateOf(selectedIndices) }
            if (selectedIndices.isNotEmpty()) lastSelection = selectedIndices
            val separator = stringResource(Res.string.list_separator)
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AnimatedVisibility(
                    visible = selectedIndices.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Crossfades one summary into the next ("Firm" → "Firm, Uneven") and
                        // animates the width change with the default SizeTransform, so toggling
                        // an option never snaps the header.
                        AnimatedContent(
                            targetState = joinSelectedLabels(section, lastSelection, separator),
                            label = "sectionSummary",
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .padding(start = Variables.GapS),
                        ) { summary ->
                            OSText(
                                text = summary,
                                fontSize = 14.sp,
                                color = colors.neutral_p2,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        // Beyond one selection a single option icon would misrepresent the set,
                        // so it is dropped and the labels take the whole slot.
                        val soleOption = lastSelection.singleOrNull()
                            ?.let { section.options.getOrNull(it) }
                        // Retained like lastSelection above so the icon still has something to
                        // draw while it fades and shrinks away.
                        var lastSoleOption by remember(section) { mutableStateOf(soleOption) }
                        if (soleOption != null) lastSoleOption = soleOption
                        AnimatedVisibility(visible = soleOption != null) {
                            lastSoleOption?.let {
                                Image(
                                    painter = painterResource(it.icon),
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                )
                            }
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                }
            }

            val chevronRotation by animateFloatAsState(
                targetValue = if (expanded) 180f else 0f,
                label = "chevronRotation",
            )
            Icon(
                painter = painterResource(Res.drawable.ic_chevron_down),
                contentDescription = null,
                tint = colors.neutral_p2,
                modifier = Modifier.rotate(chevronRotation),
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(bottom = Variables.GapM)) {
                section.options.forEachIndexed { index, option ->
                    val isSelected = index in selectedIndices
                    // Kept as State, not read via `by`: drawBehind reads it in the draw phase, so
                    // the fill animates without recomposing the row each frame.
                    val rowFill = animateColorAsState(
                        targetValue = if (isSelected) colors.primary_m3 else colors.neutral_m4,
                        label = "rowFill",
                    )
                    val labelColor by animateColorAsState(
                        targetValue = if (isSelected) colors.primary_p1 else colors.neutral_p3,
                        label = "labelColor",
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .test(OSTTestTags.StaticBalance.conditionOption(section.id, index))
                            .clickable { onSelect(index) }
                            .drawBehind { drawRect(rowFill.value) }
                            // The fill alone carries the selected state visually; state it for
                            // screen readers too, in both modes.
                            .semantics { selected = isSelected }
                            .padding(horizontal = Variables.GapL, vertical = Variables.GapM),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = painterResource(option.icon),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                        )
                        Spacer(Modifier.width(Variables.GapM))
                        OSText(
                            text = option.label,
                            fontSize = 20.sp,
                            // Weight is a discrete step — the colour crossfade carries the
                            // transition, and an interpolated weight would only snap to the
                            // nearest face this font family ships anyway.
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = labelColor,
                            // Weighted so a long label yields the trailing check its width
                            // instead of squeezing it off the row.
                            modifier = Modifier.weight(1f),
                        )
                        // A multi-select row needs to read as on/off rather than "the one
                        // chosen"; the trailing check does that. Single-select keeps fill only.
                        if (section.allowsMultiSelect) {
                            SelectedCheck(selected = isSelected, tint = colors.primary_p1)
                        }
                    }
                }
            }
        }
    }
}

/**
 * The trailing check on a selected multi-select row. The 20dp slot is held whether or not the
 * row is [selected], so a check appearing or leaving never reflows the label beside it; only
 * the check itself fades and scales.
 */
@Composable
private fun SelectedCheck(
    selected: Boolean,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Box(modifier.size(20.dp), contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visible = selected,
            // A snappy, slightly overshooting pop: the check should land under the finger
            // rather than ease in, so it runs on a stiffer spring than the default
            // StiffnessMediumLow, and scale carries the bounce while alpha stays critically
            // damped. Leaving keeps the gentler default — a check going away needn't grab
            // the eye the way one arriving does.
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) +
                scaleIn(
                    initialScale = 0.6f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                ),
            exit = fadeOut() + scaleOut(targetScale = 0.6f),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_check_circle),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Preview
@Composable
private fun SelectableSectionsScreenPreview() {
    PreviewTheme {
        SelectableSectionsScreen(
            title = "Choose conditions",
            sections = SelectableSections(
                listOf(
                    SelectableSection(
                        id = "stance",
                        title = "Stance",
                        options = listOf(
                            SelectableOption("Feet together", Res.drawable.ic_stance_feet_together),
                            SelectableOption("Semi-tandem", Res.drawable.ic_stance_semi_tandem),
                            SelectableOption("Tandem", Res.drawable.ic_stance_tandem),
                        ),
                    ),
                    SelectableSection(
                        id = "vision",
                        title = "Vision",
                        options = listOf(
                            SelectableOption("Eyes open", Res.drawable.ic_vision_eyes_open),
                            SelectableOption("Eyes closed", Res.drawable.ic_vision_eyes_closed),
                        ),
                    ),
                ),
            ),
            onContinue = { _, _ -> },
        )
    }
}

@Preview
@Composable
private fun SelectableSectionCardPreview() {
    PreviewTheme {
        SelectableSectionCard(
            section = SelectableSection(
                id = "vision",
                title = "Vision",
                options = listOf(
                    SelectableOption("Eyes open", Res.drawable.ic_vision_eyes_open),
                    SelectableOption("Eyes closed", Res.drawable.ic_vision_eyes_closed),
                ),
            ),
            selectedIndices = listOf(1),
            expanded = false,
            onHeaderClick = {},
            onSelect = {},
        )
    }
}

@Preview
@Composable
private fun SelectableSectionCardMultiSelectPreview() {
    PreviewTheme {
        SelectableSectionCard(
            section = SelectableSection(
                id = "surface",
                title = "Surface",
                allowsMultiSelect = true,
                options = listOf(
                    SelectableOption("Firm", Res.drawable.ic_stance_feet_together),
                    SelectableOption("Foam", Res.drawable.ic_stance_semi_tandem),
                    SelectableOption("Uneven", Res.drawable.ic_stance_tandem),
                ),
            ),
            selectedIndices = listOf(0, 2),
            expanded = true,
            onHeaderClick = {},
            onSelect = {},
        )
    }
}
