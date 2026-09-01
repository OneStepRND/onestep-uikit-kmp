package co.onestep.kmp.uikit.features.carlog.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import co.onestep.kmp.uikit.testing.OSTTestTags
import co.onestep.kmp.uikit.utils.test
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.carlog.models.CarLogScreenState
import co.onestep.kmp.uikit.features.recordFlow.screensData.IconData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData
import co.onestep.kmp.uikit.ui.components.PrimaryBrandButton
import co.onestep.designsystem.components.OSText
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.cd_care_log_empty_icon
import co.onestep.kmp.uikit_kmp.generated.resources.ic_walks
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun EmptyCareLogScreenPreview() {
    PreviewTheme {
        EmptyCareLogScreen(
            emptyScreenState = CarLogScreenState.Empty(
                iconData = IconData(icon = Res.drawable.ic_walks),
                title = TextData(
                    text = "No activities yet",
                    textSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                ),
                subtitle = TextData(
                    text = "Complete a walk to see it here",
                    textSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                ),
                noticeCards = mutableListOf(),
            ),
        )
    }
}

@Composable
internal fun EmptyCareLogScreen(
    modifier: Modifier = Modifier,
    emptyScreenState: CarLogScreenState.Empty,
) {
    Box(
        modifier
            .fillMaxSize()
            .test(OSTTestTags.CareLog.EMPTY_STATE),
        contentAlignment = TopCenter,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            Image(
                modifier =
                    Modifier
                        .align(CenterHorizontally)
                        .size(80.dp),
                painter = painterResource(emptyScreenState.iconData.icon),
                colorFilter =
                    emptyScreenState.iconData.tintColor?.let {
                        ColorFilter.tint(it)
                    },
                contentDescription = stringResource(Res.string.cd_care_log_empty_icon),
            )

            Spacer(modifier = Modifier.height(Variables.GapL))
            OSText(
                modifier =
                    Modifier
                        .align(CenterHorizontally)
                        .padding(horizontal = Variables.GapL),
                text = emptyScreenState.title.text,
                fontWeight = emptyScreenState.title.fontWeight,
                fontSize = emptyScreenState.title.textSize,
                lineHeight = 37.sp,
                textAlign = TextAlign.Center,
            )
            emptyScreenState.subtitle?.let {
                Spacer(modifier = Modifier.height(10.dp))
                OSText(
                    modifier =
                        Modifier
                            .align(CenterHorizontally)
                            .padding(horizontal = 36.dp),
                    text = it.text,
                    fontWeight = it.fontWeight,
                    fontSize = it.textSize,
                    lineHeight = 28.sp,
                    textAlign = TextAlign.Center,
                )
            }

            emptyScreenState.buttonData?.let {
                Spacer(modifier = Modifier.height(30.dp))
                PrimaryBrandButton(
                    modifier =
                        Modifier
                            .align(CenterHorizontally)
                            .height(50.dp),
                    data = it,
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
