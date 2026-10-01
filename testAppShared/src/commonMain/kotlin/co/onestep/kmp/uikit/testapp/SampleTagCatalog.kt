package co.onestep.kmp.uikit.testapp

import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTTagField
import co.onestep.kmp.uikit.models.OSTActivityType
import kotlinx.serialization.json.Json

/**
 * The bundled backend tag catalog (OS-17546) — every activity in en_US and he_IL, generated with the
 * backend's own resolver and no org overrides — standing in for the host fetch that
 * `GET /api/camel/perception/tag_catalog/` does in a real app. Decoded exactly as a host would.
 */
internal object SampleTagCatalog {
    private val json = Json { ignoreUnknownKeys = true }

    private val catalogs: Map<String, List<OSTTagField>> by lazy {
        json.decodeFromString<Map<String, List<OSTTagField>>>(TAG_CATALOG_SAMPLE_JSON)
    }

    /** The fields served for [activity] in [locale]; empty when the snapshot has none for it. */
    fun fieldsFor(activity: OSTActivityType, locale: String): List<OSTTagField> =
        catalogs["${activity.catalogName()}.$locale"].orEmpty()

    // The catalog is keyed by the backend activity name, which is the enum's wire name.
    private fun OSTActivityType.catalogName(): String =
        json.encodeToString(OSTActivityType.serializer(), this).trim('"').lowercase()
}
