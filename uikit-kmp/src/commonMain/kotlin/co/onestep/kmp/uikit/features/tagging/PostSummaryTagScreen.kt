package co.onestep.kmp.uikit.features.tagging

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import co.onestep.kmp.uikit.models.displayNameRes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.models.OSTAssistiveDevice
import co.onestep.kmp.uikit.models.OSTLevelOfAssistance
import co.onestep.kmp.uikit.models.OSTUserInputMetaData
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTPostTaggingData
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingQuestionData
import co.onestep.kmp.uikit.features.recordFlow.screensData.PrimaryButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData
import co.onestep.kmp.uikit.features.tagging.models.Footwear
import co.onestep.kmp.uikit.ui.components.FadingSurfaceToTransparent
import co.onestep.kmp.uikit.ui.components.KeyboardAware
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.components.SecondaryButton
import co.onestep.designsystem.components.OSButtonSize
import co.onestep.kmp.uikit.ui.components.PrimaryBrandButton
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.designsystem.theme.Variables
import org.jetbrains.compose.resources.StringResource
import co.onestep.kmp.uikit.utils.test
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.add_a_note_about_the_measurement
import co.onestep.kmp.uikit_kmp.generated.resources.assistive_device
import co.onestep.kmp.uikit_kmp.generated.resources.edit
import co.onestep.kmp.uikit_kmp.generated.resources.finish
import co.onestep.kmp.uikit_kmp.generated.resources.footwear
import co.onestep.kmp.uikit_kmp.generated.resources.ic_edit
import co.onestep.kmp.uikit_kmp.generated.resources.level_of_assistance
import co.onestep.kmp.uikit_kmp.generated.resources.note_hint_text
import co.onestep.kmp.uikit_kmp.generated.resources.review_the_following_tags
import kotlinx.coroutines.launch

const val TAG_SCREEN_NOTE_TEXT_FIELD = "tag_screen_note_text_field"
const val TAG_SCREEN_ASSISTIVE_DEVICE_EDIT_BUTTON = "tag_screen_assistive_device_edit_button"
const val TAG_SCREEN_LEVEL_OF_ASSISTANCE_EDIT_BUTTON = "tag_screen_level_of_assistance_edit_button"
const val TAG_SCREEN_FOOTWEAR_EDIT_BUTTON = "tag_screen_footwear_edit_button"
const val TAG_SCREEN_ASSISTIVE_DEVICE_TEXT = "tag_screen_assistive_device_text"
const val TAG_SCREEN_LEVEL_OF_ASSISTANCE_TEXT = "tag_screen_level_of_assistance_text"
const val TAG_SCREEN_FOOTWEAR_TEXT = "tag_screen_footwear_text"
const val TAG_SCREEN_MAIN_BUTTON = "tag_screen_main_button"
const val BLANK_INPUT = "--"

@Composable
internal fun PostSummaryTagScreen(
    modifier: Modifier = Modifier,
    postTaggingData: OSTPostTaggingData.OSTPostTaggingScreen?,
    onEditAssistiveDeviceClicked: (String?) -> Unit = {},
    onEditLevelOfAssistanceClicked: (String?) -> Unit = {},
    onEditFootwearClicked: (String?) -> Unit = {},
    onGoToQuestionsClicked: (String?, OSTRecordingQuestionData) -> Unit = { _, _ -> },
    assistiveDevice: OSTAssistiveDevice? = null,
    levelOfAssistance: OSTLevelOfAssistance? = null,
    footwear: Footwear? = null,
    note: String? = null,
    action: (OSTUserInputMetaData) -> Unit = {},
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val keyboardHeight = WindowInsets.ime.getBottom(LocalDensity.current)
    val userInputMetaData by remember { mutableStateOf(OSTUserInputMetaData()) }
    val newNote = remember { mutableStateOf<String?>(note) }

    // Pre-compute footwear display name at composable level for use in non-composable lambda
    val footwearDisplayName = if (footwear != null && footwear != Footwear.NONE) footwear.displayName() else null

    val noteOnly =
        postTaggingData?.assistiveDeviceTag != true &&
            postTaggingData?.levelOfAssistanceTag != true &&
            postTaggingData?.footwearTag != true &&
            postTaggingData?.questions.isNullOrEmpty()
    val title =
        if (noteOnly) {
            stringResource(Res.string.add_a_note_about_the_measurement)
        } else {
            stringResource(Res.string.review_the_following_tags)
        }

    LaunchedEffect(keyboardHeight) {
        coroutineScope.launch {
            scrollState.scrollBy(keyboardHeight.toFloat())
        }
    }

    KeyboardAware {
        Box(
            modifier
                .fillMaxSize(),
        ) {
            Column(
                Modifier
                    .wrapContentHeight()
                    .verticalScroll(scrollState),
            ) {
                OSText(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(Variables.GapL),
                    text = title,
                    fontSize = 28.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.W700,
                    textAlign = TextAlign.Start,
                )
                if (!noteOnly) {
                    HorizontalDivider(
                        modifier =
                            Modifier
                                .fillMaxWidth(),
                    )
                }
                if (postTaggingData?.levelOfAssistanceTag == true) {
                    LevelOfAssistanceQuestion(levelOfAssistance) {
                        onEditLevelOfAssistanceClicked(newNote.value)
                    }
                }
                if (postTaggingData?.assistiveDeviceTag == true) {
                    AssistiveDeviceQuestion(assistiveDevice) {
                        onEditAssistiveDeviceClicked(newNote.value)
                    }
                }
                if (postTaggingData?.footwearTag == true) {
                    PostRecordingQuestion(
                        OSTRecordingQuestionData(
                            stringResource(Res.string.footwear),
                            description = stringResource(Res.string.footwear),
                            listOf(
                                footwear?.displayName()
                                    ?: BLANK_INPUT,
                            ),
                            isMultiSelect = false,
                        ),
                        selectedAnswer = footwear?.displayName(),
                        onGoToQuestionClicked = {
                            onEditFootwearClicked(newNote.value)
                        },
                    )
                }
                postTaggingData?.questions?.forEach { question ->
                    PostRecordingQuestion(
                        questionData = question,
                        selectedAnswer = question.selectedAnswers?.first(),
                        onGoToQuestionClicked = {
                            onGoToQuestionsClicked(
                                newNote.value,
                                question,
                            )
                        },
                    )
                }
                Spacer(modifier = Modifier.height(if (noteOnly) 0.dp else Variables.GapL))

                if (postTaggingData?.note == true) {
                    if (!noteOnly) {
                        OSText(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                            text = stringResource(Res.string.add_a_note_about_the_measurement),
                            fontSize = 18.sp,
                            lineHeight = 25.sp,
                            fontWeight = FontWeight.W400,
                        )
                    }
                    CustomTextField(
                        value = newNote.value,
                        onValueChange = { newNote.value = it },
                    )
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
            Box(
                modifier =
                    Modifier
                        .align(BottomCenter)
                        .background(Color.Transparent),
            ) {
                FadingSurfaceToTransparent(
                    Modifier.height(110.dp),
                    fadeColor = LocalOSColors.current.neutral_m4,
                )
                PrimaryBrandButton(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .align(BottomCenter)
                            .padding(Variables.GapL)
                            .test(TAG_SCREEN_MAIN_BUTTON),
                    data =
                        PrimaryButtonData(
                            text =
                                TextData(
                                    stringResource(Res.string.finish),
                                    24.sp,
                                    fontWeight = FontWeight.W600,
                                ),
                            action = {
                                val tags =
                                    mutableListOf<String>().apply {
                                        footwearDisplayName?.let { add(it) }
                                        addAll(
                                            postTaggingData
                                                ?.questions
                                                ?.mapNotNull { it.selectedAnswers }
                                                ?.flatten()
                                                .orEmpty(),
                                        )
                                    }

                                action(
                                    userInputMetaData.copy(
                                        assistiveDevice = assistiveDevice,
                                        levelOfAssistance = levelOfAssistance,
                                        tags = tags,
                                        note = newNote.value,
                                    ),
                                )
                            },
                        ),
                )
            }
        }
    }
}

@Composable
private fun AssistiveDeviceQuestion(
    assistiveDevice: OSTAssistiveDevice?,
    onEditAssistiveDeviceClicked: () -> Unit,
) = TagQuestionRow(
    labelRes = Res.string.assistive_device,
    valueText = assistiveDevice?.displayNameRes?.let { stringResource(it) } ?: BLANK_INPUT,
    editButtonTestTag = TAG_SCREEN_ASSISTIVE_DEVICE_EDIT_BUTTON,
    valueTestTag = TAG_SCREEN_ASSISTIVE_DEVICE_TEXT,
    onEditClicked = onEditAssistiveDeviceClicked,
)

@Composable
private fun LevelOfAssistanceQuestion(
    levelOfAssistance: OSTLevelOfAssistance?,
    onEditLevelOfAssistanceClicked: () -> Unit,
) = TagQuestionRow(
    labelRes = Res.string.level_of_assistance,
    valueText = levelOfAssistance?.displayNameRes?.let { stringResource(it) } ?: BLANK_INPUT,
    editButtonTestTag = TAG_SCREEN_LEVEL_OF_ASSISTANCE_EDIT_BUTTON,
    valueTestTag = TAG_SCREEN_LEVEL_OF_ASSISTANCE_TEXT,
    onEditClicked = onEditLevelOfAssistanceClicked,
)

@Composable
private fun TagQuestionRow(
    labelRes: StringResource,
    valueText: String,
    editButtonTestTag: String,
    valueTestTag: String,
    onEditClicked: () -> Unit,
) {
    Column(
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(bounded = true),
            onClick = onEditClicked,
        ),
    ) {
        Spacer(modifier = Modifier.height(Variables.GapL))
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            OSText(
                modifier =
                    Modifier
                        .align(CenterVertically)
                        .weight(1f),
                text = stringResource(labelRes),
                fontSize = 18.sp,
                maxLines = 2,
                fontWeight = FontWeight.W400,
                textAlign = TextAlign.Start,
            )
            Spacer(Modifier.width(Variables.GapL))
            SecondaryButton(
                text = stringResource(Res.string.edit),
                onClick = onEditClicked,
                modifier = Modifier
                    .wrapContentWidth()
                    .height(40.dp)
                    .test(editButtonTestTag),
                size = OSButtonSize.Small,
                icon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_edit),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                },
            )
        }
        Spacer(modifier = Modifier.height(Variables.GapL))
        OSText(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .test(valueTestTag),
            text = valueText,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.W600,
        )
        Spacer(modifier = Modifier.height(Variables.GapL))
    }

    HorizontalDivider(modifier = Modifier.fillMaxWidth())
}

@Composable
private fun PostRecordingQuestion(
    questionData: OSTRecordingQuestionData,
    selectedAnswer: String? = null,
    onGoToQuestionClicked: (OSTRecordingQuestionData?) -> Unit = {},
) {
    Column(
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(bounded = true),
            onClick = { onGoToQuestionClicked(questionData) },
        ),
    ) {
        Spacer(modifier = Modifier.height(Variables.GapL))
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            OSText(
                modifier =
                    Modifier
                        .align(CenterVertically)
                        .weight(1f),
                text = questionData.description?.let { "$it: " }.orEmpty(),
                fontSize = 18.sp,
                fontWeight = FontWeight.W400,
                textAlign = TextAlign.Start,
            )
            SecondaryButton(
                text = stringResource(Res.string.edit),
                onClick = { onGoToQuestionClicked(questionData) },
                modifier = Modifier
                    .wrapContentWidth()
                    .height(40.dp)
                    .test(TAG_SCREEN_FOOTWEAR_EDIT_BUTTON),
                size = OSButtonSize.Small,
                icon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_edit),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                },
            )
        }
        Spacer(modifier = Modifier.height(Variables.GapL))
        OSText(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .test(TAG_SCREEN_FOOTWEAR_TEXT),
            text = selectedAnswer ?: BLANK_INPUT,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.W600,
        )
        Spacer(modifier = Modifier.height(Variables.GapL))
    }

    HorizontalDivider(
        modifier =
            Modifier
                .fillMaxWidth(),
    )
}

@Composable
internal fun CustomTextField(
    value: String?,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier.padding(Variables.GapL),
    hintRes: StringResource = Res.string.note_hint_text,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier =
            modifier
                .height(250.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(5.dp))
                .background(
                    shape = RoundedCornerShape(5.dp),
                    color = LocalOSColors.current.neutral_m3,
                ).clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true),
                    onClick = {
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    },
                ),
        contentAlignment = Alignment.TopStart,
    ) {
        AnimatedVisibility(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(18.dp),
            enter = fadeIn(),
            exit = fadeOut(),
            visible = value.isNullOrEmpty(),
        ) {
            OSText(
                text = stringResource(hintRes),
                fontSize = 16.sp,
                color = LocalOSColors.current.neutral_p1,
                lineHeight = 22.sp,
            )
        }

        BasicTextField(
            value = value.orEmpty(),
            onValueChange = onValueChange,
            singleLine = false,
            textStyle =
                TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    color = LocalOSColors.current.neutral_p3,
                ),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            keyboardController?.hide()
                        }
                    }.test(TAG_SCREEN_NOTE_TEXT_FIELD),
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text,
                ),
            keyboardActions =
                KeyboardActions(onDone = {
                    keyboardController?.hide()
                    focusRequester.freeFocus()
                }),
        )
    }
}

