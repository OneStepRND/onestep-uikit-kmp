package co.onestep.kmp.uikit.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import co.onestep.kmp.uikit.features.permissions.OSTPermissionMode
import co.onestep.kmp.uikit.features.permissions.PermissionSequence
import co.onestep.kmp.uikit.features.permissions.isGranted

fun requireActivityRecognitionPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACTIVITY_RECOGNITION,
        ) != PackageManager.PERMISSION_GRANTED
    }
    return false
}

/**
 * Checks if any permissions required by the given mode are missing/not granted.
 * Returns true if the permission flow should be shown (permissions are required).
 * Returns false if all permissions for the mode are already granted.
 */
fun requiresPermissionsByMode(
    mode: OSTPermissionMode,
    activity: Activity,
): Boolean = PermissionSequence.forMode(mode).permissions.any { !it.isGranted(activity) }
