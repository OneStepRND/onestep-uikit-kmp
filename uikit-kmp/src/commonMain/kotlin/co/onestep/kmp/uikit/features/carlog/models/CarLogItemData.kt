package co.onestep.kmp.uikit.features.carlog.models

import co.onestep.kmp.uikit.models.OSTActivityType
import org.jetbrains.compose.resources.DrawableResource

internal interface CarLogItemData {
    val id: String
    val day: String
    val type: OSTActivityType
    val title: String
    val icon: DrawableResource
}
