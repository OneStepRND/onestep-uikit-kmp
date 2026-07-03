package co.onestep.kmp.uikit.features.summary.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun SummaryItemCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier =
            modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
        border = BorderStroke(width = 1.dp, color = LocalOSColors.current.neutral_m1),
        shape = RoundedCornerShape(0.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = LocalOSColors.current.neutral_m5,
                contentColor = LocalOSColors.current.neutral_p3,
            ),
    ) {
        content()
    }
}

@Preview
@Composable
private fun SummaryItemCardPreview() {
    PreviewTheme {
        SummaryItemCard {
        }
    }
}
