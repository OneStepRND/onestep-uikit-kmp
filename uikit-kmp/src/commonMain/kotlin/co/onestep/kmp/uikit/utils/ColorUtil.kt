package co.onestep.kmp.uikit.utils

import androidx.compose.ui.graphics.Color
import co.onestep.kmp.uikit.bridge.MotionDataBridge
import co.onestep.kmp.uikit.models.OSTParamName
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.abnormal_results
import co.onestep.kmp.uikit_kmp.generated.resources.outside_range
import co.onestep.kmp.uikit_kmp.generated.resources.within_normal_range
import kotlin.math.max
import kotlin.math.min

internal fun String.toPartColor(): Color =
    when (this) {
        "red" -> Color(0xFFF95816)    // health_concern_p1
        "green" -> Color(0xFF2C9D72)  // health_healthy_p1
        "yellow" -> Color(0xFFFBBC31) // health_caution_p1
        "dark_red" -> Color(0xFF8B0000)
        else -> Color.Gray
    }

internal fun String.toBubbleColor(): Color =
    when (this) {
        "red" -> Color(0xFFF05F46)    // bad
        "green" -> Color(0xFF2C9D72)  // health_healthy_p1
        "yellow" -> Color(0xFFF09846) // med
        "dark_red" -> Color(0xFF8B0000)
        else -> Color.Gray
    }

internal fun Float.toColor(motionDataBridge: MotionDataBridge): Color =
    motionDataBridge
        .discreteScore(OSTParamName.WALKING_WALK_SCORE, this)
        ?.value
        ?.toBubbleColor() ?: Color.Gray

internal fun String.toColorDescription(resourceProvider: ResourceProvider): String =
    when (this) {
        "red" -> resourceProvider.getString(Res.string.abnormal_results)
        "yellow" -> resourceProvider.getString(Res.string.outside_range)
        "green" -> resourceProvider.getString(Res.string.within_normal_range)
        "dark_red" -> resourceProvider.getString(Res.string.abnormal_results)
        else -> this
    }

data class HSL(
    val h: Float,
    val s: Float,
    val l: Float,
)

fun Color.toHsl(): HSL {
    val r = red
    val g = green
    val b = blue
    val maxVal = max(r, max(g, b))
    val minVal = min(r, min(g, b))
    val l = (maxVal + minVal) / 2f

    if (maxVal == minVal) {
        return HSL(0f, 0f, l)
    }
    val d = maxVal - minVal
    val s = if (l > 0.5f) d / (2f - maxVal - minVal) else d / (maxVal + minVal)
    val h =
        when (maxVal) {
            r -> ((g - b) / d + if (g < b) 6f else 0f)
            g -> ((b - r) / d + 2f)
            else -> ((r - g) / d + 4f)
        } / 6f
    return HSL(h, s, l)
}

fun HSL.toColor(): Color {
    if (s == 0f) return Color(l, l, l, alpha = 1f)

    fun hue2rgb(
        p: Float,
        q: Float,
        t: Float,
    ): Float {
        var tVar = t
        if (tVar < 0f) tVar += 1f
        if (tVar > 1f) tVar -= 1f
        return when {
            tVar < 1f / 6f -> p + (q - p) * 6f * tVar
            tVar < 1f / 2f -> q
            tVar < 2f / 3f -> p + (q - p) * (2f / 3f - tVar) * 6f
            else -> p
        }
    }

    val q = if (l < 0.5f) l * (1f + s) else l + s - l * s
    val p = 2f * l - q
    val r = hue2rgb(p, q, h + 1f / 3f)
    val g = hue2rgb(p, q, h)
    val b = hue2rgb(p, q, h - 1f / 3f)
    return Color(r, g, b, alpha = 1f)
}
