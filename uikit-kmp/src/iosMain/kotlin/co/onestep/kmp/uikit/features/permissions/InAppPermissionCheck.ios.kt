package co.onestep.kmp.uikit.features.permissions

import co.onestep.kmp.uikit.bridge.Permission
import co.onestep.kmp.uikit.bridge.PermissionStatus
import co.onestep.kmp.uikit.di.UIKitServiceLocator

internal actual fun hasRequiredInAppPermissions(): Boolean {
    val pm = UIKitServiceLocator.permissionsManager
    return pm.checkPermissionStatus(Permission.ACTIVITY_RECOGNITION) == PermissionStatus.GRANTED &&
        pm.checkPermissionStatus(Permission.LOCATION_WHEN_IN_USE) == PermissionStatus.GRANTED
}
