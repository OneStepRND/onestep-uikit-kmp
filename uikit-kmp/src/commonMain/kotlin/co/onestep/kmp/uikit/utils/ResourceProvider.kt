package co.onestep.kmp.uikit.utils

import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource

/**
 * Platform-abstracted string resource provider for use in ViewModels and non-@Composable code.
 *
 * - Android actual: delegates to Context.getString()
 * - iOS actual: delegates to CMP resource resolution
 */
expect class ResourceProvider {
    fun getString(resource: StringResource): String
    fun getString(resource: StringResource, vararg formatArgs: Any): String
    fun getQuantityString(resource: PluralStringResource, quantity: Int): String
    fun getLocaleLanguageTag(): String
}
