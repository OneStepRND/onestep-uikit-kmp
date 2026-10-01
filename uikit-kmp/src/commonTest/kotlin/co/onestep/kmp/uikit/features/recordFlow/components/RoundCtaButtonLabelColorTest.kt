package co.onestep.kmp.uikit.features.recordFlow.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The Start button's label sits on a host-supplied fill (`MainButtonData.color`), so white alone is
 * not safe: the Clinician app's TUG, Stairs and ROM colours all fail 3:1 against it. The label is
 * 28sp Bold — WCAG "large text" — so 3:1 is the bar every case below must clear.
 */
class RoundCtaButtonLabelColorTest {

    private val white = Color.White
    private val darkLabel = Color(0xFF3E3D3B)

    private fun contrast(a: Color, b: Color): Float {
        val (hi, lo) = listOf(a.luminance(), b.luminance()).sortedDescending()
        return (hi + 0.05f) / (lo + 0.05f)
    }

    @Test
    fun darkFillsKeepTheWhiteLabel() {
        listOf(Color(0xFF1B81DC), Color(0xFF0F3157), Color(0xFF2D1B8E)).forEach { fill ->
            assertEquals(white, labelColorOn(fill))
        }
    }

    @Test
    fun lightFillsSwitchToTheDarkLabel() {
        listOf(Color(0xFF1CA8B0), Color(0xFF85BEF4), Color(0xFF65E1E3)).forEach { fill ->
            assertEquals(darkLabel, labelColorOn(fill))
        }
    }

    @Test
    fun everyClinicianActivityColourClearsLargeTextContrast() {
        listOf(
            0xFF1B81DC, 0xFF1CA8B0, 0xFFD544D3, 0xFF57108B, 0xFF85BEF4, 0xFF2D1B8E,
            0xFF8C8884, 0xFF7E26CE, 0xFF0F3157, 0xFF65E1E3, 0xFF0D5097,
        ).map { Color(it) }.forEach { fill ->
            val ratio = contrast(labelColorOn(fill), fill)
            assertTrue(ratio >= 3f, "label on $fill is only $ratio:1")
        }
    }
}
