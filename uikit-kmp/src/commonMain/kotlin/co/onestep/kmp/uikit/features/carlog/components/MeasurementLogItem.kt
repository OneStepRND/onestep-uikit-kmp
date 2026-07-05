package co.onestep.kmp.uikit.features.carlog.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import co.onestep.kmp.uikit.ui.theme.osClickIndication
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.carlog.models.MeasurementItemData
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit.utils.capitalizeWordsAfterComma
import co.onestep.kmp.uikit.utils.isRtl
import co.onestep.kmp.uikit.utils.test
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.cd_walk_log_item
import co.onestep.kmp.uikit_kmp.generated.resources.assistive_device_key_display
import co.onestep.kmp.uikit_kmp.generated.resources.duration_key_display
import co.onestep.kmp.uikit_kmp.generated.resources.ic_walks
import co.onestep.kmp.uikit_kmp.generated.resources.level_of_assistance
import co.onestep.kmp.uikit_kmp.generated.resources.notes
import co.onestep.kmp.uikit_kmp.generated.resources.tags_key_display
import co.onestep.kmp.uikit.models.OSTActivityType
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

const val WALK_LOG_ITEM = "Walk log item"

@Composable
internal fun MeasurementLogItem(
    modifier: Modifier = Modifier,
    measurementItemData: MeasurementItemData,
    onClick: (String) -> Unit = {},
) {
    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = LocalOSColors.current.neutral_m5,
                contentColor = LocalOSColors.current.neutral_p3,
            ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, LocalOSColors.current.neutral_m1),
        modifier =
            modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(Variables.GapL)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = osClickIndication(bounded = true),
                    onClick = { onClick(measurementItemData.id) },
                ).test(WALK_LOG_ITEM),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(LocalOSColors.current.neutral_m5),
        ) {
            Row(Modifier.padding(Variables.GapL)) {
                Box(
                    Modifier
                        .size(36.dp),
                ) {
                    Icon(
                        modifier =
                            Modifier
                                .align(Alignment.Center),
                        painter = painterResource(measurementItemData.icon),
                        contentDescription = stringResource(Res.string.cd_walk_log_item),
                        tint = LocalOSColors.current.neutral_p3,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OSText(
                            modifier = Modifier.weight(1f),
                            text = measurementItemData.title,
                            fontSize = 20.sp,
                            lineHeight = 24.sp,
                            maxLines = 2,
                            fontWeight = FontWeight.W600,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.width(8.dp))
                        Row(
                            Modifier.wrapContentWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OSText(
                                text = measurementItemData.time,
                            )
                            OSText(
                                text = if (isRtl()) "\u2039" else "\u203A",
                                modifier =
                                    Modifier
                                        .align(Alignment.CenterVertically),
                                fontSize = 24.sp,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OSText(
                        text = measurementItemData.mainParam,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.W700,
                        color = LocalOSColors.current.info_p1,
                    )
                    if (measurementItemData.duration != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CarLogItemMetadata(
                            stringResource(Res.string.duration_key_display),
                            measurementItemData.duration,
                        )
                    }
                    if (measurementItemData.tags?.isNotEmpty() == true) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CarLogItemMetadata(
                            stringResource(Res.string.tags_key_display),
                            measurementItemData.tags,
                        )
                    }
                    measurementItemData.assistiveDevice?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        CarLogItemMetadata(
                            stringResource(Res.string.assistive_device_key_display),
                            it,
                        )
                    }
                    measurementItemData.levelOfAssistance?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        CarLogItemMetadata(
                            stringResource(Res.string.level_of_assistance),
                            it,
                        )
                    }
                    measurementItemData.note?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        CarLogItemMetadata(stringResource(Res.string.notes), it, dropLine = true)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Preview
@Composable
private fun MeasurementLogItemPreview() {
    PreviewTheme {
        MeasurementLogItem(
            measurementItemData = MeasurementItemData(
                id = "1",
                day = "Monday",
                type = OSTActivityType.WALK,
                title = "Walk",
                time = "10:30 AM",
                icon = Res.drawable.ic_walks,
                mainParam = "Score: 85",
                duration = "2 min 30 sec",
                tags = "Outdoor",
            ),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CarLogItemMetadata(
    key: String,
    value: String,
    dropLine: Boolean = false,
) {
    if (dropLine) {
        FlowRow {
            OSText(
                text = key,
                color = LocalOSColors.current.neutral_p1,
            )
            Spacer(modifier = Modifier.width(8.dp))
            OSText(
                text = value.capitalizeWordsAfterComma(),
            )
        }
    } else {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = LocalOSColors.current.neutral_p1)) {
                    append(key)
                }
                append(" ")
                withStyle(SpanStyle(color = LocalOSColors.current.neutral_0)) {
                    append(value.capitalizeWordsAfterComma())
                }
            },
        )
    }
}
