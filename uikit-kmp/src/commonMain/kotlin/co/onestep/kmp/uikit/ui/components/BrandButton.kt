package co.onestep.kmp.uikit.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import co.onestep.designsystem.components.OSButtonSize
import co.onestep.designsystem.components.PrimaryButton
import co.onestep.kmp.uikit.features.recordFlow.screensData.PrimaryButtonData

@Composable
internal fun PrimaryBrandButton(
    modifier: Modifier = Modifier,
    data: PrimaryButtonData,
) {
    PrimaryButton(
        text = data.text.text,
        onClick = data.action,
        enabled = data.enabled,
        modifier = modifier.fillMaxWidth(),
        size = OSButtonSize.Big,
    )
}
