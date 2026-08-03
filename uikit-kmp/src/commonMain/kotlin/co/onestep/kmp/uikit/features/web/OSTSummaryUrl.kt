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
