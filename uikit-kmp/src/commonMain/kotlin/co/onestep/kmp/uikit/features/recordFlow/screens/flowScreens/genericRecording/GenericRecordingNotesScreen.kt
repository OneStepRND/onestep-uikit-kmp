package co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.genericRecording

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import co.onestep.designsystem.components.OSButtonSize
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.components.PrimaryButton
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit.features.tagging.CustomTextField
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.kmp.uikit.utils.UIktDestination
import co.onestep.kmp.uikit.utils.test
import co.onestep.kmp.uikit.utils.toDisplayTime
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.continue_camel_case
import co.onestep.kmp.uikit_kmp.generated.resources.generic_recording_duration
import co.onestep.kmp.uikit_kmp.generated.resources.generic_recording_note_hint
import co.onestep.kmp.uikit_kmp.generated.resources.ic_check_circle
import co.onestep.kmp.uikit_kmp.generated.resources.ic_clock
import co.onestep.kmp.uikit_kmp.generated.resources.recording_saved
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

const val GENERIC_RECORDING_NOTES_CONTINUE_BUTTON = "generic_recording_notes_continue_button"

@Serializable
data object GenericRecordingNotesDestination : UIktDestination

/**
 * Generic Recording post-recording notes screen (OS-16861, PRD §2.1 "Notes Screen").
 *
 * Shown as soon as the recording ends — whether the participant stopped it or the 30-minute limit
 * did — and once the raw data has been stored. Confirms the save, recaps the actual recorded
 * duration, and offers a single optional free-text note describing what happened.
 *
 * The note is *optional* by design: Continue always proceeds, so a participant is never trapped on
 * this screen. A blank note is delivered as null rather than an empty string.
 *
 * Unlike the Static Balance "Recording saved" screen there is no session loop and no summary to go
 * to, so the screen carries one action: Continue, which finishes the flow.
 *
 * [onContinue] is suspending: the note update is awaited before the flow navigates away, otherwise
 * finishing the host tears the flow down mid-request and the note is lost.
 */
fun EntryProviderScope<NavKey>.genericRecordingNotesScreen(
    durationSeconds: () -> Int,
    onContinue: suspend (note: String?) -> Unit,
) {
    entry<GenericRecordingNotesDestination> {
        GenericRecordingNotesScreen(
            durationSeconds = durationSeconds(),
            onContinue = onContinue,
        )
    }
}

@Composable
internal fun GenericRecordingNotesScreen(
    durationSeconds: Int,
    modifier: Modifier = Modifier,
    onContinue: suspend (note: String?) -> Unit = {},
) {
    val colors = LocalOSColors.current
    val note = rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    // Guards Continue while the note update is in flight, so a second tap cannot start a duplicate
    // save or a second navigation.
    var saving by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Variables.GapL)
                // Clears the sticky Continue button below.
                .padding(bottom = 120.dp),
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
                // No recap line: OneStep does not know what was recorded, which is what the note
                // below is for.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_clock),
                        contentDescription = null,
                        tint = colors.neutral_p2,
                        modifier = Modifier.size(18.dp),
                    )
                    OSText(
                        text = stringResource(Res.string.generic_recording_duration),
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
                    hintRes = Res.string.generic_recording_note_hint,
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
                text = stringResource(Res.string.continue_camel_case),
                // Blank input is "no note": the note is optional and must not be persisted as an
                // empty string.
                onClick = {
                    if (!saving) {
                        saving = true
                        scope.launch {
                            try {
                                onContinue(note.value?.takeIf { it.isNotBlank() })
                            } finally {
                                saving = false
                            }
                        }
                    }
                },
                size = OSButtonSize.Big,
                modifier = Modifier
                    .fillMaxWidth()
                    .test(GENERIC_RECORDING_NOTES_CONTINUE_BUTTON),
            )
        }
    }
}

@Preview
@Composable
private fun GenericRecordingNotesScreenPreview() {
    PreviewTheme {
        GenericRecordingNotesScreen(durationSeconds = 754)
    }
}
