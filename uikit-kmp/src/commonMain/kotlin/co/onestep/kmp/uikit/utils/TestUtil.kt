package co.onestep.kmp.uikit.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

internal fun Modifier.test(tag: String) = this.testTag(tag).semantics { contentDescription = tag }
