package co.onestep.kmp.uikit.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import co.onestep.kmp.uikit.testing.OSTTestTags
import co.onestep.kmp.uikit.utils.test
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.designsystem.components.OSButtonSize
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.components.PrimaryButton
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.features.recordFlow.components.ToolBarHeight
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.wheel_picker_save
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Generic screen wrapping a [WheelNumberPicker] with a title and a sticky Save button.
 * Reusable for any numeric-entry use case.
 *
 * The screen has NO embedded top bar — back / close are owned by the host NavHost's shared
 * toolbar overlay. The root column reserves [ToolBarHeight] of top padding so its content
 * does not overlap the overlay.
 *
 * Per project rules this composable only takes stable primitives + lambdas — no ViewModel and
 * no unstable parameter types in the signature. `IntRange` is a stable value class.
 *
 * @param title Title text shown above the picker.
 * @param initialValue Initial value to pre-select, or `null` for the blank state.
 * @param onSave Invoked with the final integer when the user taps Save.
 * @param range Numeric range the wheel can scroll through.
 * @param saveLabel Label for the sticky Save button.
 * @param isSubmitting When `true`, the Save button is disabled and shows a spinner.
 * @param error Optional error message to display below the picker.
 * @param applyTopToolBarPadding When `true` (default), reserves [ToolBarHeight] of top padding
 * so content does not overlap a toolbar overlay. Pass `false` when the host already pushes
 * content down via a Column-based toolbar (avoids a double gap).
 */
@Composable
internal fun WheelPickerScreen(
    title: String,
    initialValue: Int?,
    onSave: (Int) -> Unit,
    range: IntRange = 0..40,
    saveLabel: String = stringResource(Res.string.wheel_picker_save),
    isSubmitting: Boolean = false,
    error: String? = null,
    applyTopToolBarPadding: Boolean = true,
) {
    var selected by remember { mutableStateOf(initialValue) }
    val colors = LocalOSColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.neutral_m4)
            .test(OSTTestTags.Summary.STS_MANUAL_REPORT_SCREEN)
            .padding(top = if (applyTopToolBarPadding) ToolBarHeight.dp else 0.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Title
        OSText(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            text = title,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Bold,
            color = colors.neutral_p3,
            textAlign = TextAlign.Start,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Wheel picker
        WheelNumberPicker(
            value = selected,
            onValueChange = { selected = it },
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .test(OSTTestTags.Summary.STS_MANUAL_REPORT_PICKER),
            range = range,
        )

        if (!error.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            OSText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                text = error,
                fontSize = 14.sp,
                color = colors.error_p2,
                textAlign = TextAlign.Center,
            )
        }

        // Push button to the bottom.
        Spacer(modifier = Modifier.weight(1f))

        // Sticky Save button
        val saveEnabled = selected != null && !isSubmitting
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            PrimaryButton(
                text = if (isSubmitting) "" else saveLabel,
                onClick = {
                    val value = selected
                    if (value != null && !isSubmitting) onSave(value)
                },
                enabled = saveEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .test(OSTTestTags.Summary.STS_MANUAL_REPORT_SAVE_BUTTON),
                size = OSButtonSize.Big,
            )
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = colors.neutral_m3,
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}

@Preview
@Composable
private fun WheelPickerScreenPreviewBlank() {
    PreviewTheme {
        WheelPickerScreen(
            title = "How many repetitions were performed?",
            initialValue = null,
            onSave = {},
        )
    }
}

@Preview
@Composable
private fun WheelPickerScreenPreviewFilled() {
    PreviewTheme {
        WheelPickerScreen(
            title = "How many repetitions were performed?",
            initialValue = 2,
            onSave = {},
        )
    }
}

@Preview
@Composable
private fun WheelPickerScreenPreviewError() {
    PreviewTheme {
        WheelPickerScreen(
            title = "How many repetitions were performed?",
            initialValue = 5,
            onSave = {},
            isSubmitting = false,
            error = "Could not submit. Please try again.",
        )
    }
}

@Preview
@Composable
private fun WheelPickerScreenPreviewSubmitting() {
    PreviewTheme {
        WheelPickerScreen(
            title = "How many repetitions were performed?",
            initialValue = 8,
            onSave = {},
            isSubmitting = true,
        )
    }
}
