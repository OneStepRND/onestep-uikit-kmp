package co.onestep.kmp.uikit.bridge

import androidx.compose.runtime.Composable

/**
 * Platform system bar effect.
 * Android: sets status bar color/icons, iOS: no-op or UIApplication setStatusBarStyle
 */
@Composable
expect fun SystemBarEffect(darkIcons: Boolean)
