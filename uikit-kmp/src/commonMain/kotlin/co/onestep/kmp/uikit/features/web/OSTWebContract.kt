package co.onestep.kmp.uikit.features.web

import co.onestep.designsystem.theme.OSColors
import co.onestep.designsystem.theme.toStringNoAlpha
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * The JavaScript contract between a native host and a OneStep web mini-app (web summary,
 * questionnaire, terms of service).
 *
 * The *serialized shape* here is a wire contract with the web app — property names become JSON keys
 * read by the page (`window.OneStep.colorThemeConfig`, `window.OneStep.safeAreaInsets`). Renaming a
 * property silently breaks theming/layout on the web side with no compile error, so treat these
 * classes as frozen and add `@SerialName` rather than renaming.
 *
 * Ported from the Patient app's and Clinician app's `MultiPlatformWebView`, which each carried their
 * own copy; this is the union of both payloads (see [injectedHostContextJs]).
 */
@Serializable
enum class OSTWebColorTheme {
    @SerialName("light")
    Light,

    @SerialName("dark")
    Dark,
}

/** Brand colors handed to the page as CSS-usable `#rrggbb` strings. */
@Serializable
data class OSTWebColorPalette(
    val primary: String,
    val secondary: String,
    val error: String,
)

/** Full color payload assigned to `window.OneStep.colorThemeConfig`. */
@Serializable
data class OSTWebColorConfig(
    val colors: OSTWebColorPalette,
    val theme: OSTWebColorTheme,
)

/**
 * Safe-area insets in **CSS px**, which for a web view means density-independent pixels — so these
 * are Compose `dp` values, not raw device pixels.
 *
 * The Clinician app passed raw Android px here while iOS passed dp, which over-inset the page on
 * high-density Android devices; this port uses dp on both platforms.
 */
@Serializable
data class OSTWebSafeAreaInsets(
    val top: Float,
    val bottom: Float,
    val left: Float = 0f,
    val right: Float = 0f,
)

private val contractJson = Json { encodeDefaults = true }

/**
 * Design-system colors → web palette. `secondary` maps to `neutral_p3` (the Patient app's choice);
 * the Clinician app used `primary_0`. Hosts that need the other mapping pass an explicit
 * [OSTWebColorConfig] to [OSTWebView] rather than uikit forking the default.
 */
fun OSColors.toOSTWebColorConfig(isDarkTheme: Boolean = false): OSTWebColorConfig =
    OSTWebColorConfig(
        colors = OSTWebColorPalette(
            primary = primary_p3_main.toStringNoAlpha(),
            secondary = neutral_p3.toStringNoAlpha(),
            error = error_p2.toStringNoAlpha(),
        ),
        theme = if (isDarkTheme) OSTWebColorTheme.Dark else OSTWebColorTheme.Light,
    )

/**
 * Theme + font-scale payload, assigned with `??` so a value the page set for itself before our
 * script ran always wins (the Patient app's semantics — the Clinician app overwrote unconditionally).
 */
internal fun injectedColorThemeJs(config: OSTWebColorConfig, fontScale: Float): String = """
    window.OneStep = window.OneStep ?? {};
    window.OneStep.colorThemeConfig = window.OneStep.colorThemeConfig ?? ${
    contractJson.encodeToString(OSTWebColorConfig.serializer(), config)
};
    window.OneStep.fontScale = window.OneStep.fontScale ?? $fontScale;
""".trimIndent()

/**
 * Safe-area payload. Unlike the theme, this is assigned unconditionally — it changes per device and
 * on rotation, so a stale page-side value must not win.
 *
 * Emits all three forms the two web apps read: the `window.OneStep.safeAreaInsets` object and the
 * Patient app's `--safe-*` CSS variables plus the Clinician app's `--safe-area-inset-*` ones. Both
 * name sets are cheap to set and dropping either would blank the insets for one of the pages.
 */
internal fun injectedSafeAreaJs(insets: OSTWebSafeAreaInsets): String = """
    window.OneStep = window.OneStep ?? {};
    window.OneStep.safeAreaInsets = ${
    contractJson.encodeToString(OSTWebSafeAreaInsets.serializer(), insets)
};
    (function () {
        var r = document.documentElement.style;
        r.setProperty('--safe-top', '${insets.top}px');
        r.setProperty('--safe-bottom', '${insets.bottom}px');
        r.setProperty('--safe-left', '${insets.left}px');
        r.setProperty('--safe-right', '${insets.right}px');
        r.setProperty('--safe-area-inset-top', '${insets.top}px');
        r.setProperty('--safe-area-inset-bottom', '${insets.bottom}px');
        r.setProperty('--safe-area-inset-left', '${insets.left}px');
        r.setProperty('--safe-area-inset-right', '${insets.right}px');
        window.dispatchEvent(new Event('safeareachange'));
    })();
""".trimIndent()

/** Everything uikit injects at document start, before the page's own scripts run. */
internal fun injectedHostContextJs(
    config: OSTWebColorConfig,
    fontScale: Float,
    insets: OSTWebSafeAreaInsets,
): String = """
    (function () {
        ${injectedColorThemeJs(config, fontScale)}
        ${injectedSafeAreaJs(insets)}
    })();
""".trimIndent()

/**
 * The `window.Android.closeForm()` polyfill the OneStep web forms call to dismiss themselves.
 *
 * The name is Android's by history: the web app was written against the Android
 * `@JavascriptInterface` bridge, so iOS supplies a shim over a `WKScriptMessageHandler` rather than
 * the web app branching per platform. Injected only when a host supplies an `onCloseForm` callback.
 */
internal const val CLOSE_FORM_MESSAGE_NAME = "closeForm"

internal val closeFormPolyfillJs = """
    window.Android = window.Android ?? {};
    window.Android.closeForm = function () {
        webkit.messageHandlers.$CLOSE_FORM_MESSAGE_NAME.postMessage(null);
    };
""".trimIndent()

/**
 * Conservative "the page loaded but rendered nothing" probe (OS-16070).
 *
 * The web summary is an SPA: the shell can finish loading while its data fetch comes back empty on
 * a cold backend pipeline, so React renders an empty body — a blank page that fixes itself on
 * reload. This probe drives that bounded auto-reload.
 *
 * Uses `innerText`, not `textContent` (OS-16501): `textContent` counts text inside `<style>` and
 * `<script>`, so a page stuck on a skeleton loader with an inline stylesheet reads as thousands of
 * characters of "content" and never trips. `innerText` is rendered text only, which is what "did
 * anything paint?" has to mean. Any JS failure returns `ok` so uncertainty never causes a reload.
 */
internal const val BLANK_CONTENT_PROBE_JS = """
(function () {
  try {
    var b = document.body;
    var text = (b && b.innerText) ? b.innerText.trim() : '';
    var content = document.querySelectorAll('img, svg, canvas, video, input, button, select, textarea, a').length;
    return (text.length < 10 && content === 0) ? 'blank' : 'ok';
  } catch (e) {
    return 'ok';
  }
})();
"""

internal const val BLANK_CONTENT_PROBE_RESULT = "blank"

/**
 * Trust-boundary check on a host-supplied URL: uikit only ever loads `https`.
 *
 * uikit is a HIPAA-scoped library whose hosts pass URLs through from server payloads, so "open
 * whatever you're given" is not acceptable. Requiring the `https://` prefix rejects `javascript:`
 * and `data:` (script injection into the authenticated origin), `file:` (local file read), and
 * plaintext `http` in one check.
 *
 * Deliberately *not* a host allowlist: OneStep runs per-environment and white-label summary domains,
 * so a baked-in allowlist would break staging and new clinics without making the scheme check any
 * stronger.
 */
internal fun isLoadableWebUrl(url: String): Boolean {
    val trimmed = url.trim()
    if (!trimmed.startsWith("https://", ignoreCase = true)) return false
    // Reject control characters and spaces, which can be used to smuggle a second URL past
    // naive prefix checks, and require a non-empty host.
    if (trimmed.any { it.isWhitespace() || it.code < 0x20 }) return false
    // Prefix length is fixed at 8 by the check above, so this is case-insensitive by construction
    // (removePrefix would miss mixed case like "HtTps://").
    val host = trimmed.substring("https://".length).takeWhile { it != '/' && it != '?' && it != '#' }
    return host.isNotEmpty()
}
