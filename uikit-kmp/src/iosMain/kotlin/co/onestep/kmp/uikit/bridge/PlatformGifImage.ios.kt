package co.onestep.kmp.uikit.bridge

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.interpretCPointer
import platform.CoreFoundation.CFDataRef
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSDictionary
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.ImageIO.CGImageSourceCreateImageAtIndex
import platform.ImageIO.CGImageSourceCreateWithData
import platform.ImageIO.CGImageSourceCopyPropertiesAtIndex
import platform.ImageIO.CGImageSourceGetCount
import platform.UIKit.UIImage
import platform.UIKit.UIImageView
import platform.UIKit.UIViewContentMode

/**
 * iOS GIF image composable using UIImageView + CGImageSource for native GIF frame extraction.
 * AVPlayer cannot decode GIF files, so we use ImageIO to extract frames and animate via UIImageView.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformGifImage(url: String, contentDescription: String, modifier: Modifier) {
    UIKitView(
        modifier = modifier,
        factory = {
            val imageView = UIImageView().apply {
                contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
                clipsToBounds = true
            }

            // Load GIF data — support both https:// URLs and file:// / raw paths
            val nsUrl = NSURL.URLWithString(url) ?: NSURL.fileURLWithPath(url)
            val data = NSData.dataWithContentsOfURL(nsUrl)

            if (data != null) {
                val cfData: CFDataRef? = CFBridgingRetain(data)?.let { interpretCPointer(it.rawValue) }
                val source = CGImageSourceCreateWithData(
                    data = cfData,
                    options = null,
                )
                if (source != null) {
                    val frameCount = CGImageSourceGetCount(source).toInt()
                    if (frameCount > 1) {
                        // Animated GIF — extract all frames
                        val images = mutableListOf<UIImage>()
                        var totalDuration = 0.0

                        for (i in 0 until frameCount) {
                            val cgImage = CGImageSourceCreateImageAtIndex(source, i.toULong(), null)
                            if (cgImage != null) {
                                images.add(UIImage.imageWithCGImage(cgImage))
                            }
                            totalDuration += getGifFrameDelay(source, i)
                        }

                        if (totalDuration <= 0.0) {
                            totalDuration = frameCount * 0.1
                        }

                        imageView.animationImages = images
                        imageView.animationDuration = totalDuration
                        imageView.startAnimating()
                    } else {
                        // Single-frame — display as static image
                        val cgImage = CGImageSourceCreateImageAtIndex(source, 0u, null)
                        if (cgImage != null) {
                            imageView.image = UIImage.imageWithCGImage(cgImage)
                        }
                    }
                }
            }

            imageView
        },
    )
}

/**
 * Extract the delay time for a single GIF frame.
 * In K/N, CGImageSourceCopyPropertiesAtIndex returns a CFDictionaryRef that is
 * toll-free bridged to NSDictionary and can be cast directly.
 * Falls back to 0.1s if the property is missing.
 */
@OptIn(ExperimentalForeignApi::class)
private fun getGifFrameDelay(source: platform.ImageIO.CGImageSourceRef, index: Int): Double {
    val cfProperties = CGImageSourceCopyPropertiesAtIndex(source, index.toULong(), null)
        ?: return DEFAULT_FRAME_DELAY

    // In K/N, CFDictionaryRef is toll-free bridged — interpret as NSDictionary
    val properties = kotlinx.cinterop.interpretObjCPointer<NSDictionary>(cfProperties.rawValue)

    // The GIF properties are nested under "{GIF}" key
    val gifDict = properties.objectForKey("{GIF}") as? NSDictionary
        ?: return DEFAULT_FRAME_DELAY

    // Try UnclampedDelayTime first (more accurate), then DelayTime
    val delay = (gifDict.objectForKey("UnclampedDelayTime") as? NSNumber)?.doubleValue
        ?: (gifDict.objectForKey("DelayTime") as? NSNumber)?.doubleValue
        ?: return DEFAULT_FRAME_DELAY

    return if (delay > 0.0) delay else DEFAULT_FRAME_DELAY
}

private const val DEFAULT_FRAME_DELAY = 0.1
