package co.onestep.kmp.uikit.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

/**
 * Returns true if the current layout direction is RTL.
 */
@Composable
@ReadOnlyComposable
fun isRtl(): Boolean = LocalLayoutDirection.current == LayoutDirection.Rtl

/**
 * Mirrors content horizontally when layout direction is RTL.
 * Useful for directional icons (arrows, chevrons) that should flip in RTL.
 */
@Composable
fun Modifier.rtlMirror(): Modifier {
    val scaleX = if (isRtl()) -1f else 1f
    return this.scale(scaleX = scaleX, scaleY = 1f)
}

/**
 * Returns [rtl] value when layout is RTL, [ltr] value otherwise.
 */
@Composable
@ReadOnlyComposable
fun <T> rtlAware(ltr: T, rtl: T): T = if (isRtl()) rtl else ltr
