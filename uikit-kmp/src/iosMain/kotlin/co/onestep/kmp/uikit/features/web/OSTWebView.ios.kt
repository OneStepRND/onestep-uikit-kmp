package co.onestep.kmp.uikit.features.web

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import co.onestep.kmp.uikit.utils.PlatformBackHandler
import kotlin.coroutines.resume
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.readValue
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSError
import platform.Foundation.NSHTTPCookie
import platform.Foundation.NSHTTPCookieStorage
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.UIKit.UIScrollViewContentInsetAdjustmentBehavior
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKUserScript
import platform.WebKit.WKUserScriptInjectionTime
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

/**
 * `WKWebView`-backed engine.
 *
 * **Cookies / authentication — the part that differs fundamentally from Android.** iOS has *two*
 * cookie stores that do not share:
 *
 *  - `NSHTTPCookieStorage.sharedHTTPCookieStorage`, used by `URLSession` — this is where the native
 *    OneStep SDK reads auth, and where a clinician host plants the JWT as
 *    `auth_token` on `.onestep.co` (`IosOneStepSdkController.seedSdkAuthCookie`).
 *  - `configuration.websiteDataStore.httpCookieStore`, the only store a `WKWebView` reads.
 *
 * A clinician logs in over OTP and never establishes a web cookie session, so without an explicit
 * copy the summary page loads unauthenticated and renders blank. [loadAuthenticated] bridges the two
 * and — critically — issues the request only after the last `setCookie` completion fires, because
 * `setCookie` is asynchronous and a load started earlier races the cookies it needs.
 *
 * Two things this deliberately does *not* do:
 *
 *  - It does not use `WKWebsiteDataStore.nonPersistent()`. That would look like the privacy-safe
 *    default, but the Patient app logs in *through the web view*, so its session lives in the
 *    persistent default store; an ephemeral store would sign that host out on every launch.
 *  - It does not clear either store on dispose, for the same reason. Cookie lifetime belongs to the
 *    host, at logout.
 */
@OptIn(ExperimentalForeignApi::class)
private fun WKWebView.loadAuthenticated(target: NSURL) {
    val request = NSURLRequest.requestWithURL(target)
    val webCookieStore = configuration.websiteDataStore.httpCookieStore

    // cookiesForURL applies Foundation's own domain / path / Secure matching rules, so only cookies
    // that actually belong to this URL cross over. The Clinician app copied the entire shared jar,
    // which put unrelated domains' cookies into the web store.
    @Suppress("UNCHECKED_CAST")
    val applicable = (
        NSHTTPCookieStorage.sharedHTTPCookieStorage.cookiesForURL(target) as? List<NSHTTPCookie>
        ).orEmpty()

    if (applicable.isEmpty()) {
        // Nothing to bridge. Either the host authenticates in the web view itself (Patient app,
        // whose session already lives in the WK store) or the page is public.
        loadRequest(request)
        return
    }

    var remaining = applicable.size
    applicable.forEach { cookie ->
        webCookieStore.setCookie(cookie) {
            // WebKit invokes these completion handlers on the main thread, so this counter needs no
            // synchronization. Load once the last one lands.
            remaining -= 1
            if (remaining == 0) loadRequest(request)
        }
    }
}

private fun hostContentController(
    theme: OSTWebColorConfig,
    fontScale: Float,
    safeAreaInsets: OSTWebSafeAreaInsets,
    onCloseForm: (() -> Unit)?,
    injectedJavaScript: String?,
): WKUserContentController = WKUserContentController().apply {
    addUserScript(
        WKUserScript(
            source = injectedHostContextJs(theme, fontScale, safeAreaInsets),
            injectionTime = WKUserScriptInjectionTime.WKUserScriptInjectionTimeAtDocumentStart,
            forMainFrameOnly = true,
        ),
    )

    if (onCloseForm != null) {
        addUserScript(
            WKUserScript(
                source = closeFormPolyfillJs,
                injectionTime = WKUserScriptInjectionTime.WKUserScriptInjectionTimeAtDocumentStart,
                forMainFrameOnly = true,
            ),
        )
        addScriptMessageHandler(
            object : NSObject(), WKScriptMessageHandlerProtocol {
                override fun userContentController(
                    userContentController: WKUserContentController,
                    didReceiveScriptMessage: WKScriptMessage,
                ) {
                    onCloseForm()
                }
            },
            name = CLOSE_FORM_MESSAGE_NAME,
        )
    }

    // Host-specific script runs at document end so it can see the page's own globals.
    injectedJavaScript?.let {
        addUserScript(
            WKUserScript(
                source = it,
                injectionTime = WKUserScriptInjectionTime.WKUserScriptInjectionTimeAtDocumentEnd,
                forMainFrameOnly = true,
            ),
        )
    }
}

/** `NSURLErrorCancelled`-adjacent: a frame load interrupted by our own navigation, not a failure. */
private const val WK_ERROR_FRAME_LOAD_INTERRUPTED = 102L

@OptIn(ExperimentalComposeUiApi::class, ExperimentalForeignApi::class)
@Composable
internal actual fun PlatformWebView(
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
) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val fontScale = density.fontScale

    val insets = WindowInsets.safeDrawing
    val safeAreaInsets = with(density) {
        OSTWebSafeAreaInsets(
            top = insets.getTop(this).toDp().value,
            bottom = insets.getBottom(this).toDp().value,
            left = insets.getLeft(this, layoutDirection).toDp().value,
            right = insets.getRight(this, layoutDirection).toDp().value,
        )
    }

    // The delegate is created once, so it reads the latest callbacks through holders rather than
    // capturing the first composition's values.
    val insetsHolder = remember { mutableStateOf(safeAreaInsets) }
    insetsHolder.value = safeAreaInsets
    val routerHolder = remember { mutableStateOf(urlRouter) }
    routerHolder.value = urlRouter
    val pageCommittedHolder = remember { mutableStateOf(onPageCommitted) }
    pageCommittedHolder.value = onPageCommitted

    val navDelegate = remember {
        object : NSObject(), WKNavigationDelegateProtocol {
            override fun webView(
                webView: WKWebView,
                decidePolicyForNavigationAction: WKNavigationAction,
                decisionHandler: (WKNavigationActionPolicy) -> Unit,
            ) {
                val requestUrl = decidePolicyForNavigationAction.request.URL?.absoluteString
                if (requestUrl != null && routerHolder.value?.route(requestUrl) == true) {
                    decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
                    return
                }
                decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
            }

            @ObjCSignatureOverride
            override fun webView(webView: WKWebView, didStartProvisionalNavigation: WKNavigation?) {
                // A full main-frame load began. SPA client-side route changes go through pushState
                // and do not fire this, so the loader won't flash on in-page navigation.
                isLoading.value = true
            }

            @ObjCSignatureOverride
            override fun webView(webView: WKWebView, didCommitNavigation: WKNavigation?) {
                // Do NOT hide the loader here: didCommit fires at the first received bytes, long
                // before an SPA has painted, and hiding now exposes the blank white WKWebView
                // background. Content is arriving, so only clear the error state.
                isError.value = false
                webView.URL?.absoluteString?.let { pageCommittedHolder.value?.invoke(it) }
            }

            @ObjCSignatureOverride
            override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
                // Only the loader is cleared here. WebKit does not call didFinish after a
                // did-fail callback, but leaving the error flag alone keeps this symmetric with
                // Android, where onPageFinished *does* fire for the error page.
                isLoading.value = false
                loadFinishedToken.value += 1
            }

            @ObjCSignatureOverride
            override fun webView(
                webView: WKWebView,
                didFailNavigation: WKNavigation?,
                withError: NSError,
            ) {
                isLoading.value = false
                isError.value = true
            }

            @ObjCSignatureOverride
            override fun webView(
                webView: WKWebView,
                didFailProvisionalNavigation: WKNavigation?,
                withError: NSError,
            ) {
                if (withError.code == WK_ERROR_FRAME_LOAD_INTERRUPTED) return
                isLoading.value = false
                isError.value = true
            }
        }
    }

    // Deliberately NOT keyed on safeAreaInsets: they change on rotation and when the keyboard
    // appears, and recreating the WKWebView there (which the ported code did) discards the loaded
    // page, its scroll position, and its data fetch. Inset changes are pushed into the live page by
    // the LaunchedEffect below instead.
    val webView = remember(theme, fontScale, userAgentSuffix) {
        WKWebView(
            frame = CGRectZero.readValue(),
            configuration = WKWebViewConfiguration().apply {
                userContentController = hostContentController(
                    theme = theme,
                    fontScale = fontScale,
                    safeAreaInsets = insetsHolder.value,
                    onCloseForm = onCloseForm,
                    injectedJavaScript = injectedJavaScript,
                )
                userAgentSuffix?.let { applicationNameForUserAgent = it }
                allowsInlineMediaPlayback = true
                allowsAirPlayForMediaPlayback = true
                allowsPictureInPictureMediaPlayback = true
            },
        ).apply {
            navigationDelegate = navDelegate
            // Compose already applies safe-area padding and the page gets the insets as CSS vars;
            // letting UIKit adjust as well would double-inset the content.
            scrollView.contentInsetAdjustmentBehavior =
                UIScrollViewContentInsetAdjustmentBehavior.UIScrollViewContentInsetAdjustmentNever
        }
    }

    // Resolved once per URL. `NSURL(string:)` returns nil for a malformed URL, and passing that on
    // would trap in requestWithURL — common code has already scheme-checked, but a URL can pass the
    // scheme check and still fail to parse.
    val target = remember(url) { NSURL.URLWithString(url) }

    DisposableEffect(webView) {
        engine.value = object : OSTWebEngine {
            override fun reload() {
                // reload() over a fresh request: it reuses the cached JS bundle and just re-runs the
                // SPA (re-firing the data fetch we need), which is far less main-thread work than
                // re-fetching and re-parsing the page — it keeps the Compose loader from stuttering
                // during the reload.
                webView.reload()
            }

            override suspend fun isRenderedContentBlank(): Boolean =
                suspendCancellableCoroutine { continuation ->
                    webView.evaluateJavaScript(BLANK_CONTENT_PROBE_JS) { result, error ->
                        continuation.resume(
                            error == null && (result as? String) == BLANK_CONTENT_PROBE_RESULT,
                        )
                    }
                }
        }
        onDispose {
            engine.value = null
            webView.navigationDelegate = null
            webView.stopLoading()
            if (onCloseForm != null) {
                webView.configuration.userContentController
                    .removeScriptMessageHandlerForName(CLOSE_FORM_MESSAGE_NAME)
            }
        }
    }

    LaunchedEffect(webView, target) {
        if (target == null) {
            isLoading.value = false
            isError.value = true
            return@LaunchedEffect
        }
        webView.loadAuthenticated(target)
    }

    LaunchedEffect(reloadSignal) {
        if (reloadSignal > 0 && target != null) webView.loadAuthenticated(target)
    }

    // Push inset changes to the live page instead of recreating the view. The page re-reads them
    // from the `safeareachange` event this script dispatches.
    LaunchedEffect(safeAreaInsets) {
        webView.evaluateJavaScript(injectedSafeAreaJs(safeAreaInsets), null)
    }

    PlatformBackHandler {
        if (webView.canGoBack) {
            webView.goBack()
        } else {
            onNavigateBack?.invoke()
        }
    }

    UIKitView(
        modifier = Modifier.fillMaxSize(),
        // Touch-to-scroll does not work on a physical device with the cooperative default.
        // See https://youtrack.jetbrains.com/issue/CMP-6922
        properties = UIKitInteropProperties(
            interactionMode = UIKitInteropInteractionMode.NonCooperative,
        ),
        factory = { webView },
    )
}
