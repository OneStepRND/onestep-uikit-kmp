package co.onestep.kmp.uikit.features.summary.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.summary.models.SummaryListState
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit.utils.partialSuccessPreview
import org.jetbrains.compose.ui.tooling.preview.Preview
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.ic_clock_and_stars_blue
import co.onestep.kmp.uikit_kmp.generated.resources.ic_walk_stars
import co.onestep.kmp.uikit_kmp.generated.resources.minutes_walked
import co.onestep.kmp.uikit_kmp.generated.resources.steps_completed
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PartialSummaryScreen(
    modifier: Modifier = Modifier,
    partialScreenState: SummaryListState.Partial.Success,
) {

    val steps = partialScreenState.steps
    val durationText = partialScreenState.durationText

    if (steps != null || durationText != null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(LocalOSColors.current.neutral_m4)
                .then(modifier),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(Variables.GapXXL))

            steps?.let {
                Image(
                    painter = painterResource(Res.drawable.ic_walk_stars),
                    contentDescription = null,
                    modifier = Modifier.size(70.dp),
                )

                Spacer(modifier = Modifier.height(Variables.GapM))

                OSText(
                    modifier = modifier,
                    text = it.toString(),
                    fontSize = 52.sp,
                    fontWeight = FontWeight.ExtraBold,
                )

                OSText(
                    modifier = modifier,
                    text = stringResource(Res.string.steps_completed),
                    fontSize = 18.sp,
                    color = LocalOSColors.current.neutral_p2,
                    fontWeight = FontWeight.Normal,
                )
            }

            durationText?.let {
                Spacer(modifier = Modifier.height(Variables.GapXXL))
                Image(
                    painter = painterResource(Res.drawable.ic_clock_and_stars_blue),
                    contentDescription = null,
                    modifier = Modifier.size(70.dp),
                )
                Spacer(modifier = Modifier.height(Variables.GapM))

                OSText(
                    modifier = modifier,
                    text = it,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.ExtraBold,
                )

                OSText(
                    modifier = modifier,
                    text = stringResource(Res.string.minutes_walked),
                    fontSize = 18.sp,
                    color = LocalOSColors.current.neutral_p2,
                    fontWeight = FontWeight.Normal,
                )
            }
        }
    } else {
        Column(
            Modifier
                .fillMaxSize()
                .background(LocalOSColors.current.neutral_m4)
                .padding(top = Variables.GapXXL)
                .then(modifier),
        ) {
            OSText(
                text = partialScreenState.title,
                fontWeight = FontWeight.W700,
                fontSize = 28.sp,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(Variables.GapL))
            OSText(
                text = partialScreenState.subtitle,
                fontWeight = FontWeight.W400,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(Variables.GapL))
        }
    }
}

@Preview
@Composable
private fun PartialSummaryScreenPreview() {
    PreviewTheme {
        PartialSummaryScreen(
            partialScreenState = partialSuccessPreview(),
        )
    }
}
