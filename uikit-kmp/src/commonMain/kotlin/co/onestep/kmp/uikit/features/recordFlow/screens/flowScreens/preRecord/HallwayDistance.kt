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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.recordFlow.components.ToolBarHeight
import co.onestep.kmp.uikit.features.recordFlow.previewHallwayFresh
import co.onestep.kmp.uikit.features.recordFlow.previewHallwayValid
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import co.onestep.kmp.uikit.features.recordFlow.screensData.HallwayDistanceScreenState
import co.onestep.kmp.uikit.features.recordFlow.screensData.PrimaryButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData
import co.onestep.kmp.uikit.ui.components.KeyboardAware
import co.onestep.designsystem.components.OSText
import co.onestep.kmp.uikit.ui.components.PrimaryBrandButton
import co.onestep.designsystem.components.TertiaryButton
import co.onestep.designsystem.components.OSButtonSize
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit.ui.typography.NoirFontFamily
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.continue_camel_case
import co.onestep.kmp.uikit_kmp.generated.resources.continue_without_hallway_length
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
                    lineHeight = 36.sp,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )

                state.subtitle?.let { text ->
                    Spacer(modifier = Modifier.height(Variables.GapL))

                    OSText(
                        text = text,
                        fontSize = 16.sp,
                        color = LocalOSColors.current.neutral_p1,
                    )
                }

                Spacer(modifier = Modifier.height(64.dp))

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
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = Variables.GapL, vertical = Variables.GapXL),
                ) {
                    PrimaryBrandButton(
                        modifier = Modifier.fillMaxWidth(),
                        data =
                            PrimaryButtonData(
                                text =
                                    TextData(
                                        text = stringResource(Res.string.continue_camel_case),
                                        textSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                enabled = state.canContinue,
                                action = onContinue,
                            ),
                    )

                    if (!state.fromSummary) {
                        Spacer(Modifier.height(Variables.GapL))

                        TertiaryButton(
                            text = stringResource(Res.string.continue_without_hallway_length),
                            onClick = onContinueWithoutLength,
                            modifier = Modifier.fillMaxWidth(),
                            size = OSButtonSize.Big,
                        )
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

                Spacer(modifier = Modifier.width(Variables.GapM))

                OSText(
                    text = unitText,
                    fontSize = 18.sp,
                    color = LocalOSColors.current.neutral_p1,
                    modifier = Modifier.padding(bottom = Variables.GapL),
                )
            }
        }

        Spacer(modifier = Modifier.height(Variables.GapL))

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = LocalOSColors.current.neutral_0,
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

@Preview
@Composable
private fun HallwayDistanceScreenFreshPreview() {
    PreviewTheme {
        HallwayDistanceScreen(
            state = previewHallwayFresh,
            onValueChange = {},
            onContinue = {},
            onContinueWithoutLength = {},
        )
    }
}
