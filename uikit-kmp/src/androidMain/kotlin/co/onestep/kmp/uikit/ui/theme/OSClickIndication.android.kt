package co.onestep.kmp.uikit.ui.theme

import androidx.compose.foundation.Indication
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable

/** Android keeps the familiar Material ink ripple. */
actual fun osClickIndication(bounded: Boolean): Indication = ripple(bounded = bounded)

/** Android keeps the default Material ripple configuration. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun osRippleConfiguration(): RippleConfiguration? = RippleConfiguration()
