package co.onestep.kmp.uikit.features.recordFlow.destinations

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import co.onestep.kmp.uikit.features.recordFlow.RecordFlowDataFactory
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.UiKitScreen
import co.onestep.kmp.uikit.features.tagging.models.Footwear
import co.onestep.kmp.uikit.testing.OSTTestTags
import co.onestep.kmp.uikit.utils.UIktDestination
import kotlinx.serialization.Serializable

@Serializable
data object PreFootwearDestination : UIktDestination

// Preview skipped: requires NavController

/**
 * Optional pre-recording clinical question: pick the [Footwear] worn for this recording. Gated by
 * [OSTRecordingConfiguration.showPreRecordingFootwearSelection]. Mirrors the Android uikit
 * `preFootwearScreen`: the selection is passed up together with its localized display name so the
 * caller can attach it as a tag (`NONE` adds no tag).
 */
fun EntryProviderScope<NavKey>.preFootwearScreen(
    onFootwearSelected: (Footwear, String) -> Unit,
) {
    entry<PreFootwearDestination> {
        // Resolve every option's localized display name once so the selection callback (which is
        // not itself composable) can hand the caller the tag string, matching uikit.
        val displayNames = Footwear.entries.associateWith { it.displayName() }
        // Analytics (screen-view + footwear-selected) are fired by the NavGraph: the
        // screen-view via the route-keyed screen-view effect, the selection via the
        // onFootwearSelected callback. Kept here to avoid threading a tracker into leaf screens.
        UiKitScreen(
            modifier = Modifier,
            screenTag = OSTTestTags.RecordFlow.PRE_FOOTWEAR_SCREEN,
            screenData = RecordFlowDataFactory.selectFootwearScreenData(
                onSelection = { footwear ->
                    onFootwearSelected(footwear, displayNames[footwear].orEmpty())
                },
            ),
        )
    }
}
