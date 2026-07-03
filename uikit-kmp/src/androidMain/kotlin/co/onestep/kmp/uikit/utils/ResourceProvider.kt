package co.onestep.kmp.uikit.utils

import android.content.Context
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.getPluralString

actual class ResourceProvider(
    val context: Context,
) {
    actual fun getString(resource: StringResource): String =
        runBlocking { org.jetbrains.compose.resources.getString(resource) }

    actual fun getString(resource: StringResource, vararg formatArgs: Any): String =
        runBlocking { org.jetbrains.compose.resources.getString(resource, *formatArgs) }

    actual fun getQuantityString(resource: PluralStringResource, quantity: Int): String =
        runBlocking { getPluralString(resource, quantity) }

    actual fun getLocaleLanguageTag(): String =
        context.resources.configuration.locales[0].toLanguageTag()

    // Legacy helpers for transition — used by ViewModels still referencing R.string
    fun getStringById(resId: Int): String = context.getString(resId)
    fun getStringById(resId: Int, vararg formatArgs: Any): String = context.getString(resId, *formatArgs)
    fun getQuantityStringById(resId: Int, quantity: Int, vararg formatArgs: Any): String =
        context.resources.getQuantityString(resId, quantity, *formatArgs)
}
