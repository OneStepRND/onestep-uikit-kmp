package co.onestep.kmp.uikit.utils

import androidx.compose.ui.graphics.Color
import co.onestep.kmp.uikit.models.OSTNorm

fun Float.getNormColor(norm: OSTNorm): Color {
    val parts = norm.parts ?: return Color.Gray
    if (parts.isEmpty()) return Color.Gray

    val totalStart = parts.firstOrNull()?.start ?: 0f
    val totalEnd = parts.lastOrNull()?.end ?: 1f
    val value = this

    if (value < totalStart) {
        return parts.firstOrNull()?.color?.toBubbleColor() ?: Color.Unspecified
    }

    if (value > totalEnd) {
        return parts.lastOrNull()?.color?.toBubbleColor() ?: Color.Unspecified
    }

    for (part in parts) {
        val isInRange =
            when {
                part.includeStart && part.includeEnd -> value >= part.start && value <= part.end
                part.includeStart -> value >= part.start && value < part.end
                part.includeEnd -> value > part.start && value <= part.end
                else -> value > part.start && value < part.end
            }

        if (isInRange) {
            return part.color.toBubbleColor()
        }

        if (value == parts.lastOrNull()?.end) {
            return part.color.toBubbleColor()
        }
    }

    return Color.Unspecified
}
