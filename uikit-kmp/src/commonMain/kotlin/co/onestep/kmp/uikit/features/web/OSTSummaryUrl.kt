package co.onestep.kmp.uikit.features.web

import co.onestep.kmp.uikit.utils.Languages

/**
 * Languages the OneStep web summary mini-app ships translations for. Narrower than uikit's own
 * locale set on purpose — this is the *web app's* contract, and an unsupported code must be dropped
 * so the page falls back to its own default instead of rendering half-translated.
 */
private val WEB_SUMMARY_LOCALES = setOf(Languages.ENGLISH, Languages.HEBREW, Languages.RUSSIAN)

/**
 * Appends the host-context query params the OneStep web summary expects to [url].
 *
 * Consolidates the copies that had drifted apart in the Patient app (`pa_recorder`) and the Clinician
 * app (`ca_carelog`), which is why [origin] has no default: it identifies the calling surface in the
 * web app's analytics and guessing it would silently mislabel traffic.
 *
 * Existing query params are preserved and the fragment is kept; on a key collision the value added
 * here wins. Returns [url] unchanged when blank.
 *
 * @param language the device language code, e.g. from `androidx.compose.ui.text.intl.Locale.current`.
 *   The legacy `iw` code is normalized to `he`; codes outside [WEB_SUMMARY_LOCALES] are dropped.
 * @param unitSystem `"metric"` or `"imperial"`; omitted when null or blank.
 * @param origin the calling surface, e.g. `"pa_recorder"`, `"ca_carelog"`.
 * @param overlayClose whether the native host draws its own close affordance over the page. `true`
 *   tells the web summary to leave room for it and not render its own — keep it in step with
 *   [OSTWebScreen]'s `showCloseButton`.
 */
fun enhanceOSTSummaryUrl(
    url: String,
    origin: String,
    language: String? = null,
    unitSystem: String? = null,
    overlayClose: Boolean = true,
): String {
    if (url.isBlank()) return url

    val locale = language
        ?.let { if (it == Languages.HEBREW_LEGACY) Languages.HEBREW else it }
        ?.takeIf { it in WEB_SUMMARY_LOCALES }

    val additions = buildList {
        add("embedded" to "true")
        add("platform" to "mobile")
        add("overlay_close" to overlayClose.toString())
        add("origin" to origin)
        locale?.let { add("locale" to it) }
        unitSystem?.takeIf { it.isNotBlank() }?.let { add("unitSystem" to it) }
    }

    return withQueryParams(url, additions)
}

/**
 * The device's text-scaling setting, handed to a OneStep mini-app as the `fontScale` query param.
 *
 * **Why the query string and not the `window.OneStep.fontScale` global this library also injects.**
 * The global has been on the wire since this contract existed and the mini-apps deliberately ignore
 * it: `libs/mobile-adapter` reads every value that is static for the session's lifetime — `platform`,
 * `embedded`, `locale`, `fontScale` — from the URL through `getEmbeddedParams()`, and reserves
 * `window.OneStep.*` for values a host can update after mount (today only the safe-area insets, which
 * is why those keep both channels). Reading the global is being removed there, not added to. So the
 * query param is the only channel that actually reaches the page.
 *
 * Applied by [OSTWebView] to every URL it loads, rather than by each host at its own call sites: the
 * value comes from `LocalDensity.current.fontScale`, so only a composable can read it, and the web
 * view is the one composable every web surface in both apps passes through.
 *
 * **Not clamped here.** `mobile-adapter` clamps to `[1, 2]` at its provider whatever the source
 * (`clampFontScale`), so a host-side ceiling would only be a second, quieter limit that disagrees with
 * it. Values below 1 — which iOS produces for the small Dynamic Type categories — are floored to 1 by
 * that same clamp; passing them through keeps this function a faithful report of the device rather
 * than a policy.
 *
 * Sent unconditionally, including at `1`. `mobile-adapter` treats an absent param and `1` identically
 * (`parseFontScale(null)` is `DEFAULT_FONT_SCALE`), so this buys no behaviour — it buys the answer to
 * "did the host send it?" being visible in one committed-URL log line, which is exactly what was not
 * answerable while the two sides were passing on different channels.
 *
 * A non-finite scale is reported as `1` rather than as `NaN`, which `Number.parseFloat` would turn
 * into the page's default anyway but only after putting `fontScale=NaN` in a URL.
 */
internal fun withHostFontScale(url: String, fontScale: Float): String {
    if (url.isBlank()) return url
    val scale = if (fontScale.isFinite() && fontScale > 0f) fontScale else 1f
    return withQueryParams(url, listOf("fontScale" to scale.toString()))
}

/**
 * Merges [additions] into [url]'s query string. Existing params are preserved and the fragment is
 * kept; on a key collision the value in [additions] wins.
 */
private fun withQueryParams(url: String, additions: List<Pair<String, String>>): String {
    val hashIndex = url.indexOf('#')
    val beforeFragment = if (hashIndex >= 0) url.substring(0, hashIndex) else url
    val fragment = if (hashIndex >= 0) url.substring(hashIndex) else ""

    val queryIndex = beforeFragment.indexOf('?')
    val base = if (queryIndex >= 0) beforeFragment.substring(0, queryIndex) else beforeFragment
    val existingQuery = if (queryIndex >= 0) beforeFragment.substring(queryIndex + 1) else ""

    val addedKeys = additions.map { it.first }.toSet()
    val preserved = existingQuery
        .split('&')
        .filter { it.isNotEmpty() }
        .filterNot { it.substringBefore('=') in addedKeys }

    val merged = (preserved + additions.map { (key, value) -> "$key=$value" }).joinToString("&")
    return base + (if (merged.isEmpty()) "" else "?$merged") + fragment
}
