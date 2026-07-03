package co.onestep.kmp.uikit.ui.components

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import co.onestep.kmp.uikit.utils.rtlMirror
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * An Icon wrapper that automatically mirrors horizontally in RTL layout.
 * Use for directional icons like back arrows and chevrons.
 */
@Composable
fun MirroredIcon(
    resource: DrawableResource,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
) {
    Icon(
        painter = painterResource(resource),
        contentDescription = contentDescription,
        modifier = modifier.rtlMirror(),
        tint = tint,
    )
}
