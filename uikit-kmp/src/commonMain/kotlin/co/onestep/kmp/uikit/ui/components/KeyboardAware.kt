package co.onestep.kmp.uikit.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun KeyboardAware(content: @Composable () -> Unit) {
    Box(modifier = Modifier.imePadding()) {
        content()
    }
}

@Preview
@Composable
private fun KeyboardAwarePreview() {
    PreviewTheme {
        KeyboardAware {
            Text(text = "Content above keyboard")
        }
    }
}
