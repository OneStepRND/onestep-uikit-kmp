package co.onestep.kmp.uikit.bridge

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import co.onestep.kmp.uikit.features.permissions.PermissionsManagerImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

actual class PlatformPermissionsManager(
    context: Context? = null,
    preferencesBridge: PreferencesBridge? = null,
) {
    private val delegate: PermissionsManagerImpl? =
        if (context != null && preferencesBridge != null) PermissionsManagerImpl(context, preferencesBridge)
        else null

    private val appContext: Context? = context?.applicationContext

    actual fun requestActivityRecognition(): Flow<PermissionResult> =
        flowOf(PermissionResult(Permission.ACTIVITY_RECOGNITION, checkPermissionStatus(Permission.ACTIVITY_RECOGNITION)))

    actual fun requestNotifications(): Flow<PermissionResult> =
        flowOf(PermissionResult(Permission.NOTIFICATIONS, checkPermissionStatus(Permission.NOTIFICATIONS)))

    actual fun requestLocationWhenInUse(): Flow<PermissionResult> =
        flowOf(PermissionResult(Permission.LOCATION_WHEN_IN_USE, PermissionStatus.DENIED))

    actual fun requestLocationAlways(): Flow<PermissionResult> =
        flowOf(PermissionResult(Permission.LOCATION_ALWAYS, PermissionStatus.DENIED))

    actual fun requestHealthKit(): Flow<PermissionResult> =
        flowOf(PermissionResult(Permission.HEALTH_KIT, PermissionStatus.DENIED))

    actual fun checkPermissionStatus(permission: Permission): PermissionStatus {
        val ctx = appContext ?: return PermissionStatus.NOT_DETERMINED
        val androidPermission = permission.toAndroidManifestString() ?: return PermissionStatus.NOT_DETERMINED
        val granted = ContextCompat.checkSelfPermission(ctx, androidPermission) == PackageManager.PERMISSION_GRANTED
        return if (granted) PermissionStatus.GRANTED else PermissionStatus.DENIED
    }
}

private fun Permission.toAndroidManifestString(): String? = when (this) {
    Permission.ACTIVITY_RECOGNITION ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) Manifest.permission.ACTIVITY_RECOGNITION
        else null
    Permission.NOTIFICATIONS ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS
        else null
    Permission.MICROPHONE -> Manifest.permission.RECORD_AUDIO
    // iOS-only permissions — not applicable on Android
    Permission.LOCATION_WHEN_IN_USE,
    Permission.LOCATION_ALWAYS,
    Permission.HEALTH_KIT -> null
}
