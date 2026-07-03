package co.onestep.kmp.uikit.utils

import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.getPluralString
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

actual class ResourceProvider {
    actual fun getString(resource: StringResource): String =
        runBlocking { org.jetbrains.compose.resources.getString(resource) }

    actual fun getString(resource: StringResource, vararg formatArgs: Any): String =
        runBlocking { org.jetbrains.compose.resources.getString(resource, *formatArgs) }

    actual fun getQuantityString(resource: PluralStringResource, quantity: Int): String =
        runBlocking { getPluralString(resource, quantity) }

    actual fun getLocaleLanguageTag(): String =
        NSLocale.currentLocale.languageCode
}
