package co.onestep.kmp.uikit.features.carlog.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.carlog.models.InfoData
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.designsystem.theme.Variables
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun Info(
    modifier: Modifier = Modifier,
    infoData: InfoData,
) {
    Column(Modifier.padding(Variables.GapL).then(modifier)) {
        OSText(
            text = infoData.title,
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.W700,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(modifier = Modifier.height(10.dp))
        OSText(
            text = infoData.subtitle,
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow {
            infoData.infos.forEach { info ->
                Row {
                    Indicator(Modifier.align(CenterVertically), color = info.color)
                    Spacer(modifier = Modifier.size(5.dp))
                    DiscreteText(Modifier.align(CenterVertically), text = info.text)
                }
            }
        }
    }
}

@Preview
@Composable
private fun InfoPreview() {
    PreviewTheme {
        Info(
            infoData = InfoData(
                title = "Activity Summary",
                subtitle = "Your walk metrics",
                infos = listOf(
                    InfoData.Info(color = androidx.compose.ui.graphics.Color(0xFF4CAF50), text = "Normal"),
                    InfoData.Info(color = androidx.compose.ui.graphics.Color(0xFFFFC107), text = "Caution"),
                ),
            ),
        )
    }
}

@Composable
private fun DiscreteText(
    modifier: Modifier = Modifier,
    text: String,
) {
    OSText(
        modifier = modifier.padding(end = 5.dp),
        text = text,
        textAlign = TextAlign.Center,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        color = LocalOSColors.current.neutral_p1,
        fontWeight = FontWeight.Normal,
    )
}

@Composable
private fun Indicator(
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color,
) {
    Box(
        modifier =
            Modifier
                .size(12.dp)
                .background(
                    color,
                    shape = RoundedCornerShape(5.dp),
                ).then(modifier),
    )
}
