package co.onestep.kmp.uikit.features.recordFlow.screensData

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Covers `RecordingScreenData.colorTheme`'s GET_READY host-colour override (OS-16980): the exact
 * regression this guards against is `colorTheme` silently reverting to always-orange, or the host
 * colour leaking into RECORDING/ANALYZING, on a future edit.
 */
class RecordingScreenDataColorThemeTest {

    // Deliberately distinct from every fixed stage colour (orange 0xFFF5960B, recording grey
    // 0xFF3E3D3B, analyzing blue 0xFF0D5097) so a test that should see it fail loudly instead of
    // passing by coincidence.
    private val hostColor = Color(0xFF2196F3)

    private fun screenData(
        stage: RecordingScreenData.RecordScreenStage,
        activityColor: Color? = null,
    ) = RecordingScreenData(
        recordScreenStage = stage,
        title = TextData("Get ready", 60.sp, FontWeight.Bold),
        instructions = TextData("instructions", 28.sp, FontWeight.Bold),
        activityColor = activityColor,
    )

    @Test
    fun getReadyFallsBackToOrangeWhenNoHostColourIsGiven() {
        assertEquals(
            Color(0xFFF5960B),
            screenData(RecordingScreenData.RecordScreenStage.GET_READY).colorTheme,
        )
    }

    @Test
    fun getReadyTakesTheHostColourWhenGiven() {
        assertEquals(
            hostColor,
            screenData(RecordingScreenData.RecordScreenStage.GET_READY, activityColor = hostColor).colorTheme,
        )
    }

    @Test
    fun recordingIgnoresTheHostColour() {
        assertEquals(
            Color(0xFF3E3D3B),
            screenData(RecordingScreenData.RecordScreenStage.RECORDING, activityColor = hostColor).colorTheme,
        )
    }

    @Test
    fun analyzingIgnoresTheHostColour() {
        assertEquals(
            Color(0xFF0D5097),
            screenData(RecordingScreenData.RecordScreenStage.ANALYZING, activityColor = hostColor).colorTheme,
        )
    }

    /**
     * `colorTheme` also paints the GET_READY title text
     * ([co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.recording.RecordingScreenContent]
     * line ~159) against the screen's light header background (`neutral_m4` /
     * `Color(0xFFFBFBFB)`, see [co.onestep.kmp.uikit.ui.theme.Palette]). That title renders at
     * 60sp/48sp Bold — WCAG "large text" — so the applicable minimum is 3:1, not the 4.5:1 normal
     * -text threshold.
     *
     * `Color(0xFF0D5097)` (Primary/700, already used for the fixed ANALYZING colour — a natural
     * "Walk blue" a host would pick) measures ~7.76:1 against that header: comfortably clears 3:1
     * (and would clear normal-text 4.5:1 too). A mid-tone brand blue like `Color(0xFF1B81DC)`
     * (Primary/500) measures ~3.89:1: it clears the 3:1 large-text minimum this title needs, but
     * would fail 4.5:1 if reused anywhere the text isn't large — evidence for the KDoc's "dark/
     * saturated enough" guidance, not a decorative claim.
     */
    @Test
    fun aRepresentativeDarkHostColourStaysReadableOnTheLightHeader() {
        val header = Color(0xFFFBFBFB) // neutral_m4
        val recommendedDark = Color(0xFF0D5097) // Primary/700 — matches ANALYZING's fixed colour
        val midToneBrand = Color(0xFF1B81DC) // Primary/500

        val darkRatio = contrastRatio(recommendedDark, header)
        val midRatio = contrastRatio(midToneBrand, header)

        assertTrue(
            darkRatio >= 4.5,
            "expected Primary/700 to clear normal-text AA (4.5:1) against the header, got $darkRatio",
        )
        assertTrue(
            midRatio >= 3.0 && midRatio < 4.5,
            "expected Primary/500 to clear large-text AA (3:1) but not normal-text AA (4.5:1) " +
                "against the header, got $midRatio",
        )
    }

    // WCAG 2.x relative luminance + contrast ratio, https://www.w3.org/TR/WCAG21/#dfn-relative-luminance
    private fun contrastRatio(a: Color, b: Color): Double {
        val lA = relativeLuminance(a)
        val lB = relativeLuminance(b)
        val lighter = maxOf(lA, lB)
        val darker = minOf(lA, lB)
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun relativeLuminance(color: Color): Double {
        fun linearize(channel: Float): Double {
            val c = channel.toDouble()
            return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        }
        val r = linearize(color.red)
        val g = linearize(color.green)
        val b = linearize(color.blue)
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }
}
