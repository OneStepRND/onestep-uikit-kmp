package co.onestep.kmp.uikit.features.recordFlow.destinations

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import co.onestep.kmp.uikit.features.recordFlow.components.ToolBarHeight
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.UiKitScreen
import co.onestep.kmp.uikit.features.recordFlow.screensData.PrimaryButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.IconData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData
import co.onestep.kmp.uikit.features.recordFlow.screensData.UiKitScreenData
import co.onestep.kmp.uikit.testing.OSTTestTags
import co.onestep.kmp.uikit.utils.UIktDestination
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.continue_camel_case
import co.onestep.kmp.uikit_kmp.generated.resources.ic_like_stars
import co.onestep.kmp.uikit_kmp.generated.resources.no_summary_notice_title
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource

@Serializable
data object NoSummaryNoticeDestination : UIktDestination

// Preview skipped: requires NavController

/**
 * @param topPadding space to leave for the host's toolbar. Defaults to [ToolBarHeight] for the
 *   summary flow, whose NavDisplay reserves nothing and leaves each screen to inset itself. The
 *   record flow's NavDisplay already reserves the status bar plus the toolbar for every
 *   non-collapsed route, so it passes `0.dp` — otherwise this screen pays for the toolbar twice.
 */
fun EntryProviderScope<NavKey>.noSummaryNoticeScreen(
    topPadding: Dp = ToolBarHeight.dp,
    onPrimaryAction: () -> Unit,
) {
    entry<NoSummaryNoticeDestination> {
        NoSummaryNoticeContent(topPadding = topPadding, onPrimaryAction = onPrimaryAction)
    }
}

@Composable
private fun NoSummaryNoticeContent(topPadding: Dp, onPrimaryAction: () -> Unit) {
    UiKitScreen(
        modifier = Modifier.padding(top = topPadding),
        screenTag = OSTTestTags.RecordFlow.NO_SUMMARY_NOTICE_SCREEN,
        onBackPress = onPrimaryAction,
        screenData = UiKitScreenData(
            title = TextData(
                stringResource(Res.string.no_summary_notice_title),
                textSize = 28.sp,
                fontWeight = FontWeight.Bold,
            ),
            mainIcon = IconData(
                Res.drawable.ic_like_stars,
            ),
            brandButton = PrimaryButtonData(
                text = TextData(
                    stringResource(Res.string.continue_camel_case),
                    24.sp,
                    FontWeight.W600,
                ),
                action = { onPrimaryAction() },
            ),
        ),
    )
}
