package co.onestep.kmp.uikit.features.carlog.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import co.onestep.kmp.uikit.ui.theme.osClickIndication
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.carlog.components.Info
import co.onestep.kmp.uikit.features.carlog.components.NoticeCard
import co.onestep.kmp.uikit.features.carlog.models.BackgroundScreenState
import co.onestep.kmp.uikit.features.carlog.models.CarLogScreenState
import co.onestep.kmp.uikit.features.carlog.models.InAppScreenState
import co.onestep.kmp.uikit.features.carlog.models.NoticeCardType
import co.onestep.kmp.uikit.ui.components.BottomSheet
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.ic_info_circle
import co.onestep.kmp.uikit_kmp.generated.resources.walk_score_daily_average
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CareLogContentScreen(
    modifier: Modifier = Modifier,
    carLogScreenState: CarLogScreenState,
    onClickItem: (String) -> Unit = {},
) {
    val showInfoSheet = remember { mutableStateOf(false) }
    Column {
        NoticeCards(carLogScreenState)
        when {
            carLogScreenState is BackgroundScreenState.Content -> {
                CareLogContentTitle(showInfoSheet)
            }
        }

        AnimatedContent(
            targetState = carLogScreenState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "CarLogScreen",
        ) { state ->
            when (state) {
                is CarLogScreenState.Loading -> CarLogShimmer()
                is CarLogScreenState.Empty ->
                    EmptyCareLogScreen(
                        modifier = modifier,
                        emptyScreenState = state,
                    )

                is InAppScreenState.Content -> {
                    InAppCarLogScreen(
                        modifier = modifier,
                        items = state.carLogItems,
                        onClickItem,
                    )
                }

                is BackgroundScreenState.Content -> {
                    BackgroundScreen(
                        modifier = modifier,
                        backgroundRecords = state.backgroundRecords,
                    )
                }
            }
        }

        if (showInfoSheet.value) {
            carLogScreenState.infoData?.let { infoData ->
                BottomSheet(
                    dragHandle = null,
                    onDismissRequest = {
                        showInfoSheet.value = false
                    },
                ) {
                    Info(Modifier, infoData)
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.NoticeCards(carLogScreenState: CarLogScreenState) {
    carLogScreenState.noticeCards.forEach { card ->
        AnimatedVisibility(
            visible = card.isVisible,
        ) {
            NoticeCard(Modifier.padding(Variables.GapL), card) {
                if (card.type == NoticeCardType.BackgroundMonitoring) {
                    card.isVisible = false
                }
            }
        }
    }
}

@Composable
private fun CareLogContentTitle(showInfoSheet: MutableState<Boolean>) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(Variables.GapL),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        OSText(
            text = stringResource(Res.string.walk_score_daily_average),
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.W600,
            // Foreground role (dark in light, near-white in dark). Was neutral_m4 (a surface
            // role), i.e. invisible once the screen paints its own neutral_m4 background.
            color = LocalOSColors.current.neutral_p3,
            modifier = Modifier.align(Alignment.CenterVertically),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Icon(
            painter = painterResource(Res.drawable.ic_info_circle),
            contentDescription = null,
            modifier =
                Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = osClickIndication(bounded = false),
                        onClick = {
                            showInfoSheet.value = !showInfoSheet.value
                        },
                    ),
        )
    }
}
