package co.onestep.kmp.uikit.features.summary.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit.utils.gaitLabItem
import co.onestep.kmp.uikit.utils.getNormColor
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun GaitLabCard(
    modifier: Modifier = Modifier,
    gaitLabItems: SummaryScreenItem.GaitLabItem,
) {
    SummaryItemCard(modifier) {
        GaitLabContent(gaitLabItem = gaitLabItems)
    }
}

@Preview
@Composable
private fun GaitLabCardPreview() {
    PreviewTheme {
        GaitLabCard(gaitLabItems = gaitLabItem)
    }
}

@Composable
internal fun GaitLabContent(gaitLabItem: SummaryScreenItem.GaitLabItem) {
    Column(Modifier.padding(horizontal = Variables.GapL)) {
        Row(
            modifier = Modifier.padding(vertical = Variables.GapL),
            verticalAlignment = Alignment.Bottom,
        ) {
            OSText(
                text = gaitLabItem.metaData?.displayName.orEmpty(),
                fontSize = 20.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.W400,
            )
            Spacer(modifier = Modifier.width(8.dp))
            OSText(
                text = gaitLabItem.units,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.W400,
            )
        }
        NormIndicator(
            norm = gaitLabItem.norm,
            value = gaitLabItem.value,
            previousValue = gaitLabItem.previousValue,
        )
        if (gaitLabItem.previousValue != null) {
            CardBottomInfo(
                currentColor = gaitLabItem.value.getNormColor(gaitLabItem.norm),
                previousColor = gaitLabItem.previousValue.getNormColor(gaitLabItem.norm),
                previousText = gaitLabItem.previousLocalizedTime,
            )
            Spacer(Modifier.height(Variables.GapL))
        }
    }
}
