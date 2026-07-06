package co.onestep.kmp.uikit.ui.theme

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Wraps [content] (an icon) in a native-iOS "Liquid Glass" circular chip — a frosted, near-white
 * disc with a soft drop shadow — used for the toolbar back `<` / close `X` buttons and the
 * dialog/sheet close buttons. [onClick] is the button action.
 *
 * **iOS only.** iOS draws the glass chip (`io.github.fletchmckee.liquid` lens + a white frost disc
 * + a soft shadow). Android renders [content] in a plain centering box with **no chip**, so neither
 * the shader library nor its `RuntimeShader` API-33 floor ever reaches Android consumers.
 *
 * The click handling (and its press indication — the iOS dim / Android press) is applied *inside*
 * this composable on a circular-clipped layer, so the indication is a circle, never a square, and
 * the drop shadow (on a separate unclipped layer) is never cut off. Put sizing and test tags on
 * [modifier]; do not add your own `clickable`/`clip` there. Size the icon in [content] a little
 * smaller than the chip so the glass reads as a ring around it.
 */
@Composable
expect fun OSTLiquidGlassCircle(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
)
