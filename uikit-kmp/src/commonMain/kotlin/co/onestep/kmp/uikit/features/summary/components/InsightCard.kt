package co.onestep.kmp.uikit.features.summary.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.summary.models.SummaryScreenItem
import co.onestep.designsystem.components.OSText
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.kmp.uikit.utils.trendItem
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.cd_insight_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun InsightCard(trendItem: SummaryScreenItem.SimpleInsightItem) {
    SummaryItemCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
        ) {
            Image(
                painter = painterResource(trendItem.icon.icon),
                contentDescription = stringResource(Res.string.cd_insight_title),
                modifier =
                    Modifier
                        .size(24.dp)
                        .align(Alignment.Top),
            )
            Spacer(modifier = Modifier.width(12.dp))
            OSText(
                text = trendItem.text.text,
                fontSize = 20.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Normal,
            )
        }
    }
}

@Preview
@Composable
private fun InsightCardPreview() {
    PreviewTheme {
        InsightCard(trendItem = trendItem)
    }
}
