package co.onestep.kmp.uikit.features.permissions.ios.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Data class representing a button in the mock iOS alert dialog.
 */
internal data class MockAlertButton(
    val title: String,
    val isHighlighted: Boolean = false,
    val onClick: () -> Unit = {},
)

/**
 * A composable that mimics the appearance of an iOS system alert dialog.
 * Purely visual — used to show users what the system dialog will look like.
 */
@Composable
internal fun IosMockAlertDialog(
    title: String,
    message: String,
    buttons: List<MockAlertButton>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF2F2F7))
            .fillMaxWidth(0.75f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Title and message
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                textAlign = TextAlign.Center,
            )
            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        // Buttons
        buttons.forEachIndexed { index, button ->
            HorizontalDivider(color = Color(0xFFD1D1D6), thickness = 0.5.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = button.onClick)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = button.title,
                    fontSize = 17.sp,
                    fontWeight = if (button.isHighlighted) FontWeight.SemiBold else FontWeight.Normal,
                    color = Color(0xFF007AFF),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
