package co.onestep.kmp.uikit.features.recordFlow.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.kmp.uikit.utils.test
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.clear_all
import co.onestep.kmp.uikit_kmp.generated.resources.continue_camel_case
import co.onestep.kmp.uikit_kmp.generated.resources.ic_chevron_down
import co.onestep.kmp.uikit_kmp.generated.resources.ic_stance_feet_together
import co.onestep.kmp.uikit_kmp.generated.resources.ic_stance_semi_tandem
import co.onestep.kmp.uikit_kmp.generated.resources.ic_stance_tandem
import co.onestep.kmp.uikit_kmp.generated.resources.ic_vision_eyes_closed
import co.onestep.kmp.uikit_kmp.generated.resources.ic_vision_eyes_open
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
 * One collapsible section of mutually-exclusive [options].
 *
 * @param id Stable, caller-defined key echoed back in the selection map so the
 *   caller can map a selection to its own domain type regardless of section order.
 * @param title Section header label (already localized).
 * @param options Selectable rows, in display order.
 * @param required When true, this section needs a selection before Continue enables.
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
)

/** Stable wrapper around the section list (a bare List is unstable for the Compose compiler). */
@Immutable
internal data class SelectableSections(
    val sections: List<SelectableSection>,
)

/**
 * Generic, domain-agnostic option picker: a vertical list of collapsible
 * single-choice [sections], with Clear-all / Continue actions pinned to the bottom.
 *
 * Behaviour mirrors the Figma progressive-disclosure flow: exactly one section is
 * expanded at a time, and selecting a value advances to the next unanswered
 * section. Continue enables once every [SelectableSection.required] section has a
 * selection. The screen owns all selection state internally and hands the result out
 * via [onContinue] as a `sectionId -> selectedOptionIndex` map plus the trimmed note
 * (null when blank or hidden).
 *
 * @param continueButtonTestTag Optional test tag applied to the Continue button.
 * @param clearButtonTestTag Optional test tag applied to the Clear-all button.
 */
@Composable
internal fun SelectableSectionsScreen(
    title: String,
    sections: SelectableSections,
    onContinue: (selections: Map<String, Int>, note: String?) -> Unit,
    modifier: Modifier = Modifier,
    onScreenView: () -> Unit = {},
    continueButtonTestTag: String? = null,
    clearButtonTestTag: String? = null,
) {
    // Fires the screen-view event once per entry (including each loop back into
    // the screen, where the caller may bump its own counters).
    LaunchedEffect(Unit) { onScreenView() }

    val items = sections.sections

    // -1 = unanswered. A fresh array is assigned on each change so the structural-equality
    // policy schedules a recomposition. Keyed by section count so it resets when the config
    // changes.
    var selected by rememberSaveable(items.size) {
        mutableStateOf(IntArray(items.size) { -1 })
    }

    // One section expanded at a time; -1 = none. Defaults to the first section.
    var expandedSection by rememberSaveable(items.size) { mutableStateOf(0) }

    fun advanceFrom(sectionIndex: Int) {
        expandedSection = items.indices.firstOrNull { it != sectionIndex && selected[it] < 0 } ?: -1
    }

    val continueEnabled = items.indices.all { selected[it] >= 0 || !items[it].required }

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
                SelectableSectionCard(
                    section = section,
                    selectedIndex = selected[index].takeIf { it >= 0 },
                    expanded = expandedSection == index,
                    onHeaderClick = {
                        expandedSection = if (expandedSection == index) -1 else index
                    },
                    onSelect = { optionIndex ->
                        selected = selected.copyOf().also { it[index] = optionIndex }
                        advanceFrom(index)
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
                    selected = IntArray(items.size) { -1 }
                    expandedSection = 0
                },
                size = OSButtonSize.Big,
                modifier = Modifier
                    .weight(1f)
                    .let { if (clearButtonTestTag != null) it.test(clearButtonTestTag) else it },
            )
            PrimaryButton(
                text = stringResource(Res.string.continue_camel_case),
                onClick = {
                    val selections = buildMap {
                        items.forEachIndexed { index, section ->
                            if (selected[index] >= 0) put(section.id, selected[index])
                        }
                    }
                    onContinue(selections, null)
                },
                enabled = continueEnabled,
                size = OSButtonSize.Big,
                modifier = Modifier
                    .weight(1f)
                    .let { if (continueButtonTestTag != null) it.test(continueButtonTestTag) else it },
            )
        }
    }
}

@Composable
private fun SelectableSectionCard(
    section: SelectableSection,
    selectedIndex: Int?,
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
                .padding(Variables.GapL),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OSText(
                text = section.title,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
                color = colors.neutral_p3,
            )

            // The selected-value summary flexes into the space between the title and
            // the chevron; its label ellipsizes so the icon and chevron are never pushed
            // off-screen at large font scales. When nothing is selected the empty weighted
            // slot simply pushes the chevron to the end.
            var lastSelectedIndex by remember { mutableStateOf(selectedIndex) }
            if (selectedIndex != null) lastSelectedIndex = selectedIndex
            AnimatedVisibility(
                visible = selectedIndex != null,
                modifier = Modifier.weight(1f),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                lastSelectedIndex?.let {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OSText(
                            text = section.options[it].label,
                            fontSize = 14.sp,
                            color = colors.neutral_p2,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .padding(start = Variables.GapS),
                        )
                        Spacer(Modifier.width(4.dp))
                        Image(
                            painter = painterResource(section.options[it].icon),
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                        )
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
                    val isSelected = index == selectedIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(index) }
                            .background(
                                if (isSelected) colors.primary_m3 else colors.neutral_m4,
                            )
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
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) colors.primary_p1 else colors.neutral_p3,
                        )
                    }
                }
            }
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
            selectedIndex = 1,
            expanded = false,
            onHeaderClick = {},
            onSelect = {},
        )
    }
}
