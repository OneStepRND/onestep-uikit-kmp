package co.onestep.kmp.uikit.features.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import co.onestep.kmp.uikit.utils.test
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import co.onestep.kmp.uikit.navigation.UIktNavDisplay
import co.onestep.kmp.uikit.navigation.UIktNavSavedStateConfiguration
import co.onestep.kmp.uikit.navigation.platformProvidesBackGesture
import co.onestep.kmp.uikit.navigation.pop
import co.onestep.kmp.uikit.utils.UIktDestination
import kotlinx.serialization.Serializable
import org.jetbrains.compose.ui.tooling.preview.Preview

@Serializable
internal data object PushPopDemoFirstDestination : UIktDestination

@Serializable
internal data object PushPopDemoSecondDestination : UIktDestination

@Serializable
internal data object PushPopDemoThirdDestination : UIktDestination

/**
 * Test-harness demo of the Cupertino push/pop transition
 * ([co.onestep.kmp.uikit.navigation.CupertinoTransition]): a three-screen stack showing the
 * push animation (slide-in over a parallaxed, dimmed underlay with a leading-edge shadow), the
 * pop reverse, and the interactive edge-swipe back.
 *
 * Public only so the Android/iOS test apps can reach it — not intended as consumer API.
 *
 * @param onDismiss called when back is invoked on the root screen.
 * @param forceInteractiveBackGesture enables the Compose edge-swipe even where the platform has
 *   its own back gesture (Android) so the swipe itself can be exercised in the harness.
 */
@Composable
fun OSTPushPopDemo(
    onDismiss: () -> Unit,
    forceInteractiveBackGesture: Boolean = false,
) {
    MaterialTheme {
        val backStack = rememberNavBackStack(
            UIktNavSavedStateConfiguration,
            PushPopDemoFirstDestination,
        )
        val onBack = { if (!backStack.pop()) onDismiss() }
        UIktNavDisplay(
            backStack = backStack,
            onBack = onBack,
            modifier = Modifier.fillMaxSize(),
            interactiveBackGesture = forceInteractiveBackGesture || !platformProvidesBackGesture,
            entryProvider = entryProvider {
                demoScreen<PushPopDemoFirstDestination>(
                    title = "First",
                    tint = Color(0xFFE8F0FE),
                    tag = "demo.first",
                    onPush = { backStack.add(PushPopDemoSecondDestination) },
                    onBack = onBack,
                )
                demoScreen<PushPopDemoSecondDestination>(
                    title = "Second",
                    tint = Color(0xFFE6F4EA),
                    tag = "demo.second",
                    onPush = { backStack.add(PushPopDemoThirdDestination) },
                    onBack = onBack,
                )
                demoScreen<PushPopDemoThirdDestination>(
                    title = "Third",
                    tint = Color(0xFFFCE8E6),
                    tag = "demo.third",
                    onPush = null,
                    onBack = onBack,
                )
            },
        )
    }
}

private inline fun <reified T : NavKey> EntryProviderScope<NavKey>.demoScreen(
    title: String,
    tint: Color,
    tag: String,
    noinline onPush: (() -> Unit)?,
    noinline onBack: () -> Unit,
) {
    entry<T> {
        DemoScreen(title = title, tint = tint, tag = tag, onPush = onPush, onBack = onBack)
    }
}

@Composable
private fun DemoScreen(
    title: String,
    tint: Color,
    tag: String,
    onPush: (() -> Unit)?,
    onBack: () -> Unit,
) {
    Surface(color = tint, modifier = Modifier.fillMaxSize().test(tag)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = title, fontSize = 34.sp)
            Text(
                text = "Swipe from the leading edge to go back interactively, " +
                    "or use the buttons.",
                fontSize = 14.sp,
                color = Color.Gray,
            )
            if (onPush != null) {
                Button(
                    onClick = onPush,
                    modifier = Modifier.test("$tag.push"),
                ) { Text("Push next screen") }
            }
            TextButton(
                onClick = onBack,
                modifier = Modifier.test("$tag.back"),
            ) { Text("Pop back") }
            Spacer(Modifier.height(8.dp))
            // Filler rows make the parallax + scrim on the underlying screen easy to see.
            repeat(12) { index ->
                Text(
                    text = "$title · row ${index + 1}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .clickable { }
                        .padding(16.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun DemoScreenPreview() {
    DemoScreen(
        title = "First",
        tint = Color(0xFFE8F0FE),
        tag = "demo.first",
        onPush = {},
        onBack = {},
    )
}
