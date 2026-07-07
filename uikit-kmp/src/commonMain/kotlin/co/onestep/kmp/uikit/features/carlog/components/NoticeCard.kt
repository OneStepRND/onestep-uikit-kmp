package co.onestep.kmp.uikit.features.carlog.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import co.onestep.kmp.uikit.ui.theme.osClickIndication
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.carlog.models.NoticeCardData
import co.onestep.kmp.uikit.features.recordFlow.screensData.PrimaryButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.designsystem.theme.Variables
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun NoticeCard(
    modifier: Modifier = Modifier,
    noticeCardData: NoticeCardData,
    onCardAction: (() -> Unit)? = null,
) {
    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = LocalOSColors.current.primary_m3,
                contentColor = LocalOSColors.current.brand_text,
            ),
        shape = RoundedCornerShape(8.dp),
        border =
            BorderStroke(
                width = 1.dp,
                color = LocalOSColors.current.neutral_p1,
            ),
        modifier =
            Modifier
                .wrapContentHeight()
                .then(modifier),
    ) {
        Column(
            Modifier.padding(Variables.GapL),
        ) {
            OSText(
                text = noticeCardData.textData.text,
                fontSize = noticeCardData.textData.textSize,
                fontWeight = noticeCardData.textData.fontWeight,
            )
            Spacer(modifier = Modifier.height(Variables.GapL))
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(40.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .wrapContentWidth()
                            .wrapContentHeight()
                            .background(
                                LocalOSColors.current.primary_p3_main,
                                shape = RoundedCornerShape(4.dp),
                            ).align(Alignment.CenterEnd)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = osClickIndication(bounded = true),
                                onClick = {
                                    onCardAction?.invoke()
                                    noticeCardData.button.action()
                                },
                            ),
                ) {
                    OSText(
                        text = noticeCardData.button.text.text,
                        fontSize = noticeCardData.button.text.textSize,
                        fontWeight = noticeCardData.button.text.fontWeight,
                        textAlign = TextAlign.Center,
                        color = LocalOSColors.current.neutral_m4,
                        modifier =
                            Modifier
                                .padding(horizontal = Variables.GapL, vertical = 8.dp)
                                .wrapContentWidth(),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun NoticeCardPreview() {
    PreviewTheme {
        NoticeCard(
            noticeCardData = NoticeCardData(
                textData = TextData(
                    text = "Enable background monitoring to track your activity throughout the day.",
                    textSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                ),
                button = PrimaryButtonData(
                    text = TextData(
                        text = "Enable",
                        textSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    action = {},
                ),
            ),
        )
    }
}
