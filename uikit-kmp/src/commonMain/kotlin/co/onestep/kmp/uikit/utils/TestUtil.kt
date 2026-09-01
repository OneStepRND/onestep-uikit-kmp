package co.onestep.kmp.uikit.utils

import androidx.compose.ui.Modifier

/**
 * Marks this node with [tag] from [co.onestep.kmp.uikit.testing.OSTTestTags] so end-to-end and
 * Compose tests can select it.
 *
 * The tag is exposed the way each platform's test drivers read it, so one string is a valid
 * Maestro `id:` selector on both:
 * - **Android** — `testTag` plus `testTagsAsResourceId` on the same node, which publishes the tag
 *   as the view's `resource-id`. Setting it here rather than at the host app's root means the kit
 *   works in an app that never opted in, and inside dialogs and bottom sheets, which render in
 *   their own semantics owner and do not inherit the host's flag.
 * - **iOS** — `testTag`, which Compose Multiplatform surfaces as the node's
 *   `accessibilityIdentifier`.
 *
 * It deliberately sets no `contentDescription`: on a merged node that would replace the control's
 * own text, and a screen reader would read the test id out loud instead of the label.
 */
internal expect fun Modifier.test(tag: String): Modifier
