package co.onestep.kmp.uikit.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ColorGroup(
    val p4: Color = Color.Unspecified,
    val p3: Color = Color.Unspecified,
    val p2: Color = Color.Unspecified,
    val p1: Color = Color.Unspecified,
    val zero: Color = Color.Unspecified,
    val m1: Color = Color.Unspecified,
    val m2: Color = Color.Unspecified,
    val m3: Color = Color.Unspecified,
    val m4: Color = Color.Unspecified,
)

@Immutable
data class HealthColorGroup(
    val healthy: ColorGroup = ColorGroup(),
    val caution: ColorGroup = ColorGroup(),
    val concern: ColorGroup = ColorGroup(),
)

@Immutable
data class Palette(
    val primary: ColorGroup = ColorGroup(),
    val neutral: ColorGroup = ColorGroup(),
    val error: ColorGroup = ColorGroup(),
    val warning: ColorGroup = ColorGroup(),
    val success: ColorGroup = ColorGroup(),
    val info: ColorGroup = ColorGroup(),
    val dataViz: List<Color> = emptyList(),
    val dataVizLight: List<Color> = emptyList(),
    val health: HealthColorGroup = HealthColorGroup(),
)

fun Palette.overrideWith(
    primaryColor: Color?,
): Palette {
    if (primaryColor == null || primaryColor == Color.Unspecified) return this
    return copy(
        primary = primary.copy(zero = primaryColor),
    )
}

/**
 * Light palette — color values sourced from `co.onestep:design-system` v1.1
 * (`Themes.osLightTheme()` with default primary override).
 *
 * The design-system neutral scale has 9 levels (p3..m5); the UIKit Palette has 8
 * (p3..m4). We map design-system m4 (FBFBFB) → Palette m3 and m5 (FFFFFF) → Palette m4
 * so the lightest neutrals land in the right slots.
 */
val DefaultLightPalette = Palette(
    primary = ColorGroup(
        p4 = Color(0xFF0C2545),   // Primary/950
        p3 = Color(0xFF0F3157),   // Primary/900 (default primary)
        p2 = Color(0xFF0F457D),   // Primary/800
        p1 = Color(0xFF0D5097),   // Primary/700
        zero = Color(0xFF1B81DC), // Primary/500
        m1 = Color(0xFF85BEF4),   // Primary/300
        m2 = Color(0xFFE2EEFC),   // Primary/100
        m3 = Color(0xFFF1F7FE),   // Primary/50
    ),
    neutral = ColorGroup(
        p3 = Color(0xFF3E3D3B),   // Grey/900
        p2 = Color(0xFF716D69),   // Grey/500
        p1 = Color(0xFF8C8884),   // Grey/400
        zero = Color(0xFFB3B0AD), // Grey/300
        m1 = Color(0xFFD2D0CF),   // Grey/200
        m2 = Color(0xFFE7E6E6),   // Grey/100
        m3 = Color(0xFFFBFBFB),   // Grey/50  (design-system neutral_m4)
        m4 = Color(0xFFFFFFFF),   // Grey/white (design-system neutral_m5)
    ),
    error = ColorGroup(
        p2 = Color(0xFFB00404),   // Red/800
        p1 = Color(0xFFFF9595),   // Red/300
        zero = Color(0xFFFFC1C1), // Red/200
        m1 = Color(0xFFFFDDDD),   // Red/100
    ),
    warning = ColorGroup(
        p2 = Color(0xFFF5960B),   // Yellow/500
        p1 = Color(0xFFFBBC31),   // Yellow/400
        zero = Color(0xFFFDE28A), // Yellow/200
        m1 = Color(0xFFFEF1C7),   // Yellow/100
        m2 = Color(0xFFFFFAEB),   // Yellow/50
    ),
    success = ColorGroup(
        p3 = Color(0xFF2C9D72),   // Green/500
        zero = Color(0xFFB4E6CD), // Green/200
        m1 = Color(0xFFD8F3E4),   // Green/100
        m2 = Color(0xFFEFFAF4),   // Green/50
    ),
    info = ColorGroup(
        p1 = Color(0xFF0D5097),   // Primary/700
        m3 = Color(0xFFF1F7FE),   // Primary/50
    ),
    dataViz = listOf(
        Color(0xFF1B81DC),   // Primary/500
        Color(0xFF85BEF4),   // Primary/300
        Color(0xFF0F3157),   // Primary/900
        Color(0xFF0D5097),   // Primary/700
        Color(0xFF1CA8B0),   // DataViz/Turquoise
        Color(0xFF65E1E3),   // DataViz/Light turquoise
        Color(0xFFECF614),   // DataViz/Poison
        Color(0xFFF1FD99),   // DataViz/Light poison
        Color(0xFFFEA2EF),   // DataViz/Pink
        Color(0xFFFFD5F8),   // DataViz/Light pink
    ),
    dataVizLight = listOf(
        Color(0xFFE2EEFC),   // Primary/100
        Color(0xFFF1F7FE),   // Primary/50
    ),
    health = HealthColorGroup(
        healthy = ColorGroup(
            p1 = Color(0xFF2C9D72),   // Green/500
            zero = Color(0xFFB4E6CD), // Green/200
            m1 = Color(0xFFD8F3E4),   // Green/100
            m2 = Color(0xFFEFFAF4),   // Green/50
        ),
        caution = ColorGroup(
            p2 = Color(0xFFF5960B),   // Yellow/500
            p1 = Color(0xFFFBBC31),   // Yellow/400
            zero = Color(0xFFFDE28A), // Yellow/200
            m1 = Color(0xFFFEF1C7),   // Yellow/100
            m2 = Color(0xFFFFFAEB),   // Yellow/50
        ),
        concern = ColorGroup(
            p1 = Color(0xFFF95816),   // Orange/500
            zero = Color(0xFFFDAA74), // Orange/300
            m1 = Color(0xFFFFE8D5),   // Orange/100
            m2 = Color(0xFFFFF5ED),   // Orange/50
        ),
    ),
)

val LocalPalette = staticCompositionLocalOf { DefaultLightPalette }
