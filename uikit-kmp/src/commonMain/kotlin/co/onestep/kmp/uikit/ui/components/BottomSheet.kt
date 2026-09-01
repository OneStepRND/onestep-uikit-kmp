package co.onestep.kmp.uikit.ui.components

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.kmp.uikit.utils.test
import org.jetbrains.compose.ui.tooling.preview.Preview

@ExperimentalMaterial3Api
@Composable
internal fun BottomSheet(
    sheetState: SheetState = rememberModalBottomSheetState(),
    onDismissRequest: () -> Unit = {},
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    // A sheet composes in its own semantics owner, so its tag has to be set here rather
    // than inherited from the screen behind it.
    testTag: String? = null,
    content: @Composable () -> Unit = { },
) {
    ModalBottomSheet(
        onDismissRequest = {
            onDismissRequest()
        },
        modifier = Modifier
            .statusBarsPadding()
            .let { if (testTag != null) it.test(testTag) else it },
        dragHandle = dragHandle,
        sheetState = sheetState,
    ) {
        content()
    }
}

@ExperimentalMaterial3Api
@Composable
internal fun BottomSheet(
    sheetData: BottomSheetData,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    testTag: String? = null,
) {
    ModalBottomSheet(
        onDismissRequest = {
            sheetData.hide()
        },
        modifier = Modifier
            .statusBarsPadding()
            .let { if (testTag != null) it.test(testTag) else it },
        dragHandle = dragHandle,
        sheetState = sheetData.sheetState,
    ) {
        sheetData.sheetContent.value.invoke()
    }
}

data class BottomSheetData
    @OptIn(ExperimentalMaterial3Api::class)
    constructor(
        var showSheet: MutableState<Boolean>,
        var sheetContent: MutableState<@Composable () -> Unit>,
        val sheetState: SheetState,
    ) {
        fun show(content: @Composable () -> Unit) {
            sheetContent.value = content
            showSheet.value = true
        }

        fun hide() {
            showSheet.value = false
            sheetContent.value = {}
        }

        fun showBottomSheet(): Boolean = showSheet.value
    }

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun BottomSheetPreview() {
    PreviewTheme {
        BottomSheet(
            onDismissRequest = {},
            content = { Text(text = "Bottom sheet content") },
        )
    }
}
