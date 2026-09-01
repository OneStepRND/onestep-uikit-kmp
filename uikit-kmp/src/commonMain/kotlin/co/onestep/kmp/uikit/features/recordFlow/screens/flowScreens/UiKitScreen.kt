package co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.recordFlow.components.MainIcon
import co.onestep.kmp.uikit.features.recordFlow.components.NoteBanner
import co.onestep.kmp.uikit.features.recordFlow.recordFlowStartRecordScreenData
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import co.onestep.kmp.uikit.features.recordFlow.components.RoundCtaButtonWithPulse
import co.onestep.kmp.uikit.features.recordFlow.components.UiKitFlowSelectionItem
import co.onestep.kmp.uikit.features.recordFlow.screensData.SelectionItemData
import co.onestep.kmp.uikit.features.recordFlow.screensData.UiKitScreenData
import co.onestep.kmp.uikit.testing.OSTTestTags
import co.onestep.kmp.uikit.ui.components.PrimaryBrandButton
import co.onestep.kmp.uikit.ui.components.FadingSurfaceToTransparent
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.components.SecondaryButton
import co.onestep.designsystem.components.OSButtonSize
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit.utils.test
import androidx.compose.material3.Icon
import org.jetbrains.compose.resources.painterResource

@Deprecated(
    "Moved to the OSTTestTags catalog",
    ReplaceWith("OSTTestTags.RecordFlow.PRIMARY_BUTTON", "co.onestep.kmp.uikit.testing.OSTTestTags"),
)
const val WALK_FLOW_SCREEN_BRAND_BUTTON = OSTTestTags.RecordFlow.PRIMARY_BUTTON

@Deprecated(
    "Moved to the OSTTestTags catalog",
    ReplaceWith("OSTTestTags.RecordFlow.SECONDARY_BUTTON", "co.onestep.kmp.uikit.testing.OSTTestTags"),
)
const val WALK_FLOW_SCREEN_BORDER_BRAND_BUTTON = OSTTestTags.RecordFlow.SECONDARY_BUTTON

@Deprecated(
    "Moved to the OSTTestTags catalog",
    ReplaceWith("OSTTestTags.RecordFlow.MAIN_BUTTON", "co.onestep.kmp.uikit.testing.OSTTestTags"),
)
const val WALK_FLOW_SCREEN_MAIN_BUTTON = OSTTestTags.RecordFlow.MAIN_BUTTON

@Composable
internal fun UiKitScreen(
    screenData: UiKitScreenData,
    modifier: Modifier = Modifier,
    screenTag: String? = null,
    playAudio: ((String) -> Unit)? = null,
    onBackPress: (() -> Unit)? = null,
) {
    val selectedItems =
        remember {
            screenData.selectionList
                ?.items
                ?.map { false }
                ?.toMutableStateList()
        }

    LaunchedEffect(Unit) {
        playAudio?.invoke(screenData.playAudioKey ?: return@LaunchedEffect)
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .let { if (screenTag != null) it.test(screenTag) else it },
    ) {
        // `modifier` is deliberately applied to both the Box and this Column (callers pass a
        // top padding they expect on both). `screenTag` is not: it stays on the Box alone, so
        // the screen has exactly one node carrying its test id.
        Column(modifier = modifier.fillMaxSize()) {
            screenData.noteBanner?.let {
                NoteBanner(
                    bannerData = it,
                    modifier = Modifier.fillMaxWidth().test(OSTTestTags.RecordFlow.NOTE_BANNER),
                )
            }
            screenData.mainIcon?.let {
                Spacer(modifier = Modifier.height(Variables.GapL))
                MainIcon(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .align(CenterHorizontally),
                    iconData = it,
                )
            }
            screenData.title?.let {
                Spacer(modifier = Modifier.height(Variables.GapXL))
                OSText(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .align(CenterHorizontally)
                            .padding(horizontal = Variables.GapL)
                            .test(OSTTestTags.RecordFlow.TITLE),
                    textAlign = it.textAlign ?: TextAlign.Center,
                    text = it.text,
                    lineHeight = 37.sp,
                    fontSize = it.textSize,
                    fontWeight = it.fontWeight,
                )
            }
            screenData.subtitle?.let {
                Spacer(modifier = Modifier.height(Variables.GapM))
                OSText(
                    modifier =
                        Modifier
                            .align(CenterHorizontally)
                            .padding(horizontal = 20.dp)
                            .verticalScroll(rememberScrollState())
                            .test(OSTTestTags.RecordFlow.SUBTITLE),
                    textAlign = it.textAlign ?: TextAlign.Center,
                    text = it.text,
                    lineHeight = 28.sp,
                    fontSize = it.textSize,
                    fontWeight = it.fontWeight,
                )
            }

            screenData.mainButton?.let {
                Spacer(modifier = Modifier.height(it.topSpace))
                RoundCtaButtonWithPulse(
                    modifier =
                        Modifier
                            .align(CenterHorizontally)
                            .size(it.buttonSize)
                            .test(OSTTestTags.RecordFlow.MAIN_BUTTON),
                    mainButtonData = it,
                )
            }

            screenData.selectionList?.let {
                SelectionList(
                    modifier =
                        if (it.isMultiSelect) {
                            Modifier.padding(bottom = 110.dp)
                        } else {
                            Modifier.padding(bottom = 0.dp)
                        },
                    it.items,
                    selectedItems,
                ) { selectionItemData ->
                    if (it.isMultiSelect) {
                        selectedItems?.set(selectionItemData, !selectedItems[selectionItemData])
                    } else {
                        it.onItemSelected(listOf(selectionItemData))
                    }
                }
            }
        }
        if (screenData.brandButton != null || screenData.outlineBrandButton != null) {
            Box(
                modifier =
                    Modifier
                        .background(Color.Transparent)
                        .align(Alignment.BottomCenter),
            ) {
                FadingSurfaceToTransparent(
                    Modifier.height(140.dp),
                    fadeColor = LocalOSColors.current.neutral_m5,
                )
                Column(
                    Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = Variables.GapL)
                        .align(Alignment.BottomCenter),
                ) {
                    screenData.brandButton?.let {
                        PrimaryBrandButton(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .test(OSTTestTags.RecordFlow.PRIMARY_BUTTON),
                            data =
                                it.copy(
                                    action = {
                                        it.action()
                                        if (screenData.selectionList?.isMultiSelect == true) {
                                            screenData.selectionList.onItemSelected(
                                                selectedItems?.mapIndexedNotNull { index, isSelected ->
                                                    if (isSelected) index else null
                                                } ?: emptyList(),
                                            )
                                        }
                                    },
                                ),
                        )
                        Spacer(Modifier.height(Variables.GapL))
                    }
                    screenData.outlineBrandButton?.let { secondary ->
                        // Figma shows a leading ⓘ on the secondary CTA ("View instructions").
                        // SecondaryButtonData carries it as a DrawableResource; the design-system
                        // button's `icon` is a composable slot, so render it here (inherits the
                        // button's content color).
                        val leadingIcon: (@Composable () -> Unit)? =
                            secondary.iconData?.let { iconData ->
                                {
                                    Icon(
                                        painter = painterResource(iconData.icon),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            }
                        SecondaryButton(
                            text = secondary.text.text,
                            onClick = secondary.action,
                            modifier = Modifier
                                .fillMaxWidth()
                                .test(OSTTestTags.RecordFlow.SECONDARY_BUTTON),
                            size = OSButtonSize.Big,
                            icon = leadingIcon,
                        )
                        Spacer(Modifier.height(Variables.GapXL))
                    }
                }
            }
        }
    }
}

@Composable
internal fun SelectionList(
    modifier: Modifier = Modifier,
    items: List<SelectionItemData>,
    selectedItems: SnapshotStateList<Boolean>? = null,
    onItemSelected: (Int) -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .verticalScroll(rememberScrollState())
                .padding(Variables.GapL),
    ) {
        items.forEachIndexed { index, data ->
            val selected = if (selectedItems != null) selectedItems[index] else false
            UiKitFlowSelectionItem(
                modifier =
                    Modifier
                        .align(CenterHorizontally)
                        .test(OSTTestTags.RecordFlow.selectionItem(index)),
                selected = selected,
                selectionItemData = data,
                onItemSelected = {
                    onItemSelected(index)
                },
            )
        }
    }
}

@Preview
@Composable
private fun UiKitScreenPreview() {
    PreviewTheme {
        UiKitScreen(screenData = recordFlowStartRecordScreenData)
    }
}
