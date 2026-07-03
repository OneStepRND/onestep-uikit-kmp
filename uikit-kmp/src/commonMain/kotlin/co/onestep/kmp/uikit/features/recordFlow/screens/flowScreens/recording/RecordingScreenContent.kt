package co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.recording

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.onestep.kmp.uikit.bridge.RecorderBridge
import co.onestep.kmp.uikit.features.recordFlow.previewGetReadyRecordingScreen
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import co.onestep.kmp.uikit.features.recordFlow.screensData.RecordingScreenData
import co.onestep.kmp.uikit.models.OSTActivityType
import co.onestep.kmp.uikit.models.OSTAnalyserError
import co.onestep.kmp.uikit.models.OSTMotionMeasurement
import co.onestep.kmp.uikit.ui.components.AnimatedCounter
import co.onestep.designsystem.components.OSText
import co.onestep.kmp.uikit.ui.components.SlideToStopButton
import co.onestep.kmp.uikit.ui.components.WaterFillWave
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit.utils.PlatformBackHandler
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.data_is_ready_for_analysis
import co.onestep.kmp.uikit_kmp.generated.resources.slide_to_stop
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RecordingScreenContent(
    modifier: Modifier = Modifier,
    viewModel: MotionRecorderViewModel,
    onMeasurementResult: (OSTMotionMeasurement?) -> Unit,
    onBackPress: () -> Unit,
    onRecording: () -> Unit,
    onError: (OSTAnalyserError, OSTActivityType?) -> Unit,
) {
    val screenData = viewModel.recodingScreenState
    val stepCount by viewModel.stepCount.collectAsStateWithLifecycle(0)

    LaunchedEffect(Unit) {
        viewModel.initState()
        viewModel.onMeasurementResult = onMeasurementResult
        viewModel.onError = onError
    }

    LaunchedEffect(viewModel.recodingScreenState.value) {
        if (viewModel.recodingScreenState.value.recordScreenStage != RecordingScreenData.RecordScreenStage.GET_READY) {
            onRecording()
        }
    }

    PlatformBackHandler { onBackPress() }

    AnimatedContent(
        modifier = modifier,
        targetState = screenData.value,
        transitionSpec = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
            ) { -it } togetherWith
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                ) { it }
        },
        label = "",
    ) { currentScreenData ->
        RecordingScreenStateless(
            modifier = Modifier,
            screenData = currentScreenData,
            subtitle = viewModel.subtitle.value ?: currentScreenData.instructions.text,
            stepCount = stepCount,
            timerValue = viewModel.timerValue.value,
            onStopped = { viewModel.stopRecording() },
        )
    }
}

@Composable
internal fun RecordingScreenStateless(
    modifier: Modifier = Modifier,
    screenData: RecordingScreenData,
    subtitle: String,
    stepCount: Int,
    timerValue: String,
    onStopped: () -> Unit,
) {
    val colors = LocalOSColors.current

    val targetPercent =
        (stepCount.coerceAtMost(RecorderBridge.MIN_STEPS_FOR_ANALYSIS) * 100f) / RecorderBridge.MIN_STEPS_FOR_ANALYSIS
    val animatedPercent by animateFloatAsState(
        targetValue = targetPercent,
        animationSpec = if (targetPercent == 100f) tween(0) else tween(600),
        label = "stepsFill",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.neutral_m4),
    ) {
        // Title centered in the top white area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.25f)
                .align(Alignment.TopCenter),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                modifier = Modifier
                    .padding(horizontal = Variables.GapL),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                targetState = stepCount > RecorderBridge.MIN_STEPS_FOR_ANALYSIS,
            ) { dataIsReadyForAnalysis ->
                OSText(
                    text = if (dataIsReadyForAnalysis) stringResource(Res.string.data_is_ready_for_analysis) else screenData.title.text,
                    color = if (dataIsReadyForAnalysis) colors.success_p3 else screenData.colorTheme,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    lineHeight = 62.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = screenData.title.fontWeight,
                    fontSize = if (dataIsReadyForAnalysis) 48.sp else screenData.title.textSize,
                )
            }
        }

        // Colored bottom area with all content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .align(Alignment.BottomCenter)
                .background(screenData.colorTheme),
        ) {
            // Water fill wave during recording
            if (screenData.recordScreenStage.isRecording()) {
                WaterFillWave(
                    modifier = Modifier.fillMaxSize(),
                    percent = animatedPercent,
                    color = colors.success_p3,
                    isFlat = true,
                )
            }

            // Instructions and timer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = Variables.GapL)
                    .padding(top = Variables.GapXXL),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AnimatedContent(
                    targetState = subtitle,
                    label = "recording screen instructions",
                ) { instructionsText ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 550.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        OSText(
                            text = instructionsText,
                            fontSize = screenData.instructions.textSize,
                            lineHeight = 36.sp,
                            fontWeight = screenData.instructions.fontWeight,
                            textAlign = TextAlign.Center,
                            color = colors.neutral_m5,
                        )
                    }
                }

                // Timer
                if (timerValue.isNotEmpty() && screenData.timerValue != null) {
                    Spacer(Modifier.height(48.dp))
                    AnimatedVisibility(
                        visible = timerValue != "0",
                        exit = fadeOut(tween(delayMillis = 600)),
                    ) {
                        AnimatedCounter(
                            modifier = Modifier.animateContentSize(),
                            text = timerValue,
                            textData = screenData.timerValue.text,
                        )
                    }
                }

                // Value display
                screenData.value?.let {
                    Spacer(Modifier.height(32.dp))
                    OSText(
                        text = it.text,
                        fontSize = it.textSize,
                        lineHeight = 37.sp,
                        fontWeight = it.fontWeight,
                        textAlign = TextAlign.Center,
                        color = colors.neutral_m5,
                    )
                }
            }

            // Analyzing loader
            AnimatedVisibility(
                modifier = Modifier.align(Alignment.Center).offset(y = 50.dp),
                visible = screenData.recordScreenStage.isAnalyzing(),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(100.dp),
                    strokeWidth = 10.dp,
                    color = colors.neutral_m5,
                )
            }

            // Slide to stop button at bottom
            screenData.slideToStopButton?.let {
                SlideToStopButton(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = Variables.GapL)
                        .padding(bottom = Variables.GapL),
                    trackTextSize = it.textData?.textSize ?: 20.sp,
                    trackText = it.textData?.text ?: stringResource(Res.string.slide_to_stop),
                    trackColor = colors.error_p2,
                    trackTextColor = colors.neutral_m5,
                    thumbSizeDp = 60.dp,
                    thumbColor = colors.neutral_m5,
                ) {
                    onStopped()
                }
            }

            // Bottom outline button (Start now) — white 1px outline on the colored area,
            // radius 4, play icon + label. Bespoke because the design-system SecondaryButton
            // is a dark-on-light outline and can't render this white-on-color variant.
            screenData.bottomButton?.let { button ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = Variables.GapL)
                        .padding(bottom = Variables.GapL)
                        .height(60.dp)
                        .clip(RoundedCornerShape(Variables.RadiusR4))
                        .border(
                            width = 1.dp,
                            color = colors.neutral_m5,
                            shape = RoundedCornerShape(Variables.RadiusR4),
                        )
                        .clickable { button.action() },
                ) {
                    button.iconData?.let { icon ->
                        Icon(
                            painter = painterResource(icon.icon),
                            contentDescription = null,
                            tint = icon.tintColor ?: colors.neutral_m5,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(Modifier.width(Variables.GapS))
                    }
                    OSText(
                        text = button.text.text,
                        fontSize = button.text.textSize,
                        fontWeight = button.text.fontWeight,
                        color = colors.neutral_m5,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun RecordingScreenStatelessPreview() {
    PreviewTheme {
        RecordingScreenStateless(
            screenData = previewGetReadyRecordingScreen,
            subtitle = previewGetReadyRecordingScreen.instructions.text,
            stepCount = 0,
            timerValue = "10",
            onStopped = {},
        )
    }
}
