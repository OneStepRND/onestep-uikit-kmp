package co.onestep.kmp.uikit.features.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import co.onestep.designsystem.components.OSButtonSize
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.components.PrimaryButton
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit.features.recordFlow.components.SelectableOption
import co.onestep.kmp.uikit.features.recordFlow.components.SelectableSection
import co.onestep.kmp.uikit.features.recordFlow.components.SelectableSections
import co.onestep.kmp.uikit.features.recordFlow.components.SelectableSectionsScreen
import co.onestep.kmp.uikit.features.recordFlow.components.Toolbar
import co.onestep.kmp.uikit.features.recordFlow.components.joinSelectedLabels
import co.onestep.kmp.uikit.features.recordFlow.screensData.IconData
import co.onestep.kmp.uikit.features.recordFlow.screensData.ToolBarData
import co.onestep.kmp.uikit.utils.test
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.ic_back_arrow
import co.onestep.kmp.uikit_kmp.generated.resources.ic_footwear_barefoot
import co.onestep.kmp.uikit_kmp.generated.resources.ic_footwear_shoes
import co.onestep.kmp.uikit_kmp.generated.resources.ic_footwear_socks
import co.onestep.kmp.uikit_kmp.generated.resources.ic_stance_feet_together
import co.onestep.kmp.uikit_kmp.generated.resources.ic_stance_semi_tandem
import co.onestep.kmp.uikit_kmp.generated.resources.ic_stance_tandem
import co.onestep.kmp.uikit_kmp.generated.resources.ic_surface_dome
import co.onestep.kmp.uikit_kmp.generated.resources.ic_surface_firm
import co.onestep.kmp.uikit_kmp.generated.resources.ic_surface_foam
import co.onestep.kmp.uikit_kmp.generated.resources.ic_surface_uneven
import co.onestep.kmp.uikit_kmp.generated.resources.list_separator
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Hand-QA harness for per-section multi-select on the condition setup screen (OS-17191,
 * mirroring the native uikit's OS-17189).
 *
 * Static Balance passes `allowsMultiSelect = false` for every category, so the multi-select
 * path has no consumer in the real flow and cannot be reached from any measurement. This
 * screen is the way to exercise it on a device: a mixed section list — one single-select,
 * one required multi-select and one optional multi-select — over the very same
 * [SelectableSectionsScreen] the flow uses, followed by a readout of exactly what
 * `onContinue` emitted.
 *
 * Public only so the Android/iOS test apps can reach it — not intended as consumer API,
 * exactly like [OSTPushPopDemo] beside it. The native uikit keeps its twin in `src/debug` so
 * it never reaches the published AAR; KMP has no variant split that also covers the iOS
 * XCFramework, so this ships as unreferenced code in both artifacts instead. It depends on
 * nothing outside the module and adds one symbol to the published surface.
 *
 * What to check here (the ticket's QA list):
 * - Stance is single-select: picking a value collapses it and opens the next unanswered section.
 * - Surface is multi-select: options toggle on and off, the section stays open, nothing
 *   auto-advances, and selected rows carry the trailing check.
 * - Collapsing Surface joins the chosen labels in catalog order; the 36dp option icon shows
 *   only when exactly one option is selected.
 * - Continue stays disabled until Stance and Surface each hold a selection; optional
 *   Footwear never gates it.
 * - Clear all empties both cardinalities and reopens the first section.
 * - Rotate the device or background and restore the app mid-selection: the choices survive.
 * - The readout after Continue lists indices in catalog order (not tap order) and omits any
 *   section left unanswered.
 *
 * @param onDismiss called when back is invoked from the toolbar.
 */
@Composable
fun OSTSelectableSectionsQaScreen(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Plain remember: a Map is not reliably saveable, and the rotation that matters for QA is
    // the one mid-selection, whose state lives inside SelectableSectionsScreen.
    var emitted by remember { mutableStateOf<Map<String, List<Int>>?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LocalOSColors.current.neutral_m4)
            .test("qa.multiSelect"),
    ) {
        Toolbar(
            toolbarData = ToolBarData(
                startIcon = IconData(icon = Res.drawable.ic_back_arrow, action = onDismiss),
            ),
        )
        val result = emitted
        if (result == null) {
            SelectableSectionsScreen(
                title = "Multi-select QA",
                sections = qaSections,
                modifier = Modifier.weight(1f),
                continueButtonTestTag = "qa.multiSelect.continue",
                clearButtonTestTag = "qa.multiSelect.clearAll",
                onContinue = { selections, _ -> emitted = selections },
            )
        } else {
            QaResult(
                selections = result,
                modifier = Modifier.weight(1f),
                onBackToPicker = { emitted = null },
            )
        }
    }
}

/** What `onContinue` handed out, section by section, so catalog order can be read off. */
@Composable
private fun QaResult(
    selections: Map<String, List<Int>>,
    onBackToPicker: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalOSColors.current
    // The same separator the collapsed header joins with, so the readout and the header
    // agree in every locale.
    val separator = stringResource(Res.string.list_separator)
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Variables.GapL)
            .verticalScroll(rememberScrollState())
            .test("qa.multiSelect.result"),
    ) {
        OSText(
            text = "onContinue emitted",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = Variables.GapL),
        )
        if (selections.isEmpty()) {
            OSText(
                text = "(nothing — every section was left unanswered)",
                color = colors.neutral_p2,
            )
        }
        qaSections.sections.forEach { section ->
            val indices = selections[section.id]
            val shape = buildString {
                append(if (section.allowsMultiSelect) "multi-select" else "single-select")
                append(if (section.required) ", required" else ", optional")
            }
            OSText(
                text = "${section.title}  ($shape)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = Variables.GapM),
            )
            OSText(
                text = if (indices == null) {
                    "omitted (no selection)"
                } else {
                    joinSelectedLabels(section, indices, separator) +
                        "  —  indices ${indices.joinToString(", ")}"
                },
                fontSize = 14.sp,
                color = colors.neutral_p2,
                modifier = Modifier.test("qa.multiSelect.result.${section.id}"),
            )
        }
        Spacer(Modifier.height(Variables.GapXL))
        PrimaryButton(
            text = "BACK TO PICKER",
            onClick = onBackToPicker,
            size = OSButtonSize.Big,
            modifier = Modifier
                .fillMaxWidth()
                .test("qa.multiSelect.backToPicker"),
        )
        Spacer(Modifier.height(Variables.GapXL))
    }
}

/**
 * One section of each shape the screen supports: single-select required, multi-select
 * required, multi-select optional. Labels and icons are borrowed from the Static Balance
 * catalog purely because the drawables exist; nothing here feeds a recording.
 *
 * Titles stay as short as the real catalog's. The header lays out an unweighted title, then
 * the weighted selection summary, then the chevron — so a title long enough to wrap squeezes
 * both the summary and the chevron out of the row. That is true of the screen on `main` too,
 * for any long server-provided `displayName`; a QA harness with padded titles would just hide
 * the selection summary this task is here to exercise. Each section's cardinality is spelled
 * out in the readout instead.
 */
private val qaSections = SelectableSections(
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
            id = "surface",
            title = "Surface",
            allowsMultiSelect = true,
            options = listOf(
                SelectableOption("Firm", Res.drawable.ic_surface_firm),
                SelectableOption("Foam", Res.drawable.ic_surface_foam),
                SelectableOption("Dome", Res.drawable.ic_surface_dome),
                SelectableOption("Uneven", Res.drawable.ic_surface_uneven),
            ),
        ),
        SelectableSection(
            id = "footwear",
            title = "Footwear",
            required = false,
            allowsMultiSelect = true,
            options = listOf(
                SelectableOption("Shoes", Res.drawable.ic_footwear_shoes),
                SelectableOption("Barefoot", Res.drawable.ic_footwear_barefoot),
                SelectableOption("Socks", Res.drawable.ic_footwear_socks),
            ),
        ),
    ),
)

@Preview
@Composable
private fun OSTSelectableSectionsQaScreenPreview() {
    OSTSelectableSectionsQaScreen(onDismiss = {})
}
