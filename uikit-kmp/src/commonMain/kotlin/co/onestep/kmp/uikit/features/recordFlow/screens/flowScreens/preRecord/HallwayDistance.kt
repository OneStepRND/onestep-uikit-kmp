package co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.preRecord

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.recordFlow.components.ToolBarHeight
import co.onestep.kmp.uikit.features.recordFlow.previewHallwayValid
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import co.onestep.kmp.uikit.features.recordFlow.screensData.HallwayDistanceScreenState
import co.onestep.kmp.uikit.features.recordFlow.screensData.PrimaryButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData
import co.onestep.kmp.uikit.ui.components.KeyboardAware
import co.onestep.designsystem.components.OSText
import co.onestep.kmp.uikit.ui.components.PrimaryBrandButton
import co.onestep.designsystem.components.SecondaryButton
import co.onestep.designsystem.components.OSButtonSize
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit.ui.typography.NoirFontFamily
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.continue_camel_case
import co.onestep.kmp.uikit_kmp.generated.resources.continue_without_hallway_length
import co.onestep.kmp.uikit_kmp.generated.resources.ic_close
import co.onestep.kmp.uikit_kmp.generated.resources.short_hallway_dont_show_again
import co.onestep.kmp.uikit_kmp.generated.resources.short_hallway_edit_hallway_length
import co.onestep.kmp.uikit_kmp.generated.resources.short_hallway_length_message
import co.onestep.kmp.uikit_kmp.generated.resources.short_hallway_length_title
import co.onestep.kmp.uikit_kmp.generated.resources.short_hallway_start_test
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HallwayDistanceScreen(
    state: HallwayDistanceScreenState,
    onValueChange: (String) -> Unit,
    onContinue: () -> Unit,
    onContinueWithoutLength: () -> Unit,
) {
    val modifier = if (state.fromSummary) Modifier.padding(top = ToolBarHeight.dp) else Modifier

    KeyboardAware {
        Box(
            modifier = modifier.fillMaxSize(),
        ) {
            Column(
                modifier =
                    Modifier
                        .wrapContentHeight()
                        .padding(horizontal = Variables.GapL),
            ) {
                if (state.fromSummary) {
                    Spacer(modifier = Modifier.height(Variables.GapL))
                }

                OSText(
                    modifier =
                        Modifier
                            .fillMaxWidth(),
                    text = state.title,
                    lineHeight = 37.sp,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )

                state.subtitle?.let { text ->
                    Spacer(modifier = Modifier.height(Variables.GapM))

                    OSText(
                        text = text,
                        lineHeight = 28.sp,
                        fontSize = 16.sp,
                        color = LocalOSColors.current.neutral_p1,
                    )
                }

                Spacer(modifier = Modifier.height(Variables.GapXL))

                NumericInputField(
                    inputValue = state.inputValue,
                    unitText = state.unitText,
                    errorText = state.errorText,
                    onValueChange = onValueChange,
                )
            }

            Box(
                modifier =
                    Modifier
                        .background(Color.Transparent)
                        .align(Alignment.BottomCenter),
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Variables.GapL)
                        .align(Alignment.BottomCenter),
                ) {
                    PrimaryBrandButton(
                        modifier = Modifier.fillMaxWidth(),
                        data =
                            PrimaryButtonData(
                                text =
                                    TextData(
                                        text = stringResource(Res.string.continue_camel_case),
                                        textSize = 20.sp,
                                        fontWeight = FontWeight.W600,
                                    ),
                                enabled = state.canContinue,
                                action = onContinue,
                            ),
                    )

                    Spacer(Modifier.height(Variables.GapL))

                    if (!state.fromSummary) {
                        SecondaryButton(
                            text = stringResource(Res.string.continue_without_hallway_length),
                            onClick = onContinueWithoutLength,
                            modifier = Modifier.fillMaxWidth(),
                            size = OSButtonSize.Big,
                        )
                    }

                    if (state.fromSummary) {
                        Spacer(Modifier.height(Variables.GapXL))
                    }
                }
            }
        }
    }
}

@Composable
private fun NumericInputField(
    modifier: Modifier = Modifier,
    inputValue: String,
    unitText: String,
    errorText: String?,
    onValueChange: (String) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = inputValue, selection = TextRange(inputValue.length)))
    }
    LaunchedEffect(inputValue) {
        if (textFieldValue.text != inputValue) {
            textFieldValue = TextFieldValue(text = inputValue, selection = TextRange(inputValue.length))
        }
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { focusRequester.requestFocus() },
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
                Box {
                    BasicTextField(
                        modifier = Modifier
                            .width(IntrinsicSize.Min)
                            .focusRequester(focusRequester),
                        value = textFieldValue,
                        onValueChange = { newValue ->
                            textFieldValue = newValue.copy(selection = TextRange(newValue.text.length))
                            onValueChange(newValue.text)
                        },
                        textStyle =
                            TextStyle(
                                fontSize = 50.sp,
                                fontWeight = FontWeight.Bold,
                                color = LocalOSColors.current.neutral_p3,
                                fontFamily = NoirFontFamily(),
                            ),
                        cursorBrush = SolidColor(LocalOSColors.current.primary_p3_main),
                        singleLine = true,
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                        keyboardActions =
                            KeyboardActions(
                                onDone = { keyboardController?.hide() },
                            ),
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.Center) {
                                if (textFieldValue.text.isEmpty()) {
                                    OSText(
                                        text = "0",
                                        fontSize = 50.sp,
                                        fontWeight = FontWeight.Bold,
                                        color =
                                            LocalOSColors.current.neutral_p3
                                                .copy(alpha = 0.3f),
                                    )
                                }
                                innerTextField()
                            }
                        },
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                OSText(
                    text = unitText,
                    fontSize = 18.sp,
                    color = LocalOSColors.current.neutral_p1,
                    modifier = Modifier.padding(bottom = Variables.GapL),
                )
            }
        }

        Spacer(modifier = Modifier.height(Variables.GapS))

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = LocalOSColors.current.neutral_m1,
        )

        if (!errorText.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(Variables.GapS))
            OSText(
                modifier = Modifier.fillMaxWidth(),
                text = errorText,
                fontSize = 12.sp,
                color = LocalOSColors.current.error_p2,
            )
        }
    }
}

@Composable
internal fun ShortHallwayLengthDialog(
    recommendedValue: Int,
    unitText: String,
    dontShowAgainChecked: Boolean,
    onDontShowAgainChange: (Boolean) -> Unit,
    onDismissClicked: () -> Unit,
    onStartTestClicked: () -> Unit,
    onEditHallwayClicked: () -> Unit,
) {
    Column(
        Modifier
            .wrapContentHeight()
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
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = false),
                        ) {
                            onDismissClicked()
                        },
                painter = painterResource(Res.drawable.ic_close),
                contentDescription = "",
            )
        }

        OSText(
            text = stringResource(Res.string.short_hallway_length_title),
            modifier =
                Modifier
                    .align(Alignment.CenterHorizontally),
            fontSize = 24.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        OSText(
            modifier =
                Modifier
                    .padding(Variables.GapL)
                    .align(Alignment.CenterHorizontally),
            text =
                stringResource(
                    Res.string.short_hallway_length_message,
                    recommendedValue,
                    unitText,
                ),
            fontSize = 18.sp,
            lineHeight = 24.sp,
            textAlign = TextAlign.Center,
        )

        SecondaryButton(
            text = stringResource(Res.string.short_hallway_start_test),
            onClick = onStartTestClicked,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Variables.GapL)
                .height(40.dp),
            size = OSButtonSize.Big,
        )
        PrimaryBrandButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Variables.GapL)
                .height(40.dp),
            data = PrimaryButtonData(
                text =
                    TextData(
                        stringResource(Res.string.short_hallway_edit_hallway_length),
                        18.sp,
                        FontWeight.Normal,
                    ),
                action = onEditHallwayClicked,
            ),
        )
        Row(
            Modifier
                .padding(horizontal = Variables.GapS)
                .padding(bottom = Variables.GapL)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    onDontShowAgainChange(!dontShowAgainChecked)
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = dontShowAgainChecked,
                onCheckedChange = onDontShowAgainChange,
                colors =
                    CheckboxDefaults.colors(
                        checkedColor = LocalOSColors.current.primary_p3_main,
                        uncheckedColor = LocalOSColors.current.neutral_p2,
                        checkmarkColor = LocalOSColors.current.neutral_m5,
                    ),
            )
            OSText(
                text = stringResource(Res.string.short_hallway_dont_show_again),
                fontSize = 14.sp,
            )
        }
    }
}

@Preview
@Composable
private fun HallwayDistanceScreenPreview() {
    PreviewTheme {
        HallwayDistanceScreen(
            state = previewHallwayValid,
            onValueChange = {},
            onContinue = {},
            onContinueWithoutLength = {},
        )
    }
}
