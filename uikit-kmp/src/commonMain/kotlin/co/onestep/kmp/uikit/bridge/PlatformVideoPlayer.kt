package co.onestep.kmp.uikit.bridge

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform video player composable.
 * Android: Media3 ExoPlayer, iOS: AVPlayer via UIKitView
 */
@Composable
expect fun PlatformVideoPlayer(url: String, modifier: Modifier)
