package co.onestep.kmp.uikit.ui.theme

import androidx.compose.foundation.Indication
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RippleConfiguration
import androidx.compose.runtime.Composable

/**
 * Platform-appropriate press indication for clickable surfaces.
 *
 * - **Android** returns the Material [ripple] — the expanding ink ripple Android users expect.
 * - **iOS** returns a UIKit-style press: the content dims to ~0.7 opacity on touch-down and
 *   restores on release (plus a light haptic), with no traveling ripple.
 *
 * Use this at every `Modifier.clickable`/`combinedClickable` call site instead of `ripple(...)`.
 * Clickables that omit an explicit `indication` pick this up via `LocalIndication`, which
 * [OneStepUiKitTheme] provides.
 *
 * @param bounded honoured by the Android ripple; ignored by the iOS dim.
 */
expect fun osClickIndication(bounded: Boolean = true): Indication

/**
 * Platform-appropriate global Material ripple configuration, provided via `LocalRippleConfiguration`.
 *
 * This governs Material3 components that draw their own ripple directly (e.g. `Tab`, `Checkbox`,
 * `IconButton`, `Card(onClick = …)`) rather than reading `LocalIndication`.
 *
 * - **Android** returns the default ripple config (unchanged).
 * - **iOS** returns `null`, which fully disables the Material ripple (a transparent-color config
 *   still leaves a visible state layer), so `Card`/`Surface`/etc. stop showing the Android-style
 *   animation on iOS.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
expect fun osRippleConfiguration(): RippleConfiguration?
