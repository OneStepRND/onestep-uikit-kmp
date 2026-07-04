package co.onestep.kmp.uikit.features.summary.screens.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.common.components.WheelPickerScreen
import co.onestep.kmp.uikit.di.UIKitServiceLocator
import co.onestep.kmp.uikit.features.recordFlow.RecordFlowDataFactory
import co.onestep.kmp.uikit.features.recordFlow.RecordFlowError
import co.onestep.kmp.uikit.features.recordFlow.components.ToolBarHeight
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.UiKitScreen
import co.onestep.kmp.uikit.features.summary.presentation.StsFailureType
import co.onestep.kmp.uikit.features.summary.presentation.StsManualReportViewModel
import co.onestep.kmp.uikit.utils.UIktDestination
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.sts_manual_report_title
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource

/**
 * Route for the STS manual self-report flow.
 *
 * @param uuid Motion measurement UUID to submit the manual reading against.
 * @param initialValue Optional pre-selected repetition count (typically the current/known
 * main-param value, so the wheel opens centred on it).
 */
@Serializable
internal data class StsManualReportDestination(
    val uuid: String,
    val initialValue: Int? = null,
) : UIktDestination

/**
 * Navigation builder for the [StsManualReportDestination] composable destination. Ported from
 * the Android `uikit` `stsManualReportScreen`.
 *
 * The destination cross-fades between three pieces of content:
 *  - the [WheelPickerScreen] for picking the repetition count,
 *  - a connectivity-error screen with a Reload button when the submission failed due to a
 *    [StsFailureType.Network] error, and
 *  - a server-error screen **without** any action button when the server responded with a
 *    non-2xx status ([StsFailureType.Server]).
 *
 * @param onSubmitted Invoked after a successful submission. The host should pop back to the
 * summary and trigger a refresh.
 * @param onClose Invoked when the user dismisses the wheel screen normally (back/close).
 * @param onExitOnFailure Invoked when the user dismisses an error screen after a failure.
 * @param applyTopToolBarPadding When `true`, reserves [ToolBarHeight] of top padding so content
 * does not sit under the host's shared toolbar overlay (summary flow). Pass `false` when the
 * host already pushes content down via a Column-based toolbar (recording flow).
 * @param onScreenView Fired once when the manual-report screen becomes visible (analytics).
 * @param onSaveClicked Fired with the entered value when the user taps Save (analytics).
 */
internal fun EntryProviderScope<NavKey>.stsManualReportScreen(
    onSubmitted: (uuid: String) -> Unit,
    onClose: () -> Unit,
    onExitOnFailure: () -> Unit,
    applyTopToolBarPadding: Boolean = true,
    onScreenView: () -> Unit = {},
    onSaveClicked: (value: Int) -> Unit = {},
) {
    entry<StsManualReportDestination> { route ->
        val viewModel: StsManualReportViewModel = remember { StsManualReportViewModel() }
        val state by viewModel.state.collectAsState()
        val colors = LocalOSColors.current
        val resourceProvider = UIKitServiceLocator.resourceProvider
        val title = stringResource(Res.string.sts_manual_report_title)

        LaunchedEffect(Unit) { onScreenView() }

        AnimatedContent(
            targetState = state.failureType,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "StsManualReportContent",
        ) { failureType ->
            when (failureType) {
                null -> WheelPickerScreen(
                    title = title,
                    initialValue = route.initialValue,
                    onSave = { value ->
                        onSaveClicked(value)
                        viewModel.onSubmit(route.uuid, value)
                    },
                    range = 0..40,
                    isSubmitting = state.submitting,
                    applyTopToolBarPadding = applyTopToolBarPadding,
                )

                StsFailureType.Network -> {
                    // Reuse the shared connectivity error screen ("Reload" CTA). While a retry
                    // is in flight, disable the CTA and overlay a spinner.
                    val screenData = RecordFlowDataFactory.errorScreenData(
                        error = RecordFlowError.Connectivity,
                        resourceProvider = resourceProvider,
                        onRetry = { if (!state.submitting) viewModel.retry() },
                    ).let { base ->
                        base.copy(
                            brandButton = base.brandButton?.copy(enabled = !state.submitting),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = if (applyTopToolBarPadding) ToolBarHeight.dp else 0.dp,
                            ),
                    ) {
                        UiKitScreen(
                            onBackPress = {
                                viewModel.consumeFailed()
                                onExitOnFailure()
                            },
                            screenData = screenData,
                        )
                        if (state.submitting) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 34.dp)
                                    .size(24.dp),
                                color = colors.neutral_m3,
                                strokeWidth = 2.dp,
                            )
                        }
                    }
                }

                StsFailureType.Server -> {
                    // Server-side rejection (e.g. 403, 500). Strip the brandButton off the
                    // shared server screen so the user can only dismiss — retrying a 4xx is not
                    // the right affordance.
                    val screenData = RecordFlowDataFactory.errorScreenData(
                        error = RecordFlowError.ServerIssue,
                        resourceProvider = resourceProvider,
                        onRetry = { /* never invoked — button stripped below */ },
                    ).copy(brandButton = null)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = if (applyTopToolBarPadding) ToolBarHeight.dp else 0.dp,
                            ),
                    ) {
                        UiKitScreen(
                            onBackPress = {
                                viewModel.consumeFailed()
                                onExitOnFailure()
                            },
                            screenData = screenData,
                        )
                    }
                }
            }
        }

        LaunchedEffect(state.submitted) {
            if (state.submitted) {
                viewModel.consumeSubmitted()
                onSubmitted(route.uuid)
            }
        }
    }
}
