package co.onestep.kmp.uikit.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.Top
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun BulletPointText(
    modifier: Modifier = Modifier,
    color: Color = LocalOSColors.current.neutral_p3,
    textSize: TextUnit = 16.sp,
    text: @Composable () -> Unit,
) {
    Row(modifier = modifier, verticalAlignment = Top) {
        Text(
            text = "\u2022",
            color = color,
            fontSize = textSize,
            modifier = Modifier
                .padding(end = 8.dp)
                .align(Top),
        )
        Box(modifier = Modifier.align(CenterVertically).padding(top = 4.dp)) {
            text()
        }
    }
}

@Preview
@Composable
private fun BulletPointTextPreview() {
    PreviewTheme {
        BulletPointText {
            Text(text = "Walk in a straight line")
        }
    }
}
