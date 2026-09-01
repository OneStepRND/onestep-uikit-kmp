package co.onestep.kmp.uikit.utils

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId

// testTagsAsResourceId is resolved by walking up from the tagged node, so setting both properties
// in one semantics block is enough to publish this node's tag as its resource-id — no ancestor
// opt-in required, which is what makes the kit's tags visible inside a host app that never set the
// flag, and inside dialogs/bottom sheets that compose in their own semantics owner.
@OptIn(ExperimentalComposeUiApi::class)
internal actual fun Modifier.test(tag: String): Modifier =
    this.semantics {
        testTag = tag
        testTagsAsResourceId = true
    }
