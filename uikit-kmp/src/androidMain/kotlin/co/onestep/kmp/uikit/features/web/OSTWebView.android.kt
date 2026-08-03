package co.onestep.kmp.uikit.features.web

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.viewinterop.AndroidView
import co.onestep.kmp.uikit.utils.PlatformBackHandler
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * `WebView`-backed engine.
 *
 * **Cookies / authentication.** Android's [CookieManager] is a *process-global* store shared by
 * every `WebView` and by the OneStep SDK's HTTP stack, so the `auth_token` cookie the host plants on
 * `.onestep.co` is already visible here — no copying is needed, unlike iOS. Two consequences:
 *
 *  - `setAcceptCookie(true)` and `setAcceptThirdPartyCookies` must be on, or the shared jar is
 *    ignored and the page loads unauthenticated (rendering blank).
 *  - uikit must **never** clear this jar (`removeAllCookies`, or a private/incognito store) on
 *    dispose. It is not "our" cookie store — wiping it logs the SDK out mid-session. Cookie
 *    lifetime is the host's to manage, at logout.
 */
@SuppressLint("SetJavaScriptEnabled")
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
    val context = LocalContext.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val fontScale = density.fontScale

    // dp, not raw px: these become CSS px on the page. The Clinician app passed raw px here, which
    // over-inset the page by the display density factor.
    val insets = WindowInsets.safeDrawing
    val safeAreaInsets = with(density) {
        OSTWebSafeAreaInsets(
            top = insets.getTop(this).toDp().value,
            bottom = insets.getBottom(this).toDp().value,
            left = insets.getLeft(this, layoutDirection).toDp().value,
            right = insets.getRight(this, layoutDirection).toDp().value,
        )
    }

    // The nav client is remembered once, so it reads the latest callbacks through holders rather
    // than capturing the first composition's values.
    // Insets in particular change after the first composition (rotation, keyboard) — the ported code
    // captured them once and the page never heard about it again.
    val insetsHolder = remember { mutableStateOf(safeAreaInsets) }
    insetsHolder.value = safeAreaInsets
    val routerHolder = remember { mutableStateOf(urlRouter) }
    routerHolder.value = urlRouter
    val pageCommittedHolder = remember { mutableStateOf(onPageCommitted) }
    pageCommittedHolder.value = onPageCommitted

    val webViewClient = remember {
        object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                isLoading.value = true
                // A new load starting is the only safe place to clear the error: onPageFinished also
                // fires for WebView's error page, so clearing there would swallow the failure that
                // onReceivedError just reported.
                isError.value = false
                view?.evaluateJavascript(
                    injectedHostContextJs(theme, fontScale, insetsHolder.value),
                    null,
                )
                injectedJavaScript?.let { view?.evaluateJavascript(it, null) }
            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {
                super.onPageCommitVisible(view, url)
                url?.let { pageCommittedHolder.value?.invoke(it) }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Hide the loader only once the document is done. onPageCommitVisible fires at the
                // first painted frame, which for an SPA is still the blank shell.
                isLoading.value = false
                loadFinishedToken.value += 1
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?,
            ): Boolean {
                val target = request?.url ?: return super.shouldOverrideUrlLoading(view, request)
                val scheme = target.scheme ?: return super.shouldOverrideUrlLoading(view, request)

                // The host router runs FIRST so it sees deeplink schemes (onestep-prod://open/otp)
                // before the Intent system below swallows them.
                if (routerHolder.value?.route(target.toString()) == true) return true

                // Hand non-http(s) schemes (mailto:, tel:, intent:) to the Android Intent system.
                if (!scheme.equals("http", ignoreCase = true) &&
                    !scheme.equals("https", ignoreCase = true)
                ) {
                    return try {
                        val intent = if (scheme.equals("intent", ignoreCase = true)) {
                            Intent.parseUri(target.toString(), Intent.URI_INTENT_SCHEME)
                        } else {
                            Intent(Intent.ACTION_VIEW, target)
                        }
                        view?.context?.startActivity(intent)
                        true
                    } catch (e: Exception) {
                        // No installed app handles the scheme — swallow rather than crash, and do
                        // not let the WebView try to render it either.
                        true
                    }
                }

                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?,
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                if (request?.isForMainFrame != true) return
                // 404 only, matching both apps: the web summary returns non-fatal error statuses on
                // sub-resources and even on some main-frame probes that still render fine.
                if (errorResponse?.statusCode == 404) {
                    isError.value = true
                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?,
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    isError.value = true
                }
            }
        }
    }

    val webView = remember {
        WebView(context).apply {
            // See the KDoc: enable the shared jar, never clear it.
            CookieManager.getInstance().let { cookies ->
                cookies.setAcceptCookie(true)
                cookies.setAcceptThirdPartyCookies(this, true)
            }

            scrollBarStyle = View.SCROLLBARS_OUTSIDE_OVERLAY
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            settings.apply {
                javaScriptEnabled = true
                useWideViewPort = true
                domStorageEnabled = true
                textZoom = 100
                userAgentString = listOfNotNull(
                    WebSettings.getDefaultUserAgent(context),
                    userAgentSuffix,
                ).joinToString(" ")
            }
            this.webViewClient = webViewClient

            // Only installed when the host asks for it: a @JavascriptInterface is reachable by any
            // script the page runs, so it stays absent for pages that do not need the bridge.
            if (onCloseForm != null) {
                val mainHandler = Handler(Looper.getMainLooper())
                addJavascriptInterface(
                    object {
                        @JavascriptInterface
                        fun closeForm() {
                            mainHandler.post { onCloseForm() }
                        }
                    },
                    "Android",
                )
            }
        }
    }

    DisposableEffect(webView) {
        engine.value = object : OSTWebEngine {
            override fun reload() = webView.reload()

            override suspend fun isRenderedContentBlank(): Boolean =
                suspendCancellableCoroutine { continuation ->
                    webView.evaluateJavascript(BLANK_CONTENT_PROBE_JS) { result ->
                        // evaluateJavascript returns a JSON literal, so the string comes back quoted.
                        continuation.resume(result?.trim('"') == BLANK_CONTENT_PROBE_RESULT)
                    }
                }
        }
        onDispose {
            engine.value = null
            // Stop in-flight work but do NOT touch the shared cookie jar (see the KDoc). The
            // WebView itself is destroyed in AndroidView's onRelease, which is the point at which
            // the view is guaranteed to be detached.
            webView.stopLoading()
        }
    }

    LaunchedEffect(reloadSignal) {
        if (reloadSignal > 0) webView.reload()
    }

    // Push inset changes to the live page instead of reloading it. The page re-reads them from the
    // `safeareachange` event this script dispatches.
    LaunchedEffect(safeAreaInsets) {
        webView.evaluateJavascript(injectedSafeAreaJs(safeAreaInsets), null)
    }

    PlatformBackHandler {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            onNavigateBack?.invoke()
        }
    }

    // Tracks what we asked the engine to load, so recomposition never restarts the page. Comparing
    // against WebView.url instead would reload after any server redirect.
    val requestedUrl = remember { mutableStateOf<String?>(null) }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { webView },
        update = { view ->
            if (requestedUrl.value != url) {
                requestedUrl.value = url
                view.loadUrl(url)
            }
        },
        onRelease = { view ->
            if (onCloseForm != null) view.removeJavascriptInterface("Android")
            view.destroy()
        },
    )
}
