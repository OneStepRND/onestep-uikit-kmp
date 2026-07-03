package co.onestep.kmp.uikit.features.recordFlow.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit.features.recordFlow.screensData.NoteBannerData
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.ic_phone_orientation
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Full-width warning-tinted note banner with a leading illustration and a
 * bold title over a body line (mirrors uikit NoteBanner, Figma node 12860:15843).
 */
@Composable
internal fun NoteBanner(
    bannerData: NoteBannerData,
    modifier: Modifier = Modifier,
) {
    val colors = LocalOSColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.warning_m2)
            .padding(Variables.GapL),
        horizontalArrangement = Arrangement.spacedBy(Variables.GapL),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(bannerData.icon),
            contentDescription = null,
            modifier = Modifier.height(77.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            OSText(
                text = bannerData.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.neutral_p3,
            )
            OSText(
                text = bannerData.body,
                fontSize = 16.sp,
                color = colors.neutral_p3,
            )
        }
    }
}

@Preview
@Composable
private fun NoteBannerPreview() {
    PreviewTheme {
        NoteBanner(
            bannerData = NoteBannerData(
                icon = Res.drawable.ic_phone_orientation,
                title = "Important note!",
                body = "Place the phone with the top facing up and the screen facing outward.",
            ),
        )
    }
}
