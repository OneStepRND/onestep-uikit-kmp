package co.onestep.kmp.uikit.bridge

import androidx.compose.runtime.Composable

@Composable
actual fun SystemBarEffect(darkIcons: Boolean) {
    // No-op on iOS - status bar managed by SwiftUI/UIKit host
}
