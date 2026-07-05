package co.onestep.kmp.uikit.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle

/**
 * iOS-style press indication: dims content opacity to 0.7 on press and restores on release,
 * matching UIKit highlighted-state timing, plus a light impact haptic on touch-down.
 */
actual fun osClickIndication(bounded: Boolean): Indication = IosPressIndication

// null fully disables the Material ripple on iOS (a transparent-color config still leaves a
// visible state layer on Material3 Card/Surface).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun osRippleConfiguration(): RippleConfiguration? = null

private object IosPressIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode =
        IosPressIndicationNode(interactionSource)

    override fun equals(other: Any?): Boolean = other === this

    override fun hashCode(): Int = "IosPressIndication".hashCode()
}

private class IosPressIndicationNode(
    private val interactionSource: InteractionSource,
) : Modifier.Node(), DrawModifierNode {

    // 1f = idle, 0.7f = pressed. Read in draw(), so changes invalidate the draw automatically.
    private val alpha = Animatable(1f)

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> {
                        withContext(Dispatchers.Main) {
                            UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium)
                                .impactOccurred()
                        }
                        launch { alpha.animateTo(PRESSED_ALPHA, tween(PRESS_IN_MS)) }
                    }

                    is PressInteraction.Release,
                    is PressInteraction.Cancel ->
                        launch { alpha.animateTo(1f, tween(PRESS_OUT_MS)) }
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        val current = alpha.value
        if (current < 1f) {
            drawContext.canvas.saveLayer(
                Rect(0f, 0f, size.width, size.height),
                Paint().apply { this.alpha = current },
            )
            drawContent()
            drawContext.canvas.restore()
        } else {
            drawContent()
        }
    }

    private companion object {
        const val PRESSED_ALPHA = 0.7f
        const val PRESS_IN_MS = 80
        const val PRESS_OUT_MS = 150
    }
}
