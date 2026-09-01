package co.onestep.kmp.uikit.features.summary.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import co.onestep.kmp.uikit.ui.theme.osClickIndication
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.recordFlow.screensData.PrimaryButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.components.SecondaryButton
import co.onestep.designsystem.components.OSButtonSize
import co.onestep.kmp.uikit.ui.components.PrimaryBrandButton
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit.testing.OSTTestTags
import co.onestep.kmp.uikit.utils.test
import org.jetbrains.compose.ui.tooling.preview.Preview
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.cancel
import co.onestep.kmp.uikit_kmp.generated.resources.discard
import co.onestep.kmp.uikit_kmp.generated.resources.discard_measurement
import co.onestep.kmp.uikit_kmp.generated.resources.discard_measurement_text
import co.onestep.kmp.uikit_kmp.generated.resources.ic_close
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Deprecated("Moved to the OSTTestTags catalog", ReplaceWith("OSTTestTags.Summary.DISCARD_DIALOG"))
const val DISCARD_MEASUREMENT_CONFIRMATION = OSTTestTags.Summary.DISCARD_DIALOG

@Composable
internal fun DeleteMeasurementConfirmationDialog(
    onDismissClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
) {
    Column(
        Modifier
            .wrapContentHeight()
            // The dialog id used to sit on both buttons, so a selector for it resolved to
            // whichever came first — Cancel. It now names the dialog; the buttons have
            // ids of their own.
            .test(OSTTestTags.Summary.DISCARD_DIALOG)
            .background(
                LocalOSColors.current.neutral_m5,
                shape = RoundedCornerShape(10.dp),
            ),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(Variables.GapL),
        ) {
            Icon(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                        .test(OSTTestTags.Summary.DISCARD_DIALOG_CLOSE_BUTTON)
                        .clickable(
                            interactionSource =
                                remember {
                                    MutableInteractionSource()
                                },
                            indication = osClickIndication(bounded = false),
                        ) {
                            onDismissClicked()
                        },
                painter = painterResource(Res.drawable.ic_close),
                contentDescription = "",
            )
        }
        OSText(
            text = stringResource(Res.string.discard_measurement),
            modifier =
                Modifier
                    .align(Alignment.CenterHorizontally),
            fontSize = 20.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.W700,
            textAlign = TextAlign.Center,
        )
        OSText(
            modifier =
                Modifier
                    .padding(Variables.GapL)
                    .align(Alignment.CenterHorizontally),
            text = stringResource(Res.string.discard_measurement_text),
            fontSize = 18.sp,
            lineHeight = 25.sp,
            fontWeight = FontWeight.W400,
            textAlign = TextAlign.Center,
        )
        SecondaryButton(
            text = stringResource(Res.string.cancel),
            onClick = onDismissClicked,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Variables.GapL)
                .height(40.dp)
                .test(OSTTestTags.Summary.DISCARD_DIALOG_CANCEL_BUTTON),
            size = OSButtonSize.Big,
        )
        PrimaryBrandButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Variables.GapL)
                .height(40.dp)
                .test(OSTTestTags.Summary.DISCARD_DIALOG_CONFIRM_BUTTON),
            data = PrimaryButtonData(
                text =
                    TextData(
                        stringResource(Res.string.discard),
                        14.sp,
                        FontWeight.W400,
                    ),
                action = onDeleteClicked,
            ),
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview
@Composable
private fun DeleteMeasurementConfirmationDialogPreview() {
    PreviewTheme {
        DeleteMeasurementConfirmationDialog(
            onDismissClicked = {},
            onDeleteClicked = {},
        )
    }
}
