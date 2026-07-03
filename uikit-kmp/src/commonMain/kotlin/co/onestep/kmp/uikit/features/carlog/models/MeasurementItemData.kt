package co.onestep.kmp.uikit.features.carlog.models

import co.onestep.kmp.uikit.models.OSTActivityType
import org.jetbrains.compose.resources.DrawableResource

internal data class MeasurementItemData(
    override val id: String,
    override val day: String,
    override val type: OSTActivityType,
    override val title: String,
    val time: String,
    override val icon: DrawableResource,
    val mainParam: String,
    val duration: String?,
    val tags: String? = null,
    val assistiveDevice: String? = null,
    val levelOfAssistance: String? = null,
    val note: String? = null,
) : CarLogItemData
