package co.onestep.kmp.uikit.features.permissions.ios.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.ui.components.BottomSheet
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors

/**
 * Bottom-sheet "Your data is safe with us" used by permission screens.
 *
 * Displays as a modal bottom sheet with an X close button at top-right.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DataUsageInfoSheet(
    description: String,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    BottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
        ) {
            // Close button row
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onDismiss) {
                    OSText(
                        text = "\u2715",
                        fontSize = 18.sp,
                        color = LocalOSColors.current.neutral_p1,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OSText(
                text = "Your data is safe with us",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = LocalOSColors.current.neutral_p3,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(12.dp))

            OSText(
                text = description,
                fontSize = 15.sp,
                color = LocalOSColors.current.neutral_p3,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
