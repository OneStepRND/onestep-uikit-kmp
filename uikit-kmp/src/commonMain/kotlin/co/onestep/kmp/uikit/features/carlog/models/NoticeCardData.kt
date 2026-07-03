package co.onestep.kmp.uikit.features.carlog.models

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import co.onestep.kmp.uikit.features.recordFlow.screensData.PrimaryButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData

@Stable
internal data class NoticeCardData(
    val type: NoticeCardType = NoticeCardType.Default,
    val textData: TextData,
    val button: PrimaryButtonData,
) {
    var isVisible by mutableStateOf(true)
}

internal enum class NoticeCardType {
    Permissions,
    BackgroundMonitoring,
    Default,
}
