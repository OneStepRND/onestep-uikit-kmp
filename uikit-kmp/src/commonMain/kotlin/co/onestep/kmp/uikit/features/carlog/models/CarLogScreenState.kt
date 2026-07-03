package co.onestep.kmp.uikit.features.carlog.models

import co.onestep.kmp.uikit.features.recordFlow.screensData.PrimaryButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.IconData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData

internal sealed interface CarLogScreenState {
    val noticeCards: MutableList<NoticeCardData>
    val infoData: InfoData?

    data object Loading : CarLogScreenState {
        override val noticeCards = mutableListOf<NoticeCardData>()
        override val infoData = null
    }

    data class Empty(
        val iconData: IconData,
        val title: TextData,
        val subtitle: TextData? = null,
        val buttonData: PrimaryButtonData? = null,
        override val noticeCards: MutableList<NoticeCardData>,
        override val infoData: InfoData? = null,
    ) : CarLogScreenState
}

internal sealed class BackgroundScreenState : CarLogScreenState {
    data class Content(
        val backgroundRecords: Map<String, List<BackgroundLogItemData>>,
        override val noticeCards: MutableList<NoticeCardData>,
        override val infoData: InfoData? = null,
    ) : CarLogScreenState
}

internal sealed class InAppScreenState : CarLogScreenState {
    data class Content(
        val carLogItems: List<CarLogItemData>,
        override val noticeCards: MutableList<NoticeCardData>,
        override val infoData: InfoData? = null,
    ) : CarLogScreenState
}
