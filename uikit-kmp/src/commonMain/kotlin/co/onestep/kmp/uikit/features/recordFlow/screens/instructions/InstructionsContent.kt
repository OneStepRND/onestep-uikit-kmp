package co.onestep.kmp.uikit.features.recordFlow.screens.instructions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.bridge.PlatformGifImage
import co.onestep.kmp.uikit.bridge.PlatformVideoPlayer
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTMeasurementInstructionsData
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import co.onestep.kmp.uikit.ui.components.BulletPointText
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.cd_walk_instruction_media
import co.onestep.kmp.uikit_kmp.generated.resources.hints
import co.onestep.kmp.uikit_kmp.generated.resources.instructions
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun InstructionsContent(instructionsData: OSTMeasurementInstructionsData) {
    val backgroundColor = LocalOSColors.current.neutral_m5

    // Consume leftover upward scroll/fling so the ModalBottomSheet's AnchoredDraggable
    // doesn't overscroll past the Expanded anchor (causes jitter on some Foundation versions).
    // Downward deltas pass through so dismiss-by-drag still works.
    val sheetScrollFix = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset = if (available.y < 0f) available else Offset.Zero

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity,
            ): Velocity = if (available.y < 0f) available else Velocity.Zero
        }
    }

    Column(
        modifier = Modifier
            .nestedScroll(sheetScrollFix)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .background(backgroundColor),
    ) {
        // Media block: GIF URL > GIF resource > video URL
        // Resolve gifResourceKey to a platform URI if needed
        var resolvedGifResourceUri by remember { mutableStateOf<String?>(null) }
        if (instructionsData.gifResourceKey != null) {
            LaunchedEffect(instructionsData.gifResourceKey) {
                resolvedGifResourceUri = Res.getUri("files/${instructionsData.gifResourceKey}.gif")
            }
        }

        val gifUri = instructionsData.gifUrl ?: resolvedGifResourceUri
        val hasMedia = gifUri != null || instructionsData.videoUrl != null
        if (hasMedia) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Variables.GapXXL)
                    .aspectRatio(16f / 9f),
            ) {
                when {
                    gifUri != null -> {
                        PlatformGifImage(
                            url = gifUri,
                            contentDescription = stringResource(Res.string.cd_walk_instruction_media),
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(Variables.GapL)),
                        )
                    }
                    instructionsData.videoUrl != null -> {
                        PlatformVideoPlayer(
                            url = instructionsData.videoUrl!!,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(Variables.GapL)),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Variables.GapL))

        OSText(
            modifier = Modifier.padding(20.dp),
            text = instructionsData.activityDisplayName,
            fontWeight = FontWeight.W700,
            fontSize = 24.sp,
            lineHeight = 28.sp,
        )

        OSText(
            modifier = Modifier.padding(horizontal = 20.dp),
            text = stringResource(Res.string.instructions),
            fontWeight = FontWeight.W700,
            fontSize = 20.sp,
            lineHeight = 28.sp,
        )

        instructionsData.instructions.forEach {
            BulletPointText(
                modifier = Modifier.padding(horizontal = 20.dp),
                textSize = 28.sp,
                text = {
                    OSText(
                        text = it,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Normal,
                    )
                },
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (instructionsData.hints.isNotEmpty()) {
            OSText(
                modifier = Modifier.padding(horizontal = 20.dp),
                text = stringResource(Res.string.hints),
                fontWeight = FontWeight.W700,
                fontSize = 20.sp,
                lineHeight = 28.sp,
            )
            instructionsData.hints.forEach {
                BulletPointText(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    textSize = 28.sp,
                    text = {
                        OSText(
                            text = it,
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.Normal,
                        )
                    },
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Preview
@Composable
private fun InstructionsContentPreview() {
    PreviewTheme {
        InstructionsContent(
            instructionsData = OSTMeasurementInstructionsData(
                activityDisplayName = "6-Minute Walk Test",
                instructions = listOf(
                    "Walk at your normal pace",
                    "You may use your walking aid if needed",
                ),
                hints = listOf("Wear comfortable shoes"),
            ),
        )
    }
}
