package co.onestep.kmp.uikit.features.summary.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import co.onestep.kmp.uikit.ui.theme.osClickIndication
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.cd_pager_tab
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun OSTTabsRow(
    tabBarOffsetY: Dp = 0.dp,
    selectedTabIndex: Int,
    tabs: List<OSTTabData>?,
    containerColor: Color? = null,
    onTabSelected: (Int) -> Unit,
    textSize: TextUnit = 14.sp,
) {
    TabRow(
        modifier =
            Modifier
                .offset(y = tabBarOffsetY),
        containerColor = containerColor ?: LocalOSColors.current.neutral_m4,
        contentColor = LocalOSColors.current.neutral_p3,
        selectedTabIndex = selectedTabIndex,
        indicator = @Composable { tabPositions ->
            if (selectedTabIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = LocalOSColors.current.neutral_p3,
                    height = 5.dp,
                )
            }
        },
        divider = {
            HorizontalDivider(
                modifier =
                    Modifier
                        .graphicsLayer { translationY = -1.5f },
                color = LocalOSColors.current.neutral_p2,
                thickness = 3.dp,
            )
        },
    ) {
        tabs?.forEachIndexed { index, tab ->
            OSTTab(onTabSelected, index, tab, selectedTabIndex, textSize)
        }
    }
}

@Preview
@Composable
private fun OSTTabsRowPreview() {
    PreviewTheme {
        OSTTabsRow(
            selectedTabIndex = 0,
            tabs = listOf(
                OSTTabData("Highlights", 0),
                OSTTabData("Gait Lab", 1),
            ),
            onTabSelected = {},
        )
    }
}

@Composable
private fun OSTTab(
    onTabSelected: (Int) -> Unit,
    index: Int,
    tab: OSTTabData,
    selectedTabIndex: Int,
    textSize: TextUnit = 14.sp,
) {
    Column(
        modifier =
            Modifier
                .fillMaxHeight()
                .clip(RoundedCornerShape(4.dp))
                .clickable(
                    interactionSource =
                        remember {
                            MutableInteractionSource()
                        },
                    indication = osClickIndication(bounded = true),
                    onClick = { onTabSelected(index) },
                ).padding(Variables.GapL),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        tab.icon?.let { iconRes ->
            AnimatedContent(
                targetState = selectedTabIndex,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
            ) { selected ->
                Icon(
                    painter =
                        painterResource(
                            if (index == selected) {
                                tab.selectedIcon ?: iconRes
                            } else {
                                iconRes
                            },
                        ),
                    tint =
                        if (index == selected) {
                            LocalOSColors.current.primary_p1
                        } else {
                            LocalOSColors.current.primary_m1
                        },
                    contentDescription = stringResource(Res.string.cd_pager_tab),
                )
            }
        }
        OSText(
            text = tab.title,
            fontSize = textSize,
            color =
                if (index == selectedTabIndex) {
                    LocalOSColors.current.neutral_p3
                } else {
                    LocalOSColors.current.neutral_p2
                },
        )
    }
}
