package co.onestep.kmp.uikit.ui.typography

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.noir_no1
import co.onestep.kmp.uikit_kmp.generated.resources.noir_no1_bold
import co.onestep.kmp.uikit_kmp.generated.resources.noir_no1_demibold
import org.jetbrains.compose.resources.Font

@Composable
fun NoirFontFamily() = FontFamily(
    Font(Res.font.noir_no1, FontWeight.Normal),
    Font(Res.font.noir_no1_bold, FontWeight.Bold),
    Font(Res.font.noir_no1_demibold, FontWeight.W500),
)
