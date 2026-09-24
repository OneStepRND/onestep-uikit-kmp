package co.onestep.kmp.uikit.features.tagging

import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTTagField
import kotlinx.serialization.json.Json

/**
 * The real backend tag catalog, per `"<activity>.<locale>"` (e.g. `"walk.en_US"`), decoded exactly
 * as a host would decode the endpoint's `fields`. Tests run against it rather than hand-written
 * fixtures so they see real codes and real `requiresAny` rules (see `fixtures/tag_catalog_sample.json`).
 */
internal object TagCatalogFixture {
    private val json = Json { ignoreUnknownKeys = true }

    val catalogs: Map<String, List<OSTTagField>> by lazy {
        json.decodeFromString<Map<String, List<OSTTagField>>>(TAG_CATALOG_SAMPLE_JSON)
    }

    fun fields(activity: String, locale: String = "en_US"): List<OSTTagField> =
        requireNotNull(catalogs["$activity.$locale"]) { "no catalog for $activity.$locale" }
}
