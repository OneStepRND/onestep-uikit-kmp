package co.onestep.kmp.uikit.features.recordFlow.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.recordFlow.screensData.EmptyAnalysisScreenData
import co.onestep.kmp.uikit.features.recordFlow.screensData.PrimaryButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData
import co.onestep.kmp.uikit.testing.OSTTestTags
import co.onestep.kmp.uikit.utils.test
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import co.onestep.kmp.uikit.ui.components.FadingSurfaceToTransparent
import co.onestep.kmp.uikit.ui.components.PrimaryBrandButton
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit.utils.toMainParamTitle
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.cd_empty_analysis_icon
import co.onestep.kmp.uikit_kmp.generated.resources.empty_analysis_instructions
import co.onestep.kmp.uikit_kmp.generated.resources.steps
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun EmptyAnalysisScreen(
    modifier: Modifier = Modifier,
    screenData: EmptyAnalysisScreenData,
) {
    Box(modifier.fillMaxSize().test(OSTTestTags.RecordFlow.EMPTY_ANALYSIS_SCREEN)) {
        Column(
            Modifier
                .fillMaxSize()
                .height(160.dp)
                .padding(bottom = 100.dp)
                .background(LocalOSColors.current.neutral_m4)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(
                Modifier
                    .background(LocalOSColors.current.neutral_m5),
            ) {
                OSText(
                    modifier =
                        Modifier
                            .align(CenterHorizontally)
                            .padding(top = 20.dp),
                    text = screenData.timeStampMillis.toMainParamTitle(),
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                )
                Spacer(Modifier.height(24.dp))

                screenData.steps?.let {
                    OSText(
                        modifier =
                            Modifier
                                .align(CenterHorizontally),
                        text = it.toString(),
                        fontSize = 42.sp,
                        fontWeight = FontWeight.W800,
                    )
                }
                OSText(
                    modifier =
                        Modifier
                            .align(CenterHorizontally),
                    text = stringResource(Res.string.steps),
                    fontSize = 16.sp,
                    color = LocalOSColors.current.neutral_p1,
                    lineHeight = 22.sp,
                )
                Spacer(Modifier.height(32.dp))
                HorizontalDivider(
                    Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = LocalOSColors.current.neutral_m2,
                )
            }
            Column(
                modifier =
                    Modifier
                        .padding(horizontal = Variables.GapL),
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                screenData.icon?.let { iconData ->
                    Icon(
                        painter = painterResource(iconData.icon),
                        contentDescription = stringResource(Res.string.cd_empty_analysis_icon),
                        tint = iconData.tintColor ?: LocalOSColors.current.error_p2,
                        modifier = Modifier.size(40.dp),
                    )
                }
                Spacer(modifier = Modifier.height(Variables.GapL))
                OSText(
                    modifier =
                        Modifier
                            .fillMaxWidth(),
                    fontSize = 24.sp,
                    lineHeight = 27.sp,
                    fontWeight = W700,
                    text = screenData.title,
                    textAlign = TextAlign.Start,
                )
                Spacer(modifier = Modifier.height(Variables.GapL))

                OSText(
                    modifier =
                        Modifier
                            .fillMaxWidth(),
                    text = stringResource(Res.string.empty_analysis_instructions),
                    fontSize = 18.sp,
                    lineHeight = 27.sp,
                    fontWeight = FontWeight.W400,
                    textAlign = TextAlign.Start,
                )
            }
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
                        .align(BottomCenter)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(Variables.GapL)
                        .fillMaxWidth()
                        .test(OSTTestTags.RecordFlow.PRIMARY_BUTTON),
                data = screenData.brandButtonData,
            )
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Preview
@Composable
private fun EmptyAnalysisScreenPreview() {
    PreviewTheme {
        EmptyAnalysisScreen(
            screenData = EmptyAnalysisScreenData(
                timeStampMillis = 0L,
                title = "Not enough data collected",
                subtitle = "Please try again",
                steps = 12,
                brandButtonData = PrimaryButtonData(
                    text = TextData("Try again", 24.sp, FontWeight.W600),
                    action = {},
                ),
            ),
        )
    }
}
