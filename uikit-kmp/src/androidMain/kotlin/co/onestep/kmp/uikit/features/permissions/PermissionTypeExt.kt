package co.onestep.kmp.uikit.features.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager

/**
 * Maps each [PermissionType] to its Android manifest permission string.
 * Returns null for special permissions like BATTERY_OPTIMIZATION that don't use
 * the runtime permission system.
 */
val PermissionType.androidPermission: String?
    get() = when (this) {
        PermissionType.ACTIVITY_RECOGNITION -> Manifest.permission.ACTIVITY_RECOGNITION
        PermissionType.POST_NOTIFICATIONS -> Manifest.permission.POST_NOTIFICATIONS
        PermissionType.BATTERY_OPTIMIZATION -> null
        PermissionType.RECORD_AUDIO -> Manifest.permission.RECORD_AUDIO
    }

/**
 * Checks whether this permission is currently granted on the device.
 * Handles version-gated checks for ACTIVITY_RECOGNITION (Q+) and POST_NOTIFICATIONS (TIRAMISU+),
 * PowerManager check for BATTERY_OPTIMIZATION, and standard checkSelfPermission for others.
 */
fun PermissionType.isGranted(activity: Activity): Boolean =
    when (this) {
        PermissionType.ACTIVITY_RECOGNITION -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                activity.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) ==
                    PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        }
        PermissionType.POST_NOTIFICATIONS -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                activity.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        }
        PermissionType.BATTERY_OPTIMIZATION -> {
            val powerManager = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(activity.packageName)
        }
        PermissionType.RECORD_AUDIO -> {
            activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        }
    }

/**
 * Converts an Android manifest permission string to the corresponding [PermissionType].
 *
 * @param permission The Android manifest permission string
 * @return The matching PermissionType, or null if no match is found
 */
fun PermissionType.Companion.fromAndroidPermission(permission: String): PermissionType? =
    PermissionType.entries.find { it.androidPermission == permission }
