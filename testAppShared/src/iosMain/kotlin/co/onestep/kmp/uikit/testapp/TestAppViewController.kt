package co.onestep.kmp.uikit.testapp

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * Entry point for the iOS shell: hosts the whole shared test app. The Swift app implements
 * [TestAppShell] against the native OneStepSDK and presents this as its root view controller.
 */
fun TestAppViewController(shell: TestAppShell): UIViewController =
    ComposeUIViewController {
        TestAppRoot(shell = shell, prefs = SettingsPrefs())
    }
