package co.onestep.kmp.uikit.features.summary.components

import org.jetbrains.compose.resources.DrawableResource

internal data class OSTTabData(
    val title: String,
    val index: Int,
    val icon: DrawableResource? = null,
    val selectedIcon: DrawableResource? = null,
)
