package co.onestep.kmp.uikit.features.web

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.kmp.uikit.ui.theme.osClickIndication
import co.onestep.kmp.uikit.testing.OSTTestTags
import co.onestep.kmp.uikit.utils.test
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.ic_close
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview


/**
 * Full-screen host for a OneStep web mini-app — the one-call way to show a web summary.
 *
 * Chrome matches what the Patient app's `WebMiniAppScreen` and the Clinician app's `WebViewScreen`
 * both settled on: no native title bar (the page renders its own header, so one would duplicate it),
 * just an overlaid close button in the top-right inside the safe area. Back closes the screen once
 * the page has no history left to pop.
 *
 * For a summary URL, run it through [enhanceOSTSummaryUrl] first so the page gets its host context.
 *
 * ```
 * OSTWebScreen(
 *     url = enhanceOSTSummaryUrl(measurement.summaryUrl, language = "en", origin = "pa_recorder"),
 *     onClose = { navigateBack() },
 * )
 * ```
 *
 * @param showCloseButton set `false` when the host draws its own chrome (a sheet with a grabber, a
 *   toolbar). Pair it with `overlayClose = false` in [enhanceOSTSummaryUrl] so the page knows to
 *   render its own close affordance.
 * @param consumeSafeArea whether this screen insets itself. `false` lets the page run truly
 *   edge-to-edge; it still receives the insets as CSS variables and can lay itself out around them.
 */
@Composable
fun OSTWebScreen(
    url: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    showCloseButton: Boolean = true,
    consumeSafeArea: Boolean = true,
    urlRouter: OSTWebUrlRouter? = null,
    onCloseForm: (() -> Unit)? = null,
    onPageCommitted: ((url: String) -> Unit)? = null,
    injectedJavaScript: String? = null,
    userAgentSuffix: String? = null,
    theme: OSTWebColorConfig = LocalOSColors.current.toOSTWebColorConfig(),
    autoReloadOnBlankContent: Boolean = true,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .test(OSTTestTags.Web.SCREEN)
            .then(
                if (consumeSafeArea) Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
                else Modifier,
            ),
    ) {
        OSTWebView(
            url = url,
            modifier = Modifier.fillMaxSize(),
            urlRouter = urlRouter,
            // One back handler for the whole screen: the web view pops page history while it has
            // any, then closes. The Patient app needed a second, outer handler to override the web
            // view's silent back-swallowing; routing both through onNavigateBack removes that.
            onNavigateBack = onClose,
            onCloseForm = onCloseForm,
            onPageCommitted = onPageCommitted,
            injectedJavaScript = injectedJavaScript,
            userAgentSuffix = userAgentSuffix,
            theme = theme,
            autoReloadOnBlankContent = autoReloadOnBlankContent,
        )

        if (showCloseButton) {
            OSTWebCloseButton(
                onClose = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    // When the outer Box does not consume safeDrawing, the button still has to clear
                    // the status bar on its own.
                    .then(
                        if (consumeSafeArea) Modifier
                        else Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
                    )
                    .padding(12.dp),
            )
        }
    }
}

@Composable
private fun OSTWebCloseButton(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalOSColors.current
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = osClickIndication(),
                onClick = onClose,
            )
            .test(OSTTestTags.Web.CLOSE_BUTTON),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_close),
            contentDescription = null,
            tint = colors.neutral_p3,
        )
    }
}

@Preview
@Composable
private fun OSTWebScreen_Preview() {
    PreviewTheme {
        // The engine cannot render in a preview; this exercises the chrome and the loading state.
        OSTWebScreen(url = "https://app.onestep.co/summary", onClose = {})
    }
}

@Preview
@Composable
private fun OSTWebViewError_Preview() {
    PreviewTheme {
        OSTWebViewError(onRetry = {})
    }
}

@Preview
@Composable
private fun OSTWebViewLoader_Preview() {
    PreviewTheme {
        OSTWebViewLoader()
    }
}
