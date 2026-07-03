package co.onestep.kmp.uikit.features.summary.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData
import co.onestep.kmp.uikit.features.summary.models.EmptyStateData
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.cd_empty_state_icon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun EmptyStatePreview() {
    PreviewTheme {
        EmptyState(
            data = EmptyStateData(
                subtitle = TextData(
                    text = "No data available for this measurement.",
                    textSize = 18.sp,
                    fontWeight = FontWeight.W400,
                ),
            ),
            listState = rememberLazyListState(),
        )
    }
}

@Composable
internal fun EmptyState(
    data: EmptyStateData,
    listState: LazyListState,
    lazyColumnHeight: Dp? = null,
) {
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier =
                Modifier
                    .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
        ) {
            data.icon?.let {
                item {
                    Image(
                        painterResource(it.icon),
                        contentDescription = stringResource(Res.string.cd_empty_state_icon),
                        modifier =
                            Modifier
                                .fillParentMaxWidth()
                                .height(data.icon.iconSize ?: 50.dp)
                                .align(Center),
                        colorFilter =
                            ColorFilter.tint(
                                LocalOSColors.current.primary_0,
                                blendMode = BlendMode.Dst,
                            ),
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(Variables.GapL))
            }

            data.title?.let {
                item {
                    OSText(
                        text = it.text,
                        fontWeight = it.fontWeight,
                        fontSize = it.textSize,
                        textAlign = TextAlign.Center,
                        modifier =
                            Modifier
                                .fillParentMaxWidth()
                                .padding(horizontal = Variables.GapL)
                                .align(Center),
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(Variables.GapL))
            }

            item {
                OSText(
                    text = data.subtitle.text,
                    fontWeight = data.subtitle.fontWeight,
                    fontSize = data.subtitle.textSize,
                    textAlign = TextAlign.Center,
                    color = LocalOSColors.current.neutral_p1,
                    modifier =
                        Modifier
                            .fillParentMaxWidth()
                            .padding(horizontal = Variables.GapL)
                            .align(Center),
                )
            }
        }
    }
}
