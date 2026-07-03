package co.onestep.kmp.uikit.features.summary.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.common.components.ManuallyReportedPill
import co.onestep.kmp.uikit.features.recordFlow.screensData.AnalysisBannerData
import co.onestep.kmp.uikit.features.recordFlow.screensData.InfoBottomSheetData
import co.onestep.kmp.uikit.features.summary.models.MainParamItem
import co.onestep.kmp.uikit.models.OSTActivityType
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.kmp.uikit.utils.mainParamItem
import co.onestep.kmp.uikit.utils.minimalAnalysisBannerDataPreview
import org.jetbrains.compose.ui.tooling.preview.Preview
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.components.SecondaryButton
import co.onestep.designsystem.components.OSButtonSize
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.designsystem.theme.Variables
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.ic_edit
import co.onestep.kmp.uikit_kmp.generated.resources.ic_info_circle
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun MainParamToolBar(
    modifier: Modifier = Modifier,
    mainParamItem: MainParamItem?,
    progress: Float = 1f,
    isLoading: Boolean = false,
    hallwayLengthText: String? = null,
    hallwayWarningText: String? = null,
    onHallwayEdit: () -> Unit = {},
    onEditSts: (() -> Unit)? = null,
    onLearnMore: ((InfoBottomSheetData) -> Unit)? = null,
) {
    val currentDensity = LocalDensity.current
    val noFontScaleDensity =
        Density(
            density = currentDensity.density,
            fontScale = 1f,
        )

    CompositionLocalProvider(LocalDensity provides noFontScaleDensity) {
        Surface(
            modifier = modifier,
            shadowElevation = 0.dp,
            shape = RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(LocalOSColors.current.neutral_m5),
            ) {
                Box(modifier = Modifier.graphicsLayer { alpha = progress }) {
                    CompositionLocalProvider(LocalDensity provides currentDensity) {
                        ExpandedContent(
                            mainParamItem = mainParamItem,
                            isLoading = isLoading,
                            hallwayLengthText = hallwayLengthText,
                            hallwayWarningText = hallwayWarningText,
                            onHallwayEdit = onHallwayEdit,
                            onEditSts = onEditSts,
                            onLearnMore = onLearnMore,
                        )
                    }
                }

                if (mainParamItem?.showTabs == false) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(LocalOSColors.current.neutral_m2),
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandedContent(
    mainParamItem: MainParamItem?,
    isLoading: Boolean,
    hallwayLengthText: String?,
    hallwayWarningText: String?,
    onHallwayEdit: () -> Unit,
    onEditSts: (() -> Unit)?,
    onLearnMore: ((InfoBottomSheetData) -> Unit)?,
) {
    val isSts = mainParamItem?.activityType == OSTActivityType.STS
    val showEditStsIcon = mainParamItem?.editable == true && onEditSts != null

    Column(
        modifier =
            Modifier
                .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        mainParamItem?.analysisBannerData?.let { bannerData ->
            MinimalAnalysisBanner(
                data = bannerData,
                onLearnMore = onLearnMore,
            )
        }

        Spacer(modifier = Modifier.height(Variables.GapXL))

        // Title (with optional pen icon for STS)
        MainParamCardTitle(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Variables.GapL),
            title = mainParamItem?.title.orEmpty(),
            onEdit = if (showEditStsIcon) onEditSts else null,
        )

        // "Manually reported" pill (STS only, when server flag is set)
        if (isSts && mainParamItem?.selfReport == true) {
            Spacer(modifier = Modifier.height(Variables.GapL))
            ManuallyReportedPill()
        }

        if (!hallwayLengthText.isNullOrEmpty()) {
            HallwayLengthRow(
                modifier = Modifier.padding(top = 4.dp, start = Variables.GapL, end = Variables.GapL),
                text = hallwayLengthText,
                showEdit = hallwayWarningText.isNullOrEmpty(),
                onEdit = onHallwayEdit,
            )
        }

        Crossfade(
            targetState = isLoading,
            label = "expanded circle loading",
        ) { loading ->
            if (loading || mainParamItem == null) {
                Spacer(modifier = Modifier.height(24.dp))
                ShimmerCircle(modifier = Modifier.size(ExpandedCircleSize))
            } else if (mainParamItem.mainParamValue != null) {
                LargeMainParamCircle(
                    modifier = Modifier.padding(top = 24.dp),
                    mainParamItem = mainParamItem,
                )
            }
        }

        if (mainParamItem?.showMetadata == true && mainParamItem.showValues) {
            Spacer(Modifier.height(16.dp))
            MainParamCardValues(
                modifier = Modifier.padding(horizontal = Variables.GapL),
                mainParamItem = mainParamItem,
            )
        }

        Spacer(Modifier.height(16.dp))

        if (!hallwayWarningText.isNullOrEmpty()) {
            HallwayWarningRow(
                modifier = Modifier.padding(
                    top = 8.dp,
                    start = Variables.GapL,
                    end = Variables.GapL,
                    bottom = Variables.GapL,
                ),
                text = hallwayWarningText,
            )
        }
    }
}

@Composable
private fun HallwayLengthRow(
    modifier: Modifier = Modifier,
    text: String,
    showEdit: Boolean,
    onEdit: () -> Unit,
) {
    Row(
        modifier = modifier.padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OSText(
            text = text,
            fontSize = 16.sp,
            color = LocalOSColors.current.neutral_p2,
        )

        if (showEdit) {
            Icon(
                modifier =
                    Modifier
                        .padding(start = 6.dp)
                        .size(17.dp)
                        .clickable { onEdit() },
                painter = painterResource(Res.drawable.ic_edit),
                contentDescription = null,
                tint = LocalOSColors.current.primary_0,
            )
        }
    }
}

@Composable
private fun HallwayWarningRow(
    modifier: Modifier = Modifier,
    text: String,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(LocalOSColors.current.primary_m3)
                .padding(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            modifier =
                Modifier
                    .padding(end = 8.dp, top = 2.dp)
                    .size(16.dp),
            painter = painterResource(Res.drawable.ic_info_circle),
            contentDescription = null,
            tint = LocalOSColors.current.neutral_p2,
        )
        OSText(
            text = text,
            fontSize = 16.sp,
            color = LocalOSColors.current.neutral_p2,
        )
    }
}

@Composable
fun ElevatedDivider(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(1.dp)
                .shadow(
                    elevation = 4.dp,
                    clip = false,
                )
                .background(
                    LocalOSColors.current.neutral_p1,
                    shape =
                        RoundedCornerShape(
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp,
                        ),
                ),
    )
}

@Composable
private fun LargeMainParamCircle(modifier: Modifier = Modifier, mainParamItem: MainParamItem) {
    MainParamCircle(
        modifier = modifier,
        circleOffsetY = 0.dp,
        scoreOffsetY = scoreExpandedYOffset,
        circleOffsetX = 0.dp,
        circleSize = ExpandedCircleSize,
        circleStrokeWidth = 7.dp,
        position = 1f,
        scoreFontSize = 42.sp,
        animateMainParam = mainParamItem.animateMainParam,
        mainParam = mainParamItem.mainParamValue,
        mainParamText = mainParamItem.mainParamText,
        mainParamColor = mainParamItem.mainParamColor,
    )
}

@Composable
internal fun MinimalAnalysisBanner(
    modifier: Modifier = Modifier,
    data: AnalysisBannerData,
    onLearnMore: ((InfoBottomSheetData) -> Unit)?,
) {
    val colors = LocalOSColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.warning_m2)
            .padding(Variables.GapL),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            data.title?.let { textData ->
                OSText(
                    text = textData.text,
                    fontSize = textData.textSize,
                    fontWeight = textData.fontWeight,
                    color = textData.color ?: colors.neutral_p3,
                )
            }

            OSText(
                text = data.subtitle.text,
                fontSize = data.subtitle.textSize,
                fontWeight = data.subtitle.fontWeight,
                color = data.subtitle.color ?: colors.neutral_p3,
            )
        }

        data.button?.let { button ->
            SecondaryButton(
                text = button.text.text,
                onClick = {
                    data.infoBottomSheetData?.let {
                        onLearnMore?.invoke(it)
                    }
                },
                modifier = Modifier
                    .height(40.dp)
                    .padding(start = 8.dp),
                size = OSButtonSize.Small,
            )
        }
    }
}

private val ExpandedCircleSize = 115.dp
private val scoreExpandedYOffset = 0.dp

@Preview
@Composable
private fun MainParamToolBarPreview() {
    PreviewTheme {
        MainParamToolBar(
            mainParamItem = mainParamItem,
        )
    }
}
