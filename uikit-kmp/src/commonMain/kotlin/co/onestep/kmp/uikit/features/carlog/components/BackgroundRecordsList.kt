package co.onestep.kmp.uikit.features.carlog.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.carlog.models.BackgroundLogItemData
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.not_available
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun BackgroundRecordsList(
    modifier: Modifier = Modifier,
    backgroundRecords: Map<String, List<BackgroundLogItemData>>,
) {
    LazyColumn(
        modifier =
            modifier
                .fillMaxSize(),
    ) {
        backgroundRecords.forEach { (month, items) ->
            stickyHeader {
                BackgroundRecordStartElement(Modifier, month, ignoreDensity = true)
            }
            items(items) { item ->
                BackgroundLogItem(item)
            }
        }
    }
}

@Preview
@Composable
private fun BackgroundRecordsListPreview() {
    PreviewTheme {
        BackgroundRecordsList(
            backgroundRecords = mapOf(
                "March 2024" to listOf(
                    BackgroundLogItemData(day = "1", value = 75f, color = androidx.compose.ui.graphics.Color(0xFF4CAF50)),
                    BackgroundLogItemData(day = "2", value = 50f, color = androidx.compose.ui.graphics.Color(0xFFFFC107)),
                ),
            ),
        )
    }
}

@Composable
private fun BackgroundLogItem(item: BackgroundLogItemData) {
    var progress by remember { mutableFloatStateOf(0.0f) }
    var value by remember { mutableIntStateOf(0) }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
            ),
        label = "",
    )

    val animatedValue by animateIntAsState(
        targetValue = value,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
            ),
        label = "",
    )

    LaunchedEffect(Unit) {
        progress = item.value / 100
        value = item.value.toInt()
    }

    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(end = 8.dp),
    ) {
        BackgroundRecordStartElement(
            modifier = Modifier.align(CenterVertically),
            item.day,
            false,
        )
        BackgroundLogBar(
            Modifier.align(CenterVertically).weight(1f),
            animatedProgress,
            item,
        )
        OSText(
            modifier = Modifier.align(CenterVertically),
            text = if (item.value == 0f) stringResource(Res.string.not_available) else "$animatedValue",
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            fontWeight = FontWeight.W600,
        )
    }
}

@Composable
private fun BackgroundLogBar(
    modifier: Modifier = Modifier,
    animatedProgress: Float,
    item: BackgroundLogItemData,
) {
    Box(
        Modifier
            .height(33.dp)
            .padding(8.dp)
            .background(LocalOSColors.current.neutral_m2, RoundedCornerShape(50.dp))
            .then(modifier),
    ) {
        Box(
            modifier =
                Modifier
                    .height(20.dp)
                    .fillMaxWidth(animatedProgress)
                    .background(item.color, RoundedCornerShape(50.dp)),
        )
    }
}

@Composable
private fun BackgroundRecordStartElement(
    modifier: Modifier,
    text: String,
    ignoreDensity: Boolean,
) {
    Row(Modifier.wrapContentHeight().then(modifier)) {
        Box(
            Modifier
                .width(100.dp)
                .background(LocalOSColors.current.neutral_m4),
        ) {
            val currentDensity = LocalDensity.current
            val noFontScaleDensity =
                Density(
                    density = currentDensity.density,
                    fontScale = 1f,
                )
            CompositionLocalProvider(LocalDensity provides if (ignoreDensity) noFontScaleDensity else currentDensity) {
                OSText(
                    Modifier.align(Center).padding(8.dp),
                    text = text,
                    fontSize = if (text.all { it.isDigit() }) 18.sp else 18.sp,
                    fontWeight = if (text.all { it.isDigit() }) FontWeight.W400 else FontWeight.W500,
                )
            }
        }
        VerticalDivider(
            thickness = 1.dp,
            color = LocalOSColors.current.neutral_m2,
            modifier =
                Modifier
                    .height(36.dp),
        )
    }
}
