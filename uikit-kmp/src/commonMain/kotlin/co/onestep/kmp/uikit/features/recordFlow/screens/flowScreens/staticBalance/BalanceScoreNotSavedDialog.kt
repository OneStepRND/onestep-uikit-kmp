package co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.staticBalance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
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
import co.onestep.kmp.uikit_kmp.generated.resources.continue_camel_case
import co.onestep.kmp.uikit_kmp.generated.resources.static_balance_score_not_saved_text
import co.onestep.kmp.uikit_kmp.generated.resources.static_balance_score_not_saved_title
import co.onestep.kmp.uikit_kmp.generated.resources.try_again
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Shown over "Recording saved" when the Static Balance score self-report fails (OS-17571, ported
 * from the Android SDK). The recording, its note and its result states are already saved at that
 * point; only the score is missing. The clinician either retries the self-report or continues
 * without it.
 *
 * A choice is required: back and an outside tap do nothing, so the score is never dropped
 * silently. Laid out like `DeleteMeasurementConfirmationDialog` rather than as an `OSPopup`, whose
 * buttons cannot carry test tags (see `docs/TestTags.md`).
 *
 * @param retrying True while a retry is in flight: both buttons ignore taps, so a second tap cannot
 *   start a duplicate request or leave mid-retry. The design-system buttons have no loading state,
 *   so neither shows a spinner.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BalanceScoreNotSavedDialog(
    retrying: Boolean,
    onTryAgain: () -> Unit,
    onContinue: () -> Unit,
) {
    BasicAlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    ) {
        BalanceScoreNotSavedContent(retrying = retrying, onTryAgain = onTryAgain, onContinue = onContinue)
    }
}

@Composable
private fun BalanceScoreNotSavedContent(
    retrying: Boolean,
    onTryAgain: () -> Unit,
    onContinue: () -> Unit,
) {
    Column(
        Modifier
            .wrapContentHeight()
            // A dialog composes in its own window, so the tag goes on its content root.
            .test(OSTTestTags.StaticBalance.BALANCE_SCORE_NOT_SAVED_DIALOG)
            .background(
                LocalOSColors.current.neutral_m4,
                shape = RoundedCornerShape(10.dp),
            ),
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        OSText(
            text = stringResource(Res.string.static_balance_score_not_saved_title),
            modifier = Modifier
                .padding(horizontal = Variables.GapL)
                .align(Alignment.CenterHorizontally),
            fontSize = 20.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.W700,
            textAlign = TextAlign.Center,
        )
        OSText(
            text = stringResource(Res.string.static_balance_score_not_saved_text),
            modifier = Modifier
                .padding(Variables.GapL)
                .align(Alignment.CenterHorizontally),
            fontSize = 18.sp,
            lineHeight = 25.sp,
            fontWeight = FontWeight.W400,
            textAlign = TextAlign.Center,
        )
        SecondaryButton(
            text = stringResource(Res.string.continue_camel_case),
            onClick = onContinue,
            enabled = !retrying,
            size = OSButtonSize.Big,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Variables.GapL)
                .test(OSTTestTags.StaticBalance.BALANCE_SCORE_NOT_SAVED_CONTINUE),
        )
        PrimaryButton(
            text = stringResource(Res.string.try_again),
            onClick = onTryAgain,
            enabled = !retrying,
            size = OSButtonSize.Big,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Variables.GapL)
                .test(OSTTestTags.StaticBalance.BALANCE_SCORE_NOT_SAVED_TRY_AGAIN),
        )
        Spacer(modifier = Modifier.height(Variables.GapL))
    }
}

@Preview
@Composable
private fun BalanceScoreNotSavedPreview() {
    PreviewTheme {
        BalanceScoreNotSavedContent(retrying = false, onTryAgain = {}, onContinue = {})
    }
}

@Preview
@Composable
private fun BalanceScoreNotSavedRetryingPreview() {
    PreviewTheme {
        BalanceScoreNotSavedContent(retrying = true, onTryAgain = {}, onContinue = {})
    }
}
