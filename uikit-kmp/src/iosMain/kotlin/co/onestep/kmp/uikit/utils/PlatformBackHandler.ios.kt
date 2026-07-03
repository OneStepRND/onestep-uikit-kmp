package co.onestep.kmp.uikit.utils

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS doesn't have a system back button - back navigation handled by swipe gesture / nav bar
}
