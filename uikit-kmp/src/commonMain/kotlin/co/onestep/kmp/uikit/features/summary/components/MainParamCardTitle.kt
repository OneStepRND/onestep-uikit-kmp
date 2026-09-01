package co.onestep.kmp.uikit.features.summary.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import co.onestep.kmp.uikit.testing.OSTTestTags
import co.onestep.kmp.uikit.utils.test
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.edit_result_content_description
import co.onestep.kmp.uikit_kmp.generated.resources.ic_edit
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun MainParamCardTitle(
    modifier: Modifier = Modifier,
    title: String,
    onEdit: (() -> Unit)? = null,
) {
    if (onEdit == null) {
        OSText(
            modifier = modifier,
            text = title,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.W400,
            fontSize = 16.sp,
        )
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            OSText(
                text = title,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.W400,
                fontSize = 16.sp,
            )
            // IconButton (48 dp min touch target) instead of a bare 17 dp clickable icon —
            // keeps the pen reachable for touch + TalkBack.
            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .padding(start = 2.dp)
                    .test(OSTTestTags.Summary.STS_EDIT_BUTTON),
            ) {
                Icon(
                    modifier = Modifier.size(17.dp),
                    painter = painterResource(Res.drawable.ic_edit),
                    contentDescription = stringResource(Res.string.edit_result_content_description),
                    tint = LocalOSColors.current.primary_0,
                )
            }
        }
    }
}

@Preview
@Composable
private fun MainParamCardTitlePlainPreview() {
    PreviewTheme {
        MainParamCardTitle(title = "Today at 2:34 PM, 00:30")
    }
}

@Preview
@Composable
private fun MainParamCardTitleEditablePreview() {
    PreviewTheme {
        MainParamCardTitle(title = "Today at 2:34 PM, 00:30", onEdit = {})
    }
}
