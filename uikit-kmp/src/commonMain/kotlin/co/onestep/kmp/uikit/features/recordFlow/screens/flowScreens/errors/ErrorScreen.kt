package co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.errors

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import co.onestep.kmp.uikit.features.recordFlow.generalErrorScreenData
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.UiKitScreen
import co.onestep.kmp.uikit.features.recordFlow.screensData.UiKitScreenData
import co.onestep.kmp.uikit.testing.OSTTestTags
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ErrorScreen(
    onBackPress: () -> Unit,
    onSelection: () -> Unit,
    onSecondaryAction: (() -> Unit)? = null,
    screenDataFactory: (() -> Unit, (() -> Unit)?) -> UiKitScreenData,
) {
    UiKitScreen(
        modifier = Modifier,
        screenTag = OSTTestTags.RecordFlow.ERROR_SCREEN,
        onBackPress = onBackPress,
        screenData = screenDataFactory(onSelection, onSecondaryAction),
    )
}

@Preview
@Composable
private fun ErrorScreenPreview() {
    PreviewTheme {
        UiKitScreen(
            modifier = Modifier,
            screenData = generalErrorScreenData,
        )
    }
}
