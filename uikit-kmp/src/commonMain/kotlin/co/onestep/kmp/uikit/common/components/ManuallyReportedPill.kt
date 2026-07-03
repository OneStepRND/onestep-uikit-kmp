package co.onestep.kmp.uikit.common.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.summary_manually_reported
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Manually reported" pill shown next to a measurement's main-param value when the result was
 * overridden via the STS self-report flow.
 *
 * Shared between the summary toolbar (default styling) and the care-log list item (compact
 * styling via [fontSize]/[cornerRadius]/[textColor]).
 */
@Composable
internal fun ManuallyReportedPill(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    cornerRadius: Dp = 16.dp,
    textColor: Color = LocalOSColors.current.neutral_p3,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = LocalOSColors.current.neutral_m2,
    ) {
        OSText(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            text = stringResource(Res.string.summary_manually_reported),
            fontSize = fontSize,
            color = textColor,
        )
    }
}

@Preview
@Composable
private fun ManuallyReportedPillPreview() {
    PreviewTheme {
        ManuallyReportedPill()
    }
}

@Preview
@Composable
private fun ManuallyReportedPillCompactPreview() {
    PreviewTheme {
        ManuallyReportedPill(
            fontSize = 12.sp,
            cornerRadius = 12.dp,
            textColor = LocalOSColors.current.neutral_p2,
        )
    }
}
