package co.onestep.kmp.uikit.features.summary.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.summary.models.MainParamItem
import co.onestep.designsystem.components.OSText
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit.utils.mainParamItem
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.steps
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun MainParamCardValues(
    modifier: Modifier,
    mainParamItem: MainParamItem,
) {
    Row(
        modifier =
            modifier
                .then(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                ),
        horizontalArrangement = Arrangement.Center,
    ) {
        MainParamCardValue(
            value = mainParamItem.steps.toString(),
            units = stringResource(Res.string.steps),
        )
        Spacer(modifier = Modifier.width(Variables.GapL))
        MainParamCardValue(
            value = mainParamItem.duration,
            units = mainParamItem.durationUnits,
        )
    }
}

@Composable
fun MainParamCardValue(
    modifier: Modifier = Modifier,
    value: String,
    units: String,
) {
    Column(modifier = modifier) {
        MainParamCardDataValue(
            Modifier.align(Alignment.CenterHorizontally),
            value,
        )
        Spacer(modifier = Modifier.width(5.dp))
        MainParamCardDataUnit(Modifier.align(Alignment.CenterHorizontally), units)
    }
}

@Composable
private fun MainParamCardDataUnit(
    modifier: Modifier = Modifier,
    units: String,
) {
    OSText(
        modifier = modifier,
        text = units,
        fontWeight = FontWeight.W400,
        fontSize = 16.sp,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun MainParamCardDataValue(
    modifier: Modifier = Modifier,
    value: String,
) {
    OSText(
        modifier = modifier,
        text = value,
        fontSize = 20.sp,
        fontWeight = FontWeight.W700,
    )
}

@Preview
@Composable
private fun MainParamCardValuesPreview() {
    PreviewTheme {
        MainParamCardValues(
            modifier = Modifier,
            mainParamItem = mainParamItem,
        )
    }
}
