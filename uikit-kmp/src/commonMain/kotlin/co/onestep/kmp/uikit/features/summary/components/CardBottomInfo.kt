package co.onestep.kmp.uikit.features.summary.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.current
import co.onestep.kmp.uikit_kmp.generated.resources.previous
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun CardBottomInfo(
    modifier: Modifier = Modifier,
    currentColor: Color,
    previousColor: Color,
    previousText: String?,
) {
    Column(
        modifier,
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            color = LocalOSColors.current.neutral_m2,
        )
        Spacer(Modifier.height(Variables.GapS))
        Row {
            Box(
                Modifier
                    .size(12.dp)
                    .background(color = currentColor, shape = CircleShape)
                    .align(Alignment.CenterVertically),
            )
            Spacer(Modifier.width(Variables.GapS))
            OSText(
                text = stringResource(Res.string.current),
                fontSize = 12.sp,
            )
            Spacer(Modifier.width(Variables.GapL))
            Box(
                Modifier
                    .size(12.dp)
                    .background(color = Color.White, shape = CircleShape)
                    .border(width = 1.dp, color = previousColor, shape = CircleShape)
                    .align(Alignment.CenterVertically),
            )
            Spacer(Modifier.width(Variables.GapS))
            OSText(
                text = stringResource(Res.string.previous),
                fontSize = 12.sp,
            )
            Spacer(Modifier.width(2.dp))
            OSText(
                text = " : ",
                fontSize = 12.sp,
            )
            Spacer(Modifier.width(2.dp))
            OSText(
                text = previousText.orEmpty(),
                fontSize = 12.sp,
            )
        }
    }
}

@Preview
@Composable
private fun CardBottomInfoPreview() {
    PreviewTheme {
        CardBottomInfo(
            currentColor = androidx.compose.ui.graphics.Color.Green,
            previousColor = androidx.compose.ui.graphics.Color.Red,
            previousText = "Jan 5, 2023",
        )
    }
}
