package co.onestep.kmp.uikit.features.recordFlow.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import co.onestep.kmp.uikit.ui.theme.osClickIndication
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import co.onestep.kmp.uikit.features.recordFlow.toolbarData
import co.onestep.kmp.uikit.features.recordFlow.screensData.ToolBarData
import co.onestep.kmp.uikit.testing.OSTTestTags
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.utils.rtlMirror
import co.onestep.kmp.uikit.utils.test
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

const val ToolBarHeight = 60

@Deprecated(
    "Moved to the OSTTestTags catalog",
    ReplaceWith("OSTTestTags.RecordFlow.TOOLBAR", "co.onestep.kmp.uikit.testing.OSTTestTags"),
)
const val TOOLBAR = OSTTestTags.RecordFlow.TOOLBAR

@Deprecated(
    "Moved to the OSTTestTags catalog",
    ReplaceWith("OSTTestTags.RecordFlow.TOOLBAR_START_ICON", "co.onestep.kmp.uikit.testing.OSTTestTags"),
)
const val TOOLBAR_START_ICON = OSTTestTags.RecordFlow.TOOLBAR_START_ICON

@Deprecated(
    "Moved to the OSTTestTags catalog",
    ReplaceWith("OSTTestTags.RecordFlow.TOOLBAR_END_ICON", "co.onestep.kmp.uikit.testing.OSTTestTags"),
)
const val TOOLBAR_END_ICON = OSTTestTags.RecordFlow.TOOLBAR_END_ICON

data class ToolBarColors(
    val containerColor: Color,
    val contentColor: Color,
)

@Composable
fun Toolbar(
    modifier: Modifier = Modifier,
    toolbarData: ToolBarData,
    toolBarColor: ToolBarColors? = null,
) {
    val hasStartIcon = toolbarData.startIcon != null
    val endIconCount = toolbarData.endIcons?.size ?: 0
    val startIconWidth = 60.dp
    val endIconWidth = if (endIconCount > 0) (endIconCount * 56 + 16).dp else 0.dp
    val defaultPadding = 16.dp

    val (startPadding, endPadding) =
        when {
            hasStartIcon && endIconCount > 0 -> startIconWidth to endIconWidth
            hasStartIcon -> startIconWidth to startIconWidth
            !hasStartIcon && endIconCount > 0 -> endIconWidth to endIconWidth
            else -> defaultPadding to defaultPadding
        }

    Box(
        Modifier
            .fillMaxWidth()
            .background(toolBarColor?.containerColor ?: LocalOSColors.current.neutral_m4)
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(ToolBarHeight.dp)
            .test(OSTTestTags.RecordFlow.TOOLBAR)
            .then(modifier),
    ) {
        toolbarData.startIcon?.let {
            Icon(
                painter = painterResource(it.icon),
                contentDescription = null,
                tint = toolBarColor?.contentColor ?: LocalOSColors.current.neutral_p3,
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource =
                                remember {
                                    MutableInteractionSource()
                                },
                            indication = osClickIndication(bounded = false),
                        ) {
                            it.action?.invoke()
                        }.size(36.dp)
                        .rtlMirror()
                        .test(OSTTestTags.RecordFlow.TOOLBAR_START_ICON),
            )
        }
        AnimatedVisibility(
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth()
                    .padding(
                        start = startPadding,
                        end = endPadding,
                    ),
            visible = toolbarData.title != null,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            toolbarData.title?.let {
                OSText(
                    text = it.text,
                    fontSize = it.textSize,
                    fontWeight = it.fontWeight,
                    color = toolBarColor?.contentColor ?: LocalOSColors.current.neutral_p3,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        toolbarData.endIcons?.let {
            Row(
                modifier =
                    Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 0.dp),
            ) {
                it.forEachIndexed { index, iconData ->
                    Icon(
                        painter = painterResource(iconData.icon),
                        contentDescription = null,
                        tint = toolBarColor?.contentColor ?: LocalOSColors.current.neutral_p3,
                        modifier =
                            Modifier
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource =
                                        remember {
                                            MutableInteractionSource()
                                        },
                                    indication = osClickIndication(bounded = false),
                                ) {
                                    iconData.action?.invoke()
                                }.padding(10.dp)
                                .size(36.dp)
                                .test(OSTTestTags.RecordFlow.toolbarEndIcon(index)),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ToolbarPreview() {
    PreviewTheme {
        Toolbar(toolbarData = toolbarData)
    }
}
