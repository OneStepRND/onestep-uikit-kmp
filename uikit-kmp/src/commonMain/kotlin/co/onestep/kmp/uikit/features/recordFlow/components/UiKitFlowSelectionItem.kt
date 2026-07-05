package co.onestep.kmp.uikit.features.recordFlow.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.onestep.kmp.uikit.features.recordFlow.previewSelectionItemThigh
import co.onestep.kmp.uikit.features.recordFlow.screensData.SelectionItemData
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UiKitFlowSelectionItem(
    modifier: Modifier = Modifier,
    selectionItemData: SelectionItemData,
    selected: Boolean = false,
    onItemSelected: (SelectionItemData) -> Unit = {},
) {
    val borderWidth by animateFloatAsState(targetValue = if (selected) 4f else 1f, label = "")

    Card(
        border =
            if (selected) {
                BorderStroke(
                    borderWidth.dp,
                    color = LocalOSColors.current.primary_0,
                )
            } else {
                BorderStroke(
                    borderWidth.dp,
                    color = LocalOSColors.current.neutral_m1,
                )
            },
        shape = RoundedCornerShape(4.dp),
        colors =
            CardDefaults.cardColors(
                // Figma "Card selector": white card surface (neutral/m5), text neutral/p3.
                containerColor = LocalOSColors.current.neutral_m5,
                contentColor = LocalOSColors.current.neutral_p3,
            ),
        onClick = { onItemSelected(selectionItemData) },
        modifier =
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 8.dp)
                .then(modifier),
    ) {
        Row(
            modifier =
                Modifier
                    // Figma "Card selector": uniform 16dp padding, 15px icon→text gap.
                    .padding(Variables.GapL)
                    .fillMaxWidth(),
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            selectionItemData.icon?.let {
                Icon(
                    painter = painterResource(it.icon),
                    tint = LocalOSColors.current.primary_p3_main,
                    contentDescription = null,
                    modifier =
                        Modifier
                            .size(it.iconSize ?: 70.dp)
                            .align(CenterVertically),
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.Start,
                verticalArrangement = Arrangement.Center,
            ) {
                OSText(
                    text = selectionItemData.text.text,
                    fontSize = selectionItemData.text.textSize,
                    fontWeight = selectionItemData.text.fontWeight,
                )
                if (selectionItemData.description != null) {
                    OSText(
                        modifier =
                            Modifier
                                .padding(start = Variables.GapL)
                                .align(CenterVertically),
                        text = selectionItemData.description.text,
                        fontSize = selectionItemData.description.textSize,
                        fontWeight = selectionItemData.description.fontWeight,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun UiKitFlowSelectionItemPreview() {
    PreviewTheme {
        UiKitFlowSelectionItem(selectionItemData = previewSelectionItemThigh)
    }
}
