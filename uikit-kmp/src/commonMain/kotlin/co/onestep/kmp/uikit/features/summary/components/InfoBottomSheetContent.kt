package co.onestep.kmp.uikit.features.summary.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.recordFlow.screensData.InfoBottomSheetData
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.ic_close
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun InfoBottomSheetContent(
    data: InfoBottomSheetData,
    onDismiss: () -> Unit,
) {
    val colors = LocalOSColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(
                painter = painterResource(Res.drawable.ic_close),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
                    .clickable { onDismiss() },
                tint = colors.neutral_p3,
            )
        }

        Spacer(Modifier.height(Variables.GapXL))

        OSText(
            text = data.title.text,
            fontSize = data.title.textSize,
            fontWeight = data.title.fontWeight,
            color = data.title.color ?: colors.neutral_p3,
            lineHeight = 32.sp
        )

        Spacer(Modifier.height(16.dp))

        OSText(
            text = data.body.text,
            fontSize = data.body.textSize,
            fontWeight = data.body.fontWeight,
            color = data.body.color ?: colors.neutral_p3,
        )

        Spacer(Modifier.height(24.dp))
    }
}
