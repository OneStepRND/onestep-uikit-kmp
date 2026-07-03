package co.onestep.kmp.uikit.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import co.onestep.designsystem.typography.NunitoFontFamily
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun BoldWordsText(
    fontSize: TextUnit = 16.sp,
    fontFamily: FontFamily = NunitoFontFamily(),
    fontWeight: FontWeight = FontWeight.Normal,
    lineHeight: TextUnit = 24.sp,
    fullText: String,
    boldWords: List<String>,
) {
    val annotatedText = buildAnnotatedString {
        val tokens = fullText.split(" ")
        tokens.forEachIndexed { index, token ->
            val cleanedToken = token
                .removeSuffix(",")
                .removeSuffix(".")
                .removeSuffix("'")
                .removePrefix("'")
            if (boldWords.contains(cleanedToken)) {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(token)
                }
            } else {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                    append(token)
                }
            }
            if (index != tokens.lastIndex) {
                append(" ")
            }
        }
    }

    Text(
        text = annotatedText,
        fontSize = fontSize,
        fontFamily = fontFamily,
        fontWeight = fontWeight,
        lineHeight = lineHeight,
    )
}

@Preview
@Composable
private fun BoldWordsTextPreview() {
    PreviewTheme {
        BoldWordsText(
            fullText = "Place the phone against your thigh",
            boldWords = listOf("phone", "thigh"),
        )
    }
}
