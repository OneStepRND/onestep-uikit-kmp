package co.onestep.kmp.uikit.testapp

import android.content.Context

/**
 * Thin SharedPreferences wrapper for the Settings/Login screen, using the same keys as
 * `iosTestApp`'s `UserDefaults` (`sdk_environment`, `sdk_customURL`, `sdk_orgName`,
 * `sdk_distinctId`) so both test apps persist the same identity selections.
 */
class SettingsPrefs(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("sdk_settings", Context.MODE_PRIVATE)

    var environment: String
        get() = prefs.getString(KEY_ENVIRONMENT, SDKEnvironment.PRODUCTION.rawValue) ?: SDKEnvironment.PRODUCTION.rawValue
        set(value) = prefs.edit().putString(KEY_ENVIRONMENT, value).apply()

    var customUrl: String
        get() = prefs.getString(KEY_CUSTOM_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_URL, value).apply()

    var orgName: String
        get() = prefs.getString(KEY_ORG_NAME, Organizations.default.name) ?: Organizations.default.name
        set(value) = prefs.edit().putString(KEY_ORG_NAME, value).apply()

    var distinctId: String
        get() = prefs.getString(KEY_DISTINCT_ID, AppConstants.DEFAULT_DISTINCT_ID) ?: AppConstants.DEFAULT_DISTINCT_ID
        set(value) = prefs.edit().putString(KEY_DISTINCT_ID, value).apply()

    private companion object {
        const val KEY_ENVIRONMENT = "sdk_environment"
        const val KEY_CUSTOM_URL = "sdk_customURL"
        const val KEY_ORG_NAME = "sdk_orgName"
        const val KEY_DISTINCT_ID = "sdk_distinctId"
    }
}

/** Mirrors iOS `SDKEnvironment` (Production / Custom URL). */
enum class SDKEnvironment(val rawValue: String) {
    PRODUCTION("Production"),
    CUSTOM("Custom URL");

    companion object {
        fun fromRaw(raw: String?): SDKEnvironment =
            entries.firstOrNull { it.rawValue == raw } ?: PRODUCTION
    }
}
