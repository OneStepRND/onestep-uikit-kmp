package co.onestep.kmp.uikit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import co.onestep.designsystem.components.ButtonVariant
import co.onestep.designsystem.components.DangerButton
import co.onestep.designsystem.components.OSButtonSize
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.components.PrimaryButton
import co.onestep.designsystem.components.SecondaryButton
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit.testing.OSTTestTags
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.kmp.uikit.utils.test
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * The design-system `OSPopup` layout, with a test tag on every control.
 *
 * `OSPopup` takes one `modifier`, for its card, so its buttons, close icon and checkbox cannot be
 * tagged from here (see `docs/TestTags.md`). This mirrors its layout and tokens exactly, so a
 * popup moved onto it looks the same, and adds a tag per control so an E2E flow can select them
 * by id rather than by localized text.
 *
 * shortcut: a copy of `OSPopupContent` from design-system-kmp 1.3.2, which drifts if the design
 * system restyles its popup. Upgrade path: give `OSPopup` per-control modifiers or tags in
 * design-system-kmp, move the callers back to it and delete this file.
 *
 * @param modifier Applied to the card; the popup's own test tag goes here.
 * @param onCancel Called by the cancel button. Defaults to [onDismissRequest], as in `OSPopup`.
 * @param onCheckboxCheckedChange Null means no checkbox, as in `OSPopup`.
 */
@Composable
internal fun TaggedPopup(
    onDismissRequest: () -> Unit,
    title: String,
    confirmButtonText: String,
    confirmButtonVariant: ButtonVariant,
    confirmButtonTestTag: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    closeIcon: ImageVector? = null,
    closeButtonTestTag: String? = null,
    cancelButtonText: String? = null,
    cancelButtonVariant: ButtonVariant = ButtonVariant.Primary,
    cancelButtonTestTag: String? = null,
    onCancel: () -> Unit = onDismissRequest,
    checkboxText: String? = null,
    checkboxChecked: Boolean = false,
    checkboxTestTag: String? = null,
    onCheckboxCheckedChange: ((Boolean) -> Unit)? = null,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        TaggedPopupContent(
            onDismissRequest = onDismissRequest,
            title = title,
            confirmButtonText = confirmButtonText,
            confirmButtonVariant = confirmButtonVariant,
            confirmButtonTestTag = confirmButtonTestTag,
            onConfirm = onConfirm,
            modifier = modifier,
            description = description,
            closeIcon = closeIcon,
            closeButtonTestTag = closeButtonTestTag,
            cancelButtonText = cancelButtonText,
            cancelButtonVariant = cancelButtonVariant,
            cancelButtonTestTag = cancelButtonTestTag,
            onCancel = onCancel,
            checkboxText = checkboxText,
            checkboxChecked = checkboxChecked,
            checkboxTestTag = checkboxTestTag,
            onCheckboxCheckedChange = onCheckboxCheckedChange,
        )
    }
}

/** The popup card without its [Dialog] window, split out so it renders in previews. */
@Composable
private fun TaggedPopupContent(
    onDismissRequest: () -> Unit,
    title: String,
    confirmButtonText: String,
    confirmButtonVariant: ButtonVariant,
    confirmButtonTestTag: String,
    onConfirm: () -> Unit,
    modifier: Modifier,
    description: String?,
    closeIcon: ImageVector?,
    closeButtonTestTag: String?,
    cancelButtonText: String?,
    cancelButtonVariant: ButtonVariant,
    cancelButtonTestTag: String?,
    onCancel: () -> Unit,
    checkboxText: String?,
    checkboxChecked: Boolean,
    checkboxTestTag: String?,
    onCheckboxCheckedChange: ((Boolean) -> Unit)?,
) {
    val colors = LocalOSColors.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Variables.GapM),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.neutral_m5)
                .padding(Variables.GapL),
        ) {
            closeIcon?.let { vector ->
                Icon(
                    imageVector = vector,
                    contentDescription = null,
                    tint = colors.neutral_p3,
                    modifier = Modifier
                        .align(Alignment.End)
                        .size(35.dp)
                        .tagged(closeButtonTestTag)
                        .clickable { onDismissRequest() },
                )
                Spacer(modifier = Modifier.height(Variables.GapL))
            }

            OSText(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            description?.let { desc ->
                Spacer(modifier = Modifier.height(Variables.GapM))
                OSText(
                    text = desc,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    color = colors.neutral_p2,
                )
            }

            Spacer(modifier = Modifier.height(Variables.GapXL))

            PopupButton(
                text = confirmButtonText,
                variant = confirmButtonVariant,
                testTag = confirmButtonTestTag,
                onClick = onConfirm,
            )

            cancelButtonText?.let { cancelText ->
                Spacer(modifier = Modifier.height(Variables.GapL))
                PopupButton(
                    text = cancelText,
                    variant = cancelButtonVariant,
                    testTag = cancelButtonTestTag,
                    onClick = onCancel,
                )
            }

            if (onCheckboxCheckedChange != null) {
                Spacer(modifier = Modifier.height(Variables.GapXL))
                PopupCheckbox(
                    text = checkboxText.orEmpty(),
                    checked = checkboxChecked,
                    onCheckedChange = onCheckboxCheckedChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .tagged(checkboxTestTag),
                )
            }
        }
    }
}

@Composable
private fun PopupButton(
    text: String,
    variant: ButtonVariant,
    testTag: String?,
    onClick: () -> Unit,
) {
    val modifier = Modifier.fillMaxWidth().tagged(testTag)
    when (variant) {
        ButtonVariant.Primary -> PrimaryButton(
            text = text,
            size = OSButtonSize.Small,
            modifier = modifier,
            fillMaxWidth = true,
            onClick = onClick,
        )

        ButtonVariant.Secondary -> SecondaryButton(
            text = text,
            size = OSButtonSize.Small,
            modifier = modifier,
            fillMaxWidth = true,
            onClick = onClick,
        )

        ButtonVariant.Danger -> DangerButton(
            text = text,
            size = OSButtonSize.Small,
            modifier = modifier,
            fillMaxWidth = true,
            onClick = onClick,
        )
    }
}

/**
 * The popup's "Don't show again"-style checkbox, drawn as in `OSPopup` (20dp box, r-4 corners,
 * own checkmark glyph). Unlike the original it is a `toggleable` with the checkbox role, so a
 * screen reader announces its state; it looks and behaves the same.
 */
@Composable
private fun PopupCheckbox(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalOSColors.current
    Row(
        modifier = modifier.toggleable(
            value = checked,
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            role = Role.Checkbox,
            onValueChange = onCheckedChange,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(Variables.RadiusR4))
                .background(if (checked) colors.primary_p3_main else colors.neutral_m5)
                .border(
                    width = 1.dp,
                    color = if (checked) colors.primary_p3_main else colors.neutral_p3,
                    shape = RoundedCornerShape(Variables.RadiusR4),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Icon(
                    imageVector = PopupCheckIcon,
                    contentDescription = null,
                    tint = colors.neutral_m5,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        Spacer(modifier = Modifier.width(Variables.GapM))
        OSText(
            text = text,
            fontSize = 14.sp,
            color = colors.neutral_p3,
        )
    }
}

private fun Modifier.tagged(tag: String?): Modifier = if (tag != null) test(tag) else this

// The checkmark `OSPopup` draws, so the kit needs no material-icons dependency. It is tinted
// where it is drawn; the SolidColor is a placeholder.
private val PopupCheckIcon: ImageVector =
    ImageVector.Builder(
        name = "PopupCheck",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(6f, 12.5f)
            lineTo(10f, 16.5f)
            lineTo(18f, 7.5f)
        }
    }.build()

@Preview
@Composable
private fun TaggedPopupPreview() {
    PreviewTheme {
        TaggedPopupContent(
            onDismissRequest = {},
            title = "Short hallway",
            confirmButtonText = "Start test",
            confirmButtonVariant = ButtonVariant.Secondary,
            confirmButtonTestTag = OSTTestTags.RecordFlow.HALLWAY_WARNING_START_BUTTON,
            onConfirm = {},
            modifier = Modifier,
            description = "A hallway shorter than the recommended length can affect the results.",
            closeIcon = null,
            closeButtonTestTag = null,
            cancelButtonText = "Edit hallway length",
            cancelButtonVariant = ButtonVariant.Primary,
            cancelButtonTestTag = OSTTestTags.RecordFlow.HALLWAY_WARNING_EDIT_BUTTON,
            onCancel = {},
            checkboxText = "Don't show again",
            checkboxChecked = true,
            checkboxTestTag = OSTTestTags.RecordFlow.HALLWAY_WARNING_DONT_SHOW_CHECKBOX,
            onCheckboxCheckedChange = {},
        )
    }
}
