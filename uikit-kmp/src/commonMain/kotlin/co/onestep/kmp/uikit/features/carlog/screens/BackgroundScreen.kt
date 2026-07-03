package co.onestep.kmp.uikit.features.carlog.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.carlog.components.BackgroundRecordsList
import co.onestep.kmp.uikit.features.carlog.models.BackgroundLogItemData
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources._100
import co.onestep.kmp.uikit_kmp.generated.resources.date
import co.onestep.kmp.uikit_kmp.generated.resources.score
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BackgroundScreen(
    modifier: Modifier = Modifier,
    backgroundRecords: Map<String, List<BackgroundLogItemData>>,
) {
    Column(
        Modifier
            .fillMaxSize()
            .then(modifier),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Variables.GapL),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            OSText(
                text = stringResource(Res.string.date),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = W500,
                modifier = Modifier.align(Alignment.CenterVertically),
            )
            Row(
                modifier = Modifier.align(Alignment.CenterVertically),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OSText(
                    text = stringResource(Res.string.score),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterVertically),
                )
                OSText(
                    text = stringResource(Res.string._100),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    color = LocalOSColors.current.neutral_p3,
                    modifier = Modifier.align(Alignment.CenterVertically),
                )
            }
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = LocalOSColors.current.neutral_m2,
        )
        BackgroundRecordsList(
            modifier = Modifier.fillMaxWidth(),
            backgroundRecords = backgroundRecords,
        )
    }
}
