package co.onestep.kmp.uikit.features.permissions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.UiKitScreen
import co.onestep.kmp.uikit.features.recordFlow.screensData.PrimaryButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.IconData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData
import co.onestep.kmp.uikit.features.recordFlow.screensData.UiKitScreenData
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.cd_close_permissions
import co.onestep.kmp.uikit_kmp.generated.resources.continue_camel_case
import co.onestep.kmp.uikit_kmp.generated.resources.ic_close
import co.onestep.kmp.uikit_kmp.generated.resources.ic_walk_stars
import co.onestep.kmp.uikit_kmp.generated.resources.please_allow_to_analyze_your_movement_and_measure_your_progress_your_data_is_securely_stored_and_shared_only_with_your_healthcare_provider
import co.onestep.kmp.uikit_kmp.generated.resources.your_data_is_safe_with_us
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun PermissionsExplanationScreen(
    onClose: () -> Unit,
    onContinue: () -> Unit,
) {
    val context = LocalContext.current
    val appName = context.applicationInfo.loadLabel(context.packageManager).toString()
    Column {
        Box(
            Modifier.fillMaxWidth(),
        ) {
            Icon(
                modifier =
                    Modifier
                        .align(CenterEnd)
                        .padding(Variables.GapL)
                        .clickable { onClose() },
                painter = painterResource(Res.drawable.ic_close),
                contentDescription = stringResource(Res.string.cd_close_permissions),
            )
        }
        UiKitScreen(
            screenData =
                UiKitScreenData(
                    mainIcon =
                        IconData(
                            icon = Res.drawable.ic_walk_stars,
                        ),
                    title =
                        TextData(
                            text = stringResource(Res.string.your_data_is_safe_with_us),
                            textAlign = TextAlign.Center,
                            textSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalOSColors.current.primary_p3_main,
                        ),
                    subtitle =
                        TextData(
                            text =
                                stringResource(
                                    Res.string.please_allow_to_analyze_your_movement_and_measure_your_progress_your_data_is_securely_stored_and_shared_only_with_your_healthcare_provider,
                                    appName,
                                ),
                            textAlign = TextAlign.Center,
                            textSize = 18.sp,
                            color = LocalOSColors.current.neutral_p2,
                            fontWeight = FontWeight.Normal,
                        ),
                    brandButton =
                        PrimaryButtonData(
                            text =
                                TextData(
                                    stringResource(Res.string.continue_camel_case),
                                    textAlign = TextAlign.Center,
                                    textSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                            action = onContinue,
                        ),
                ),
        )
    }
}

@Preview
@Composable
private fun PermissionsExplanationScreenPreview() {
    PreviewTheme {
        PermissionsExplanationScreen(
            onClose = {},
            onContinue = {},
        )
    }
}
