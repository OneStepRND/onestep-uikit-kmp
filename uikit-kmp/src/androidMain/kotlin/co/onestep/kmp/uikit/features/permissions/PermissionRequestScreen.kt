package co.onestep.kmp.uikit.features.permissions

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.recordFlow.components.Toolbar
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.WALK_FLOW_SCREEN_BORDER_BRAND_BUTTON
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.WALK_FLOW_SCREEN_BRAND_BUTTON
import co.onestep.kmp.uikit.ui.components.BottomSheet
import co.onestep.kmp.uikit.ui.components.FadingSurfaceToTransparent
import co.onestep.kmp.uikit.ui.components.Instructions
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.components.OSButtonSize
import co.onestep.designsystem.components.TertiaryButton
import co.onestep.kmp.uikit.ui.components.PrimaryBrandButton
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit.utils.test
import co.onestep.kmp.uikit.features.recordFlow.screensData.PrimaryButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.cd_permissions_icon
import co.onestep.kmp.uikit_kmp.generated.resources.data_usage_explanation
import co.onestep.kmp.uikit_kmp.generated.resources.ic_close
import co.onestep.kmp.uikit_kmp.generated.resources.ic_walks
import co.onestep.kmp.uikit_kmp.generated.resources.your_data_is_safe_with_us
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import co.onestep.kmp.uikit.features.recordFlow.screensData.IconData

const val PERMISSION_REQUEST_BUTTON = "Permission request button"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PermissionRequestScreen(
    screenData: PermissionScreenData,
    modifier: Modifier = Modifier,
    showInfo: Boolean = false,
    onInfoToggle: ((Boolean) -> Unit)? = null,
    onBackPress: (() -> Unit)? = null,
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    BackHandler {
        onBackPress?.invoke()
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(LocalOSColors.current.neutral_m4),
    ) {
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(bottom = 70.dp),
        ) {
            screenData.toolBarData?.let {
                Toolbar(toolbarData = screenData.toolBarData)
                Spacer(modifier = Modifier.height(25.dp))
            }
            screenData.mainIcon?.let {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .align(CenterHorizontally)
                        .clickable { it.action?.invoke() }
                        .padding(horizontal = 20.dp),
                ) {
                    Image(
                        modifier =
                            Modifier
                                .size(105.dp)
                                .align(Alignment.Center),
                        painter = painterResource(it.icon),
                        contentDescription = stringResource(Res.string.cd_permissions_icon),
                    )
                }
            }
            screenData.title?.let {
                Spacer(modifier = Modifier.height(Variables.GapL))
                OSText(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                    textAlign = TextAlign.Center,
                    text = it.text,
                    color = it.color ?: LocalOSColors.current.primary_p3_main,
                    lineHeight = 42.sp,
                    fontSize = it.textSize,
                    fontWeight = it.fontWeight,
                    letterSpacing = 0.sp,
                )
            }
            Column(
                modifier =
                    Modifier
                        .padding(bottom = 50.dp)
                        .verticalScroll(rememberScrollState()),
            ) {
                screenData.content?.let {
                    Spacer(modifier = Modifier.height(20.dp))

                    Instructions(
                        content = it,
                        defaultStyle =
                            TextStyle(
                                fontSize = 18.sp,
                                lineHeight = 28.sp,
                                textAlign = TextAlign.Center,
                                color = LocalOSColors.current.neutral_p2,
                            ),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Variables.GapL),
                    )
                }

                screenData.customContent?.let {
                    Spacer(modifier = Modifier.height(20.dp))
                    Card(
                        elevation =
                            CardDefaults.cardElevation(
                                defaultElevation = Variables.GapL,
                            ),
                        modifier =
                            Modifier
                                .align(CenterHorizontally)
                                .padding(horizontal = 20.dp),
                    ) {
                        it()
                    }
                }
                Spacer(modifier = Modifier.height(50.dp))
            }
        }

        if (screenData.brandButton != null || screenData.tertiaryButton != null) {
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
                        .padding(horizontal = 20.dp)
                        .align(Alignment.BottomCenter),
                ) {
                    screenData.brandButton?.let {
                        PrimaryBrandButton(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .test(WALK_FLOW_SCREEN_BRAND_BUTTON),
                            data =
                                it.copy(
                                    action = {
                                        it.action()
                                    },
                                ),
                        )
                        Spacer(Modifier.height(Variables.GapL))
                    }
                    screenData.tertiaryButton?.let {
                        TertiaryButton(
                            text = it.text.text,
                            onClick = { it.action(); onInfoToggle?.invoke(!showInfo) },
                            modifier = Modifier.fillMaxWidth().testTag(WALK_FLOW_SCREEN_BORDER_BRAND_BUTTON),
                            size = OSButtonSize.Big,
                        )
                        Spacer(Modifier.height(Variables.GapL))
                    }
                }
            }
        }

        val scope = rememberCoroutineScope()

        val dismissBottomSheet = {
            scope.launch {
                bottomSheetState.hide()
                onInfoToggle?.invoke(false)
            }
            Unit
        }

        if (showInfo) {
            BottomSheet(
                sheetState = bottomSheetState,
                onDismissRequest = dismissBottomSheet,
                dragHandle = null,
            ) {
                DataUsageNotice(dismissBottomSheet = dismissBottomSheet)
            }
        }
    }
}

@Preview
@Composable
private fun PermissionRequestScreenPreview() {
    PreviewTheme {
        PermissionRequestScreen(
            screenData = PermissionScreenData(
                mainIcon = IconData(icon = Res.drawable.ic_walks),
                title = TextData(
                    text = "Allow Motion Access",
                    textSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                ),
                brandButton = PrimaryButtonData(
                    text = TextData(
                        text = "Allow",
                        textSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    action = {},
                ),
            ),
        )
    }
}

@Composable
private fun DataUsageNotice(dismissBottomSheet: () -> Unit = {}) {
    Column {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(),
        ) {
            Icon(
                modifier =
                    Modifier
                        .align(Alignment.CenterEnd)
                        .padding(Variables.GapL)
                        .clickable { dismissBottomSheet() },
                painter = painterResource(Res.drawable.ic_close),
                contentDescription = null,
                tint = LocalOSColors.current.neutral_p3,
            )
        }
        OSText(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = Variables.GapL, bottom = 14.dp)
                    .padding(horizontal = Variables.GapL),
            text = stringResource(Res.string.your_data_is_safe_with_us),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        OSText(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Variables.GapL)
                    .padding(bottom = Variables.GapL),
            color = LocalOSColors.current.neutral_p3,
            text = stringResource(Res.string.data_usage_explanation),
            textAlign = TextAlign.Center,
        )
    }
}
