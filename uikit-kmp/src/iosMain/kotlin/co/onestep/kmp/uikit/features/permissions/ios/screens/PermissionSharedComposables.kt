package co.onestep.kmp.uikit.features.permissions.ios.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import co.onestep.kmp.uikit.testing.OSTTestTags
import co.onestep.kmp.uikit.utils.test
import androidx.compose.ui.unit.sp
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.components.TertiaryButton
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.features.permissions.ios.components.DataUsageInfoSheet
import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.darwin.NSObjectProtocol

/**
 * Registers an NSNotificationCenter observer for UIApplicationDidBecomeActiveNotification
 * and calls [onBecameActive] once each time the app returns to the foreground.
 *
 * Disposes the observer automatically when this composable leaves the composition.
 */
@Composable
internal fun ObserveReturnFromSettings(onBecameActive: () -> Unit) {
    var becameActive by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        val observer: NSObjectProtocol = NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = null,
        ) { _ ->
            becameActive = true
        }
        onDispose {
            NSNotificationCenter.defaultCenter.removeObserver(observer)
        }
    }
    LaunchedEffect(becameActive) {
        if (becameActive) {
            becameActive = false
            onBecameActive()
        }
    }
}

/** Close (✕) button rendered flush to the end of a Row at the top of a permission screen. */
@Composable
internal fun PermissionCloseButton(onDismiss: () -> Unit) {
    val colors = LocalOSColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.test(OSTTestTags.Permissions.CLOSE_BUTTON),
        ) {
            OSText(
                text = "✕",
                fontSize = 20.sp,
                color = colors.neutral_p1,
            )
        }
    }
}

/**
 * "How is my data used?" button plus the sheet it opens, self-managing visibility state.
 *
 * @param description Explanation text shown inside the [DataUsageInfoSheet].
 */
@Composable
internal fun DataUsageFooter(description: String) {
    var showDataUsage by remember { mutableStateOf(false) }
    TertiaryButton(
        text = "How is my data used?",
        onClick = { showDataUsage = true },
        modifier = Modifier.test(OSTTestTags.Permissions.DATA_USAGE_BUTTON),
    )
    if (showDataUsage) {
        DataUsageInfoSheet(
            description = description,
            onDismiss = { showDataUsage = false },
        )
    }
}
