package co.onestep.kmp.uikit.features.recordFlow.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.onestep.kmp.uikit.features.recordFlow.screensData.IconData
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.cd_image_description
import co.onestep.kmp.uikit_kmp.generated.resources.ic_shoe_prints
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MainIcon(
    modifier: Modifier = Modifier,
    iconData: IconData,
) {
    val actionModifier =
        remember {
            if (iconData.action != null) {
                modifier
                    .clickable { iconData.action.invoke() }
            } else {
                modifier
            }
        }
    Image(
        modifier =
            Modifier
                .size(120.dp)
                .clickable { iconData.action?.invoke() }
                .then(actionModifier),
        painter = painterResource(iconData.icon),
        contentDescription = stringResource(Res.string.cd_image_description),
    )
}

@Preview
@Composable
private fun MainIconPreview() {
    PreviewTheme {
        MainIcon(iconData = IconData(Res.drawable.ic_shoe_prints))
    }
}
