package co.onestep.kmp.uikit.bridge

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.currentItem
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.seekToTime
import platform.AVKit.AVPlayerViewController
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.UIColor
import platform.UIKit.UIViewController
import platform.UIKit.addChildViewController
import platform.UIKit.didMoveToParentViewController

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformVideoPlayer(url: String, modifier: Modifier) {
    val player = remember(url) {
        NSURL.URLWithString(url)?.let { AVPlayer(uRL = it) }
    }

    val avPlayerViewController = remember {
        AVPlayerViewController().apply {
            videoGravity = AVLayerVideoGravityResizeAspectFill
            showsPlaybackControls = false
        }
    }

    if (player != null) {
        avPlayerViewController.player = player

        // Auto-loop via notification observer
        DisposableEffect(player) {
            val observer = NSNotificationCenter.defaultCenter.addObserverForName(
                name = AVPlayerItemDidPlayToEndTimeNotification,
                `object` = player.currentItem,
                queue = null,
            ) { _ ->
                player.seekToTime(CMTimeMakeWithSeconds(0.0, 600))
                player.play()
            }
            onDispose {
                NSNotificationCenter.defaultCenter.removeObserver(observer)
                player.pause()
            }
        }

        UIKitView(
            modifier = modifier.fillMaxSize(),
            factory = {
                val containerVC = UIViewController().apply {
                    view.backgroundColor = UIColor.clearColor
                }
                containerVC.addChildViewController(avPlayerViewController)
                avPlayerViewController.view.translatesAutoresizingMaskIntoConstraints = false
                avPlayerViewController.view.backgroundColor = UIColor.clearColor
                avPlayerViewController.view.opaque = false

                containerVC.view.addSubview(avPlayerViewController.view)

                NSLayoutConstraint.activateConstraints(
                    listOf(
                        avPlayerViewController.view.topAnchor.constraintEqualToAnchor(containerVC.view.topAnchor),
                        avPlayerViewController.view.bottomAnchor.constraintEqualToAnchor(containerVC.view.bottomAnchor),
                        avPlayerViewController.view.leadingAnchor.constraintEqualToAnchor(containerVC.view.leadingAnchor),
                        avPlayerViewController.view.trailingAnchor.constraintEqualToAnchor(containerVC.view.trailingAnchor),
                    ),
                )

                avPlayerViewController.didMoveToParentViewController(containerVC)
                player.play()

                containerVC.view
            },
        )
    }
}
