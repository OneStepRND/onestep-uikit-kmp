package co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.staticBalance

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import co.onestep.kmp.uikit.features.tagging.CustomTextField
import co.onestep.kmp.uikit.testing.OSTTestTags
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.kmp.uikit.utils.UIktDestination
import co.onestep.kmp.uikit.utils.test
import co.onestep.kmp.uikit.utils.toDisplayTime
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.go_to_summary
import co.onestep.kmp.uikit_kmp.generated.resources.ic_check_circle
import co.onestep.kmp.uikit_kmp.generated.resources.ic_clock
import co.onestep.kmp.uikit_kmp.generated.resources.record_another_test
import co.onestep.kmp.uikit_kmp.generated.resources.recording_saved
import co.onestep.kmp.uikit_kmp.generated.resources.static_balance_duration
import co.onestep.kmp.uikit_kmp.generated.resources.static_balance_observations_hint
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
 * Static Balance "Recording saved" screen (OS-15960, PRD §4.3 Post-Recording).
 *
 * Shown after a condition's recording uploads: confirms the save, recaps the condition and
 * the actual recorded duration, and offers an optional free-text note — the single
 * per-condition note, attached to the nested `onestep_balance_conditions` metadata via
 * `updateBalanceConditionNote`. The clinician then either records another condition (same
 * session) or goes to the web summary (flow finishes; the host app opens the web summary).
 */
fun EntryProviderScope<NavKey>.recordingSavedScreen(
    conditionLine: () -> String,
    durationSeconds: () -> Int,
    onRecordAnother: (note: String?) -> Unit,
    onGoToSummary: (note: String?) -> Unit,
) {
    entry<RecordingSavedDestination> {
        RecordingSavedScreen(
            conditionLine = conditionLine(),
            durationSeconds = durationSeconds(),
            onRecordAnother = onRecordAnother,
            onGoToSummary = onGoToSummary,
        )
    }
}

@Composable
internal fun RecordingSavedScreen(
    conditionLine: String,
    durationSeconds: Int,
    modifier: Modifier = Modifier,
    onRecordAnother: (note: String?) -> Unit = {},
    onGoToSummary: (note: String?) -> Unit = {},
) {
    val colors = LocalOSColors.current
    val note = rememberSaveable { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .test(OSTTestTags.StaticBalance.RECORDING_SAVED_SCREEN),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Variables.GapL)
                .padding(bottom = 180.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.info_m3, RoundedCornerShape(12.dp))
                    .padding(Variables.GapL),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_check_circle),
                        contentDescription = null,
                        tint = colors.info_p1,
                        modifier = Modifier.size(20.dp),
                    )
                    OSText(
                        text = stringResource(Res.string.recording_saved),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.neutral_p3,
                        modifier = Modifier.padding(start = Variables.GapM),
                    )
                }
                Spacer(Modifier.height(Variables.GapM))
                HorizontalDivider(color = colors.neutral_m1)
                Spacer(Modifier.height(Variables.GapM))
                OSText(
                    text = conditionLine,
                    fontSize = 14.sp,
                    color = colors.neutral_p2,
                )
                Spacer(Modifier.height(Variables.GapM))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_clock),
                        contentDescription = null,
                        tint = colors.neutral_p2,
                        modifier = Modifier.size(18.dp),
                    )
                    OSText(
                        text = stringResource(Res.string.static_balance_duration),
                        fontSize = 14.sp,
                        color = colors.neutral_p2,
                        modifier = Modifier.padding(start = Variables.GapM),
                    )
                    Spacer(Modifier.weight(1f))
                    OSText(
                        text = durationSeconds.toDisplayTime(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.primary_p1,
                    )
                }
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
                .background(colors.neutral_m3)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(Variables.GapL),
        ) {
            PrimaryButton(
                text = stringResource(Res.string.go_to_summary),
                onClick = { onGoToSummary(note.value?.takeIf { it.isNotBlank() }) },
                size = OSButtonSize.Big,
                modifier = Modifier
                    .fillMaxWidth()
                    .test(OSTTestTags.StaticBalance.RECORDING_SAVED_GO_TO_SUMMARY_BUTTON),
            )
            Spacer(Modifier.height(Variables.GapM))
            SecondaryButton(
                text = stringResource(Res.string.record_another_test),
                onClick = { onRecordAnother(note.value?.takeIf { it.isNotBlank() }) },
                size = OSButtonSize.Big,
                modifier = Modifier
                    .fillMaxWidth()
                    .test(OSTTestTags.StaticBalance.RECORDING_SAVED_RECORD_ANOTHER_BUTTON),
            )
        }
    }
}

@Preview
@Composable
private fun RecordingSavedScreenPreview() {
    PreviewTheme {
        RecordingSavedScreen(
            conditionLine = "Feet together | Eyes open | Firm",
            durationSeconds = 30,
        )
    }
}
