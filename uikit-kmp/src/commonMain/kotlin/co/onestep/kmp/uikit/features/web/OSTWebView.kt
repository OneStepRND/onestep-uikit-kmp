package co.onestep.kmp.uikit.features.web

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.onestep.designsystem.components.OSButtonSize
import co.onestep.designsystem.components.OSCircularLoadingBar
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.components.PrimaryButton
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.testing.OSTTestTags
import co.onestep.kmp.uikit.utils.test
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.there_was_a_problem_connecting_to_the_server_please_try_again_later
import co.onestep.kmp.uikit_kmp.generated.resources.try_again
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource


/**
 * Intercepts a navigation before the web engine performs it. Return `true` to cancel the navigation
 * because the host handled it (a `onestep://...` deeplink, a "close me" URL); `false` to let the
 * page navigate.
 */
@Stable
fun interface OSTWebUrlRouter {
    fun route(url: String): Boolean
}

/**
 * [OSTWebUrlRouter] for the common case of exact-URL matching. Implement [OSTWebUrlRouter] directly
 * when the decision needs query parsing.
 */
fun ostWebUrlRouter(routes: Map<String, () -> Unit>): OSTWebUrlRouter =
    OSTWebUrlRouter { url ->
        routes[url]?.let {
            it()
            true
        } ?: false
    }

/** Handle to the live web engine, so common code can drive platform-specific operations. */
internal interface OSTWebEngine {
    fun reload()

    /** Runs [BLANK_CONTENT_PROBE_JS] in the page; `false` on any evaluation failure. */
    suspend fun isRenderedContentBlank(): Boolean
}

// OS-16070 bounded self-heal: how long to let the SPA hydrate before probing, and how many
// automatic reloads to spend before falling through to the manual-retry error UI.
private const val CONTENT_SETTLE_DELAY_MS = 1_500L
private const val MAX_AUTO_RELOADS = 2
private const val RETRY_LOADER_DELAY_MS = 100L

/**
 * Embedded web view for OneStep web mini-apps (web summary, questionnaires, terms of service),
 * backed by `WebView` on Android and `WKWebView` on iOS.
 *
 * **Authentication.** The page authenticates with the `auth_token` cookie on `.onestep.co` that the
 * host (or the native SDK) plants; uikit never handles the token itself. The two platforms need
 * different work for that cookie to reach the page, and both are handled in the platform layers:
 * Android's `CookieManager` is process-global and already shared with the SDK's HTTP stack, while on
 * iOS the SDK's `NSHTTPCookieStorage.shared` and the web view's `WKWebsiteDataStore` are separate
 * stores, so the applicable cookies are copied across before the first request. See the `actual`
 * implementations for the details and the constraints on clearing them.
 *
 * This is the union of the Patient app's and Clinician app's `MultiPlatformWebView`. The load/error/
 * retry state machine lives here in common code; only the engine itself is per-platform.
 *
 * Full-login browser handoff (Chrome Custom Tabs / `ASWebAuthenticationSession`) is deliberately
 * **not** ported: logging in is a host concern, and it would force `androidx.browser` onto every
 * consumer of this library.
 *
 * @param url the page to load. Must be `https` — anything else renders the error state rather than
 *   being handed to the engine (see [isLoadableWebUrl]).
 * @param urlRouter lets the host intercept navigations (deeplinks). Checked before the engine's own
 *   scheme handling.
 * @param onNavigateBack invoked on a back gesture when the page has no history left to pop.
 * @param onCloseForm wires the `window.Android.closeForm()` bridge the OneStep web forms call.
 *   Leaving it `null` installs no JS bridge at all — only pass it for pages that need it.
 * @param onPageCommitted reports the committed URL of each main-frame load.
 * @param injectedJavaScript extra script run after uikit's own host-context injection. This is the
 *   hook for host-specific concerns uikit has no business knowing about (the Patient app's Datadog
 *   RUM snippet, for instance).
 * @param userAgentSuffix appended to the platform default user agent, e.g. `"PatientApp/1.0"`.
 * @param theme brand colors and light/dark handed to the page.
 * @param autoReloadOnBlankContent bounded self-heal for an SPA that loads but renders nothing
 *   (OS-16070). Leave on for the web summary; turn off for pages that can legitimately be blank.
 * @param loader shown while a main-frame load is in flight.
 * @param errorContent shown when the load fails or the URL is not loadable; receives the retry action.
 */
@Composable
fun OSTWebView(
    url: String,
    modifier: Modifier = Modifier,
    urlRouter: OSTWebUrlRouter? = null,
    onNavigateBack: (() -> Unit)? = null,
    onCloseForm: (() -> Unit)? = null,
    onPageCommitted: ((url: String) -> Unit)? = null,
    injectedJavaScript: String? = null,
    userAgentSuffix: String? = null,
    theme: OSTWebColorConfig = LocalOSColors.current.toOSTWebColorConfig(),
    autoReloadOnBlankContent: Boolean = true,
    loader: @Composable () -> Unit = { OSTWebViewLoader() },
    errorContent: @Composable (retry: () -> Unit) -> Unit = { OSTWebViewError(onRetry = it) },
) {
    val isUrlLoadable = remember(url) { isLoadableWebUrl(url) }

    val isLoading = remember { mutableStateOf(isUrlLoadable) }
    val isError = remember { mutableStateOf(!isUrlLoadable) }
    val engine = remember { mutableStateOf<OSTWebEngine?>(null) }
    // Bumped by the platform layer on every finished main-frame load; drives the blank-content probe.
    val loadFinishedToken = remember { mutableStateOf(0) }
    var reloadSignal by remember { mutableIntStateOf(0) }
    var retryRequested by remember { mutableStateOf(false) }
    var autoReloadCount by remember { mutableIntStateOf(0) }

    Box(modifier = modifier.fillMaxSize().test(OSTTestTags.Web.VIEW)) {
        // A non-https URL is never handed to the engine — the error state below is the whole
        // behavior. Hosts pass URLs straight through from server payloads, so this is a real case,
        // not a defensive nicety.
        if (isUrlLoadable) {
            PlatformWebView(
                url = url,
                urlRouter = urlRouter,
                theme = theme,
                onCloseForm = onCloseForm,
                onNavigateBack = onNavigateBack,
                onPageCommitted = onPageCommitted,
                injectedJavaScript = injectedJavaScript,
                userAgentSuffix = userAgentSuffix,
                isLoading = isLoading,
                isError = isError,
                loadFinishedToken = loadFinishedToken,
                engine = engine,
                reloadSignal = reloadSignal,
            )
        }

        // Each distinct URL gets a fresh self-heal budget.
        LaunchedEffect(url) { autoReloadCount = 0 }

        LaunchedEffect(retryRequested) {
            if (!retryRequested) return@LaunchedEffect
            isError.value = false
            isLoading.value = true
            // Hold the loader briefly so a retry that fails instantly still reads as an attempt
            // rather than an unchanged error screen.
            delay(RETRY_LOADER_DELAY_MS)
            reloadSignal++
            retryRequested = false
        }

        // OS-16070: after a main-frame load finishes, let the SPA hydrate, then probe for a
        // rendered-but-blank page. Reload while under budget; once spent, surface the error UI for a
        // manual retry. Cannot loop: the budget is bounded and SPA route changes (pushState) do not
        // bump loadFinishedToken.
        LaunchedEffect(loadFinishedToken.value, autoReloadOnBlankContent) {
            if (!autoReloadOnBlankContent || loadFinishedToken.value == 0) return@LaunchedEffect
            val currentEngine = engine.value ?: return@LaunchedEffect

            delay(CONTENT_SETTLE_DELAY_MS)
            if (!currentEngine.isRenderedContentBlank()) return@LaunchedEffect

            if (autoReloadCount < MAX_AUTO_RELOADS) {
                autoReloadCount++
                isError.value = false
                // Keep the loader over the blank page for the duration of the reload.
                isLoading.value = true
                currentEngine.reload()
            } else {
                isLoading.value = false
                isError.value = true
            }
        }

        AnimatedVisibility(visible = isLoading.value, enter = fadeIn(), exit = fadeOut()) {
            loader()
        }

        AnimatedVisibility(visible = isError.value, enter = fadeIn(), exit = fadeOut()) {
            errorContent {
                // A user-initiated retry grants a fresh self-heal budget so the probe can run again
                // on the reloaded page.
                autoReloadCount = 0
                retryRequested = true
            }
        }
    }
}

/**
 * The web engine itself. Reports load state through [isLoading] / [isError], publishes an
 * [OSTWebEngine] handle into [engine], bumps [loadFinishedToken] on each finished main-frame load,
 * and reloads whenever [reloadSignal] increments past 0.
 */
@Composable
internal expect fun PlatformWebView(
    url: String,
    urlRouter: OSTWebUrlRouter?,
    theme: OSTWebColorConfig,
    onCloseForm: (() -> Unit)?,
    onNavigateBack: (() -> Unit)?,
    onPageCommitted: ((url: String) -> Unit)?,
    injectedJavaScript: String?,
    userAgentSuffix: String?,
    isLoading: MutableState<Boolean>,
    isError: MutableState<Boolean>,
    loadFinishedToken: MutableState<Int>,
    engine: MutableState<OSTWebEngine?>,
    reloadSignal: Int,
)

/** Default loading state: the design-system spinner centered on the screen backdrop. */
@Composable
fun OSTWebViewLoader(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LocalOSColors.current.neutral_m5)
            .test(OSTTestTags.Web.LOADER),
        contentAlignment = Alignment.Center,
    ) {
        // Keeps the design-system stroke-to-size ratio (3dp at 48dp).
        OSCircularLoadingBar(size = 64.dp, strokeWidth = 8.dp)
    }
}

/**
 * Default error state: connection message plus a retry button. Deliberately generic — it never
 * surfaces the URL or an HTTP body, both of which can carry patient identifiers (HIPAA).
 */
@Composable
fun OSTWebViewError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalOSColors.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.neutral_m5)
            .padding(24.dp)
            .test(OSTTestTags.Web.ERROR),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            OSText(
                text = stringResource(
                    Res.string.there_was_a_problem_connecting_to_the_server_please_try_again_later,
                ),
                color = colors.neutral_p3,
                textAlign = TextAlign.Center,
            )
            PrimaryButton(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth()
                    .test(OSTTestTags.Web.RETRY_BUTTON),
                text = stringResource(Res.string.try_again),
                onClick = onRetry,
                size = OSButtonSize.Big,
            )
        }
    }
}
