package co.onestep.kmp.uikit.features.recordFlow.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val colors = LocalOSColors.current
    val borderWidth by animateFloatAsState(targetValue = if (selected) 4f else 1f, label = "")
    // Figma "Card selector": white card surface with a 4dp-rounded border. Built from a plain
    // clickable Row (not Material3 Card) so press feedback flows through LocalIndication — the
    // iOS opacity dim / Android ripple — instead of the Material ripple, which a clickable Card
    // draws via LocalRippleConfiguration and which we disable on iOS.
    val shape = RoundedCornerShape(4.dp)

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 8.dp)
                .then(modifier)
                .clip(shape)
                .clickable { onItemSelected(selectionItemData) }
                .background(colors.neutral_m5, shape)
                .border(
                    border =
                        BorderStroke(
                            width = borderWidth.dp,
                            color = if (selected) colors.primary_0 else colors.neutral_m1,
                        ),
                    shape = shape,
                )
                // Figma "Card selector": uniform 16dp padding, 15px icon→text gap.
                .padding(Variables.GapL),
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(15.dp),
    ) {
        selectionItemData.icon?.let {
            Icon(
                painter = painterResource(it.icon),
                tint = colors.brand_text,
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
                color = colors.neutral_p3,
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
                    color = colors.neutral_p3,
                )
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
