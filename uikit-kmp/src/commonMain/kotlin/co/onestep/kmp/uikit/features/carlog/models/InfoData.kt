package co.onestep.kmp.uikit.features.carlog.models

import androidx.compose.ui.graphics.Color

internal data class InfoData(
    val title: String,
    val subtitle: String,
    val infos: List<Info>,
) {
    data class Info(
        val color: Color,
        val text: String,
    )

    companion object {
        val empty = InfoData(title = "", subtitle = "", emptyList())
    }
}
