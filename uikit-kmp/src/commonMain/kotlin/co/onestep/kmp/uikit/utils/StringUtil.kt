package co.onestep.kmp.uikit.utils

/**
 * Returns a new string with the first character converted to uppercase.
 *
 * If the string is empty, the original string is returned.
 */
fun String.toCapitalLetter(): String =
    this.replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase() else char.toString()
    }

/**
 * Capitalizes the first word of the sentence, and the first word that appears after each comma in a string.
 *
 * E.g. "first part, second part, third part" -> "First part, Second part, Third part"
 */
fun String.capitalizeWordsAfterComma(separator: String? = null): String =
    this
        .split(',')
        .joinToString(separator ?: ", ") {
            it.trim().toCapitalLetter()
        }
