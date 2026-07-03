package co.onestep.kmp.uikit.features.summary.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.summary.models.SummaryScreenItem
import co.onestep.designsystem.components.OSText
import androidx.compose.ui.graphics.Color
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit.utils.getNormColor
import co.onestep.kmp.uikit.utils.parameterItem
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.ic_filters
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun ParameterItemPreview() {
    PreviewTheme {
        ParameterItem(parameterItem = parameterItem)
    }
}

@Composable
internal fun ParameterItem(
    parameterItem: SummaryScreenItem.ParameterItem,
    modifier: Modifier = Modifier,
) {
    SummaryItemCard(modifier = modifier) {
        Column(modifier = Modifier.padding(Variables.GapL)) {
            Row {
                Icon(
                    painter = painterResource(Res.drawable.ic_filters),
                    contentDescription = null,
                    modifier =
                        Modifier
                            .width(24.dp)
                            .height(24.dp),
                )
                Spacer(Modifier.width(8.dp))
                OSText(
                    text = parameterItem.text.text,
                    fontSize = 20.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.W400,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            parameterItem.norm?.let {
                NormIndicator(
                    norm = parameterItem.norm,
                    value = parameterItem.value,
                    previousValue = parameterItem.previousValue,
                )
            }
            Row(verticalAlignment = Alignment.Bottom) {
                OSText(
                    text = parameterItem.displayName,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.W400,
                )
                Spacer(modifier = Modifier.width(8.dp))
                OSText(
                    text = parameterItem.units,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFFDCDFE3),
                    fontWeight = FontWeight.W400,
                )
            }
            if (parameterItem.previousValue != null && parameterItem.norm != null) {
                Spacer(Modifier.height(Variables.GapM))
                CardBottomInfo(
                    modifier = Modifier.padding(top = 14.dp),
                    currentColor = parameterItem.value.getNormColor(parameterItem.norm),
                    previousColor = parameterItem.previousValue.getNormColor(parameterItem.norm),
                    previousText = parameterItem.previousLocalizedTime,
                )
            }
        }
    }
}
