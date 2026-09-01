package co.onestep.kmp.uikit.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

// Compose Multiplatform publishes the testTag as the node's accessibilityIdentifier, which is what
// the iOS Maestro driver matches on; there is no resource-id equivalent to opt into.
internal actual fun Modifier.test(tag: String): Modifier = this.testTag(tag)
