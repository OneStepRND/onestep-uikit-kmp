package co.onestep.kmp.uikit.bridge

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform GIF image composable.
 * Android: Coil AsyncImage with GIF decoder, iOS: native image view
 */
@Composable
expect fun PlatformGifImage(url: String, contentDescription: String, modifier: Modifier)
