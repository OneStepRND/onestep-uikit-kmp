package co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.staticBalance

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import co.onestep.designsystem.components.OSButtonSize
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.components.PrimaryButton
import co.onestep.designsystem.components.SecondaryButton
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTTagField
import co.onestep.kmp.uikit.features.tagging.CustomTextField
import co.onestep.kmp.uikit.models.OSTTagValue
import co.onestep.kmp.uikit.testing.OSTTestTags
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.kmp.uikit.utils.UIktDestination
import co.onestep.kmp.uikit.utils.test
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.go_to_summary
import co.onestep.kmp.uikit_kmp.generated.resources.ic_alert_stroke
import co.onestep.kmp.uikit_kmp.generated.resources.ic_check_circle_solid
import co.onestep.kmp.uikit_kmp.generated.resources.ic_eye
import co.onestep.kmp.uikit_kmp.generated.resources.ic_fall
import co.onestep.kmp.uikit_kmp.generated.resources.ic_stability
import co.onestep.kmp.uikit_kmp.generated.resources.ic_steps
import co.onestep.kmp.uikit_kmp.generated.resources.record_another_test
import co.onestep.kmp.uikit_kmp.generated.resources.recording_saved
import co.onestep.kmp.uikit_kmp.generated.resources.static_balance_observations_hint
import co.onestep.kmp.uikit_kmp.generated.resources.static_balance_outcomes_question
import co.onestep.kmp.uikit_kmp.generated.resources.static_balance_outcomes_warning
import co.onestep.kmp.uikit_kmp.generated.resources.static_balance_seconds_short
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlinx.serialization.Serializable

@Deprecated(
    "Moved to the OSTTestTags catalog",
    ReplaceWith(
        "OSTTestTags.StaticBalance.RECORDING_SAVED_GO_TO_SUMMARY_BUTTON",
        "co.onestep.kmp.uikit.testing.OSTTestTags",
    ),
)
const val RECORDING_SAVED_GO_TO_SUMMARY_BUTTON =
    OSTTestTags.StaticBalance.RECORDING_SAVED_GO_TO_SUMMARY_BUTTON

@Deprecated(
    "Moved to the OSTTestTags catalog",
    ReplaceWith(
        "OSTTestTags.StaticBalance.RECORDING_SAVED_RECORD_ANOTHER_BUTTON",
        "co.onestep.kmp.uikit.testing.OSTTestTags",
    ),
)
const val RECORDING_SAVED_RECORD_ANOTHER_BUTTON =
    OSTTestTags.StaticBalance.RECORDING_SAVED_RECORD_ANOTHER_BUTTON

@Serializable
data object RecordingSavedDestination : UIktDestination

/**
 * Static Balance "Recording saved" screen (OS-15960, PRD §4.3 Post-Recording; Figma "SDK - Ready
 * for Dev" 14259:18939).
 *
 * Shown after a condition's recording uploads: confirms the save and recaps the condition with its
 * recorded length. When the tag catalog drives this recording it then asks which events happened
 * during the trial — the outcome chips, already narrowed to the condition — with a warning that
 * tagging any of them fails the test. Last, an optional free-text observation note. The clinician
 * then either records another condition (same session) or goes to the web summary (flow finishes;
 * the host app opens the web summary).
 *
 * [showNote] is the host's post-tagging decision, read off the configuration by the flow
 * (`OSTRecordingConfiguration.collectsPostRecordingNote`). It exists because this screen is where
 * the note actually lives: `staticBalance()`'s `postTaggingData` says `note = true` and explains
 * that the note is collected *here* rather than on the generic tagging screen — but until
 * OS-16914 nothing read it, so a host that switched the note off still got the field. A workspace
 * that blinds the analysis is the case that matters: its clinician cannot see the note again in
 * the app, so asking for one is a dead end (`OSTSummaryOptions.None` + `OSTPostTaggingData.None`).
 */
fun EntryProviderScope<NavKey>.recordingSavedScreen(
    conditionLine: () -> String,
    durationSeconds: () -> Int,
    onRecordAnother: (note: String?) -> Unit,
    onGoToSummary: (note: String?) -> Unit,
    showNote: () -> Boolean = { true },
) = recordingSavedScreen(
    conditionLine = conditionLine,
    durationSeconds = durationSeconds,
    outcomes = { null },
    showNote = showNote,
    onRecordAnother = { note, _ -> onRecordAnother(note) },
    onGoToSummary = { note, _ -> onGoToSummary(note) },
)

/**
 * The catalog-aware entry. [outcomes] is the `$balance_result_states` field narrowed to the
 * condition just recorded, or null on the legacy Condition Setup, which asks none. Both actions
 * suspend and receive the note and the chosen outcomes as a `tag_map` (empty when none were
 * chosen): the update is awaited before the flow navigates away, otherwise finishing the flow
 * cancels it and they are lost.
 */
internal fun EntryProviderScope<NavKey>.recordingSavedScreen(
    conditionLine: () -> String,
    durationSeconds: () -> Int,
    outcomes: () -> OSTTagField?,
    showNote: () -> Boolean,
    onRecordAnother: suspend (note: String?, outcomes: Map<String, OSTTagValue>) -> Unit,
    onGoToSummary: suspend (note: String?, outcomes: Map<String, OSTTagValue>) -> Unit,
) {
    entry<RecordingSavedDestination> {
        RecordingSavedScreen(
            conditionLine = conditionLine(),
            durationSeconds = durationSeconds(),
            outcomes = outcomes(),
            onRecordAnother = onRecordAnother,
            onGoToSummary = onGoToSummary,
            showNote = showNote(),
        )
    }
}

@Composable
internal fun RecordingSavedScreen(
    conditionLine: String,
    durationSeconds: Int,
    modifier: Modifier = Modifier,
    outcomes: OSTTagField? = null,
    onRecordAnother: suspend (note: String?, outcomes: Map<String, OSTTagValue>) -> Unit = { _, _ -> },
    onGoToSummary: suspend (note: String?, outcomes: Map<String, OSTTagValue>) -> Unit = { _, _ -> },
    showNote: Boolean = true,
) {
    val colors = LocalOSColors.current
    val note = rememberSaveable { mutableStateOf<String?>(null) }
    // The chosen outcome codes; a List<String> is saveable as-is.
    var chosen by rememberSaveable { mutableStateOf(emptyList<String>()) }
    val scope = rememberCoroutineScope()
    // Guards both actions while the update is in flight, so a second tap cannot start a
    // duplicate save or a second navigation.
    var saving by remember { mutableStateOf(false) }

    fun submit(action: suspend (String?, Map<String, OSTTagValue>) -> Unit) {
        if (saving) return
        saving = true
        scope.launch {
            try {
                action(note.value?.takeIf { showNote && it.isNotBlank() }, outcomesTagMap(outcomes, chosen))
            } finally {
                saving = false
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.neutral_m4)
            .test(OSTTestTags.StaticBalance.RECORDING_SAVED_SCREEN),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Variables.GapL)
                .padding(top = Variables.GapL, bottom = 200.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(Res.drawable.ic_check_circle_solid),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                OSText(
                    text = stringResource(Res.string.recording_saved),
                    fontSize = 24.sp,
                    lineHeight = 33.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.neutral_p3,
                    modifier = Modifier.padding(start = Variables.GapM),
                )
            }
            OSText(
                text = recapLine(
                    conditionLine,
                    stringResource(Res.string.static_balance_seconds_short, durationSeconds),
                ),
                fontSize = 16.sp,
                color = colors.neutral_p2,
                modifier = Modifier.padding(top = 4.dp),
            )
            if (outcomes != null) {
                Spacer(Modifier.height(32.dp))
                OutcomesSection(
                    outcomes = outcomes,
                    isChosen = { it in chosen },
                    onToggle = { code -> chosen = if (code in chosen) chosen - code else chosen + code },
                )
            }
            if (showNote) {
                CustomTextField(
                    value = note.value,
                    onValueChange = { note.value = it },
                    modifier = Modifier.padding(vertical = Variables.GapL),
                    hintRes = Res.string.static_balance_observations_hint,
                    testTag = OSTTestTags.StaticBalance.RECORDING_SAVED_NOTE_FIELD,
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(colors.neutral_m4)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = Variables.GapL, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(Variables.GapL),
        ) {
            PrimaryButton(
                text = stringResource(Res.string.go_to_summary),
                onClick = { submit(onGoToSummary) },
                enabled = !saving,
                size = OSButtonSize.Big,
                modifier = Modifier
                    .fillMaxWidth()
                    .test(OSTTestTags.StaticBalance.RECORDING_SAVED_GO_TO_SUMMARY_BUTTON),
            )
            SecondaryButton(
                text = stringResource(Res.string.record_another_test),
                onClick = { submit(onRecordAnother) },
                enabled = !saving,
                size = OSButtonSize.Big,
                modifier = Modifier
                    .fillMaxWidth()
                    .test(OSTTestTags.StaticBalance.RECORDING_SAVED_RECORD_ANOTHER_BUTTON),
            )
        }
    }
}

/** "Feet together | Eyes open | Firm | 10 sec": the condition, then its recorded length. */
internal fun recapLine(conditionLine: String, duration: String): String =
    if (conditionLine.isBlank()) duration else "$conditionLine | $duration"

/**
 * The chosen outcomes as a `tag_map` entry: the codes under the field's name, in catalog order, or
 * nothing when none were chosen (no key at all, never an empty list).
 */
internal fun outcomesTagMap(
    outcomes: OSTTagField?,
    chosen: List<String>,
): Map<String, OSTTagValue> {
    if (outcomes == null) return emptyMap()
    val codes = outcomes.options.map { it.value }.filter { it in chosen }
    return if (codes.isEmpty()) emptyMap() else mapOf(outcomes.name to OSTTagValue.Multiple(codes))
}

/** The design's icon for an outcome code, or null for an outcome it gives none. */
internal fun outcomeIcon(code: String): DrawableResource? =
    when (code) {
        "fell" -> Res.drawable.ic_fall
        "stepped_out" -> Res.drawable.ic_steps
        "opened_eyes" -> Res.drawable.ic_eye
        // The design's "Lost balance" chip: the trial ended because balance was lost.
        "aborted_early" -> Res.drawable.ic_stability
        else -> null
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OutcomesSection(
    outcomes: OSTTagField,
    isChosen: (String) -> Boolean,
    onToggle: (String) -> Unit,
) {
    val colors = LocalOSColors.current
    OSText(
        text = stringResource(Res.string.static_balance_outcomes_question),
        fontSize = 18.sp,
        lineHeight = 25.sp,
        fontWeight = FontWeight.Bold,
        color = colors.neutral_p3,
    )
    Spacer(Modifier.height(Variables.GapL))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Variables.GapM),
        verticalArrangement = Arrangement.spacedBy(Variables.GapM),
    ) {
        outcomes.options.forEach { option ->
            OutcomeChip(
                label = option.label,
                icon = outcomeIcon(option.value),
                selected = isChosen(option.value),
                onToggle = { onToggle(option.value) },
                testTag = OSTTestTags.StaticBalance.recordingSavedOutcome(option.value),
            )
        }
    }
    Spacer(Modifier.height(Variables.GapL))
    Row(modifier = Modifier.padding(vertical = Variables.GapM)) {
        Image(
            painter = painterResource(Res.drawable.ic_alert_stroke),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(14.dp),
        )
        OSText(
            text = stringResource(Res.string.static_balance_outcomes_warning),
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = colors.neutral_p2,
            modifier = Modifier.padding(start = Variables.GapM),
        )
    }
}

/** One outcome: a pill that fills red while chosen. A checkbox to accessibility services. */
@Composable
private fun OutcomeChip(
    label: String,
    icon: DrawableResource?,
    selected: Boolean,
    onToggle: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalOSColors.current
    val fill by animateColorAsState(
        targetValue = if (selected) colors.error_m1 else colors.neutral_m2,
        label = "outcomeChipFill",
    )
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = modifier
            .height(27.dp)
            .clip(shape)
            .background(fill, shape)
            .toggleable(value = selected, role = Role.Checkbox, onValueChange = { onToggle() })
            .test(testTag)
            .padding(horizontal = Variables.GapM, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        icon?.let {
            Icon(
                painter = painterResource(it),
                contentDescription = null,
                tint = colors.neutral_p3,
                modifier = Modifier.size(16.dp),
            )
        }
        OSText(
            text = label,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = colors.neutral_p3,
        )
    }
}

private val previewOutcomes =
    OSTTagField(
        name = "\$balance_result_states",
        label = "What happened",
        type = OSTTagField.TYPE_CHECKBOX,
        stage = OSTTagField.STAGE_POST_RECORD,
        options = listOf(
            OSTTagField.Option("Fell", "fell"),
            OSTTagField.Option("Stepped out", "stepped_out"),
            OSTTagField.Option("Grabbed support", "grabbed_support"),
            OSTTagField.Option("Opened eyes", "opened_eyes"),
        ),
    )

@Preview
@Composable
private fun RecordingSavedScreenPreview() {
    PreviewTheme {
        RecordingSavedScreen(
            conditionLine = "Feet together | Eyes open | Firm",
            durationSeconds = 10,
            outcomes = previewOutcomes,
        )
    }
}

/** The legacy Condition Setup: no outcomes are asked. */
@Preview
@Composable
private fun RecordingSavedScreenNoOutcomesPreview() {
    PreviewTheme {
        RecordingSavedScreen(
            conditionLine = "Feet together | Eyes open | Firm",
            durationSeconds = 30,
        )
    }
}

/** A host that collects no note — a blinded research workspace (OS-16914). */
@Preview
@Composable
private fun RecordingSavedScreenNoNotePreview() {
    PreviewTheme {
        RecordingSavedScreen(
            conditionLine = "Feet together | Eyes open | Firm",
            durationSeconds = 30,
            outcomes = previewOutcomes,
            showNote = false,
        )
    }
}
