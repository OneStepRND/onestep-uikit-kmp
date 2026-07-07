package co.onestep.kmp.uikit.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// Default fill is the light `neutral_m2` (#E7E6E6). Callers pass the theme role so the
// track shape adapts to dark; the cache is keyed by color so light/dark don't collide.
fun tugCShape(fillColor: Color = Color(0xFFE7E6E6)): ImageVector {
    _vector?.let { if (_vectorColor == fillColor) return it }
    _vectorColor = fillColor
    _vector =
        Builder(
            name = "Vector",
            defaultWidth = 106.0.dp,
            defaultHeight = 156.0.dp,
            viewportWidth = 106.0f,
            viewportHeight = 156.0f,
        ).apply {
            path(
                fill = SolidColor(fillColor),
                stroke = null,
                strokeLineWidth = 0.0f,
                strokeLineCap = Butt,
                strokeLineJoin = Miter,
                strokeLineMiter = 4.0f,
                pathFillType = NonZero,
            ) {
                moveTo(69.81f, 14.1f)
                curveTo(50.5f, 2.61f, 28.91f, 0.21f, 16.88f, 0.21f)
                horizontalLineTo(7.3f)
                lineTo(26.22f, 20.22f)
                curveTo(26.71f, 20.75f, 26.71f, 21.49f, 26.22f, 22.01f)
                lineTo(8.19f, 41.08f)
                horizontalLineTo(16.87f)
                curveTo(22.95f, 41.08f, 33.79f, 42.51f, 42.39f, 47.63f)
                curveTo(49.51f, 51.87f, 57.78f, 60.1f, 57.78f, 79.4f)
                curveTo(57.78f, 95.85f, 49.92f, 103.38f, 42.02f, 107.74f)
                curveTo(34.64f, 111.79f, 26.11f, 113.5f, 20.65f, 114.05f)
                lineTo(0.35f, 135.19f)
                lineTo(19.47f, 155.1f)
                curveTo(30.83f, 154.73f, 50.2f, 151.87f, 67.96f, 142.1f)
                curveTo(88.71f, 130.7f, 105.68f, 110.59f, 105.68f, 79.41f)
                curveTo(105.68f, 48.24f, 90.58f, 26.49f, 69.8f, 14.12f)
                lineTo(69.81f, 14.1f)
                close()
            }
        }.build()
    return _vector!!
}

private var _vector: ImageVector? = null
private var _vectorColor: Color? = null
