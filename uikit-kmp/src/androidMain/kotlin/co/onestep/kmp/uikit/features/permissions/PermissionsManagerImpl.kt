package co.onestep.kmp.uikit.features.permissions

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import co.onestep.kmp.uikit.bridge.PreferencesBridge

internal class PermissionsManagerImpl(
    private val context: Context,
    private val preferencesBridge: PreferencesBridge,
) : PermissionsManager {

    override fun checkPermissions(permission: String): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            permission,
        ) == PackageManager.PERMISSION_GRANTED

    override fun markPermissionRequested(permission: String) {
        preferencesBridge.setPermissionRequested(permission)
    }

    override fun hasRequestedPermissionBefore(permission: String): Boolean =
        preferencesBridge.hasPermissionRequestedBefore(permission)
}

/**
 * Android-specific extension to determine the full permission status,
 * using Activity-dependent APIs like shouldShowRequestPermissionRationale.
 *
 * @param permission The Android manifest permission string
 * @param activity The current Activity (needed for rationale check)
 * @param probeCompleted Whether we've already probed this permission in the current session
 * @return The appropriate [UiKitPermissionStatus]
 */
fun PermissionsManager.getPermissionStatus(
    permission: String,
    activity: Activity,
    probeCompleted: Boolean = false,
): UiKitPermissionStatus {
    if (checkPermissions(permission)) {
        return UiKitPermissionStatus.Granted
    }

    val shouldShow = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)

    return UiKitPermissionStatus.fromAndroidState(
        isGranted = false,
        shouldShowRationale = shouldShow,
        probeCompleted = probeCompleted,
    )
}
