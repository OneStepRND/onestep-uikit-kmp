package co.onestep.kmp.uikit.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.designsystem.theme.Variables
import org.jetbrains.compose.ui.tooling.preview.Preview

// ─────────────────────────────────────────────────────────────────────────────
// Token model
// ─────────────────────────────────────────────────────────────────────────────

sealed interface StyledToken

data class StyledSegment(
    val text: String,
    val color: Color = Color.Unspecified,
    val fontSize: TextUnit = TextUnit.Unspecified,
    val fontWeight: FontWeight? = null,
) : StyledToken

object LineBreak : StyledToken

// ─────────────────────────────────────────────────────────────────────────────
// Private helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun splitLines(tokens: List<StyledToken>): List<List<StyledSegment>> {
    val lines = mutableListOf<MutableList<StyledSegment>>()
    var current = mutableListOf<StyledSegment>()
    tokens.forEach { token ->
        when (token) {
            is StyledSegment -> current += token
            LineBreak -> {
                lines += current
                current = mutableListOf()
            }
        }
    }
    lines += current
    return lines
}

private fun buildAnnotatedStringFromSegments(
    segments: List<StyledSegment>,
    defaultStyle: TextStyle,
): AnnotatedString =
    buildAnnotatedString {
        segments.forEach { segment ->
            val style =
                SpanStyle(
                    color = if (segment.color.isSpecified) segment.color else defaultStyle.color,
                    fontSize = if (segment.fontSize != TextUnit.Unspecified) segment.fontSize else defaultStyle.fontSize,
                    fontWeight = segment.fontWeight ?: defaultStyle.fontWeight,
                )
            withStyle(style) {
                append(segment.text)
            }
        }
    }

// ─────────────────────────────────────────────────────────────────────────────
// Core composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun InstructionParagraph(
    tokens: List<StyledToken>,
    modifier: Modifier = Modifier,
    defaultStyle: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    val lines = splitLines(tokens)
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        lines.forEach { line ->
            if (line.isNotEmpty()) {
                Text(
                    text = buildAnnotatedStringFromSegments(line, defaultStyle),
                    style =
                        defaultStyle.copy(
                            color = Color.Unspecified,
                            fontSize = TextUnit.Unspecified,
                            fontWeight = null,
                        ),
                    textAlign = defaultStyle.textAlign,
                )
            }
        }
    }
}

@Composable
fun BulletInstruction(
    tokens: List<StyledToken>,
    modifier: Modifier = Modifier,
    defaultStyle: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    Row(
        modifier = modifier.wrapContentWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "\u2022",
            style = defaultStyle.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.alignByBaseline(),
            fontSize = 20.sp,
            color = LocalOSColors.current.neutral_p2,
        )
        Spacer(Modifier.width(4.dp))
        InstructionParagraph(
            tokens = tokens,
            defaultStyle = defaultStyle,
            modifier = Modifier.alignByBaseline(),
        )
    }
}

@Composable
fun NumberedInstruction(
    index: Int,
    tokens: List<StyledToken>,
    modifier: Modifier = Modifier,
    defaultStyle: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    Row(
        modifier = modifier.wrapContentWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$index.",
            style = defaultStyle.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.alignByBaseline(),
        )
        Spacer(Modifier.width(4.dp))
        InstructionParagraph(
            tokens = tokens,
            defaultStyle = defaultStyle,
            modifier = Modifier.alignByBaseline(),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Unified entry point
// ─────────────────────────────────────────────────────────────────────────────

sealed class InstructionContent {
    data class Paragraph(
        val tokens: List<StyledToken>,
    ) : InstructionContent()

    data class Numbered(
        val items: List<List<StyledToken>>,
    ) : InstructionContent()

    data class Bulleted(
        val items: List<List<StyledToken>>,
    ) : InstructionContent()

    data class SelectiveBulleted(
        val items: List<BulletedItem>,
    ) : InstructionContent()
}

data class BulletedItem(
    val tokens: List<StyledToken>,
    val isBulleted: Boolean = true,
)

@Composable
fun Instructions(
    content: InstructionContent,
    modifier: Modifier = Modifier,
    defaultStyle: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    when (content) {
        is InstructionContent.Paragraph ->
            InstructionParagraph(
                tokens = content.tokens,
                defaultStyle = defaultStyle,
                modifier = modifier,
            )

        is InstructionContent.Numbered ->
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                content.items.forEachIndexed { idx, itemTokens ->
                    NumberedInstruction(
                        index = idx + 1,
                        tokens = itemTokens,
                        defaultStyle = defaultStyle,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = Variables.GapM),
                    )
                }
            }

        is InstructionContent.Bulleted ->
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                content.items.forEachIndexed { idx, itemTokens ->
                    BulletInstruction(
                        tokens = itemTokens,
                        defaultStyle = defaultStyle,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = Variables.GapM),
                    )
                }
            }

        is InstructionContent.SelectiveBulleted ->
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                content.items.forEach { item ->
                    if (item.isBulleted) {
                        BulletInstruction(
                            tokens = item.tokens,
                            defaultStyle = defaultStyle,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = Variables.GapM),
                        )
                    } else {
                        InstructionParagraph(
                            tokens = item.tokens,
                            defaultStyle = defaultStyle,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = Variables.GapM),
                        )
                    }
                }
            }
    }
}

@Preview
@Composable
private fun InstructionParagraphPreview() {
    PreviewTheme {
        InstructionParagraph(
            tokens = listOf(
                StyledSegment("Tap "),
                StyledSegment("'Continue'", fontWeight = FontWeight.Bold),
                StyledSegment(" to proceed"),
            ),
        )
    }
}
