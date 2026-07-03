package co.onestep.kmp.uikit.features.carlog.models

import co.onestep.kmp.uikit.models.OSTActivityType
import org.jetbrains.compose.resources.DrawableResource

internal data class PendingMeasurementItemData(
    override val id: String,
    override val day: String,
    override val type: OSTActivityType,
    override val title: String,
    override val icon: DrawableResource,
    val time: String,
    val duration: String,
) : CarLogItemData
