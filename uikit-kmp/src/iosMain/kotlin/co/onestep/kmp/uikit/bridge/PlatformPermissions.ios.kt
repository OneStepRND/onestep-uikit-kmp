package co.onestep.kmp.uikit.bridge

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.CoreMotion.CMAuthorizationStatusAuthorized
import platform.CoreMotion.CMAuthorizationStatusDenied
import platform.CoreMotion.CMAuthorizationStatusNotDetermined
import platform.CoreMotion.CMAuthorizationStatusRestricted
import platform.CoreMotion.CMMotionActivityManager
import platform.Foundation.NSDate
import platform.Foundation.NSOperationQueue
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970
import platform.HealthKit.HKHealthStore
import platform.HealthKit.HKObjectType
import platform.HealthKit.HKQuantityType
import platform.HealthKit.HKQuantityTypeIdentifierStepCount
import platform.HealthKit.HKQuantityTypeIdentifierWalkingDoubleSupportPercentage
import platform.HealthKit.HKQuantityTypeIdentifierWalkingSpeed
import platform.HealthKit.HKQuantityTypeIdentifierWalkingStepLength
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class PlatformPermissionsManager {

    private val locationManager = CLLocationManager()
    private var motionManager: CMMotionActivityManager? = null

    actual fun requestActivityRecognition(): Flow<PermissionResult> = flow {
        if (!CMMotionActivityManager.isActivityAvailable()) {
            emit(PermissionResult(Permission.ACTIVITY_RECOGNITION, PermissionStatus.DENIED))
            return@flow
        }
        val status = CMMotionActivityManager.authorizationStatus()
        if (status == CMAuthorizationStatusAuthorized) {
            emit(PermissionResult(Permission.ACTIVITY_RECOGNITION, PermissionStatus.GRANTED))
            return@flow
        }
        // Trigger the permission dialog by querying activity data
        val granted = suspendCoroutine { continuation ->
            val manager = CMMotionActivityManager()
            motionManager = manager
            manager.queryActivityStartingFromDate(
                start = NSDate.dateWithTimeIntervalSince1970(NSDate().timeIntervalSince1970 - 1.0),
                toDate = NSDate(),
                toQueue = NSOperationQueue.mainQueue
            ) { _, error ->
                val authStatus = CMMotionActivityManager.authorizationStatus()
                motionManager = null
                continuation.resume(authStatus == CMAuthorizationStatusAuthorized)
            }
        }
        val permStatus = if (granted) PermissionStatus.GRANTED else PermissionStatus.DENIED
        emit(PermissionResult(Permission.ACTIVITY_RECOGNITION, permStatus))
    }

    actual fun requestNotifications(): Flow<PermissionResult> = flow {
        val granted = suspendCoroutine { continuation ->
            val center = UNUserNotificationCenter.currentNotificationCenter()
            val options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
            center.requestAuthorizationWithOptions(options) { granted, _ ->
                continuation.resume(granted)
            }
        }
        val status = if (granted) PermissionStatus.GRANTED else PermissionStatus.DENIED
        emit(PermissionResult(Permission.NOTIFICATIONS, status))
    }

    actual fun requestLocationWhenInUse(): Flow<PermissionResult> = flow {
        locationManager.requestWhenInUseAuthorization()
        // The actual result is delivered asynchronously via CLLocationManagerDelegate.
        // For now, emit NOT_DETERMINED — the coordinator will poll status.
        emit(PermissionResult(Permission.LOCATION_WHEN_IN_USE, PermissionStatus.NOT_DETERMINED))
    }

    actual fun requestLocationAlways(): Flow<PermissionResult> = flow {
        locationManager.requestAlwaysAuthorization()
        emit(PermissionResult(Permission.LOCATION_ALWAYS, PermissionStatus.NOT_DETERMINED))
    }

    actual fun requestHealthKit(): Flow<PermissionResult> = flow {
        if (!HKHealthStore.isHealthDataAvailable()) {
            emit(PermissionResult(Permission.HEALTH_KIT, PermissionStatus.DENIED))
            return@flow
        }
        val store = HKHealthStore()
        val readTypes = setOf(
            HKQuantityType.quantityTypeForIdentifier(HKQuantityTypeIdentifierStepCount),
            HKQuantityType.quantityTypeForIdentifier(HKQuantityTypeIdentifierWalkingStepLength),
            HKQuantityType.quantityTypeForIdentifier(HKQuantityTypeIdentifierWalkingSpeed),
            HKQuantityType.quantityTypeForIdentifier(HKQuantityTypeIdentifierWalkingDoubleSupportPercentage),
        ).filterNotNull().toSet()

        val granted = suspendCoroutine { continuation ->
            store.requestAuthorizationToShareTypes(
                typesToShare = null,
                readTypes = readTypes as Set<HKObjectType>,
            ) { success, _ ->
                continuation.resume(success)
            }
        }
        val status = if (granted) PermissionStatus.GRANTED else PermissionStatus.DENIED
        emit(PermissionResult(Permission.HEALTH_KIT, status))
    }

    actual fun checkPermissionStatus(permission: Permission): PermissionStatus {
        return when (permission) {
            Permission.ACTIVITY_RECOGNITION -> {
                when (CMMotionActivityManager.authorizationStatus()) {
                    CMAuthorizationStatusAuthorized -> PermissionStatus.GRANTED
                    CMAuthorizationStatusDenied, CMAuthorizationStatusRestricted -> PermissionStatus.DENIED
                    CMAuthorizationStatusNotDetermined -> PermissionStatus.NOT_DETERMINED
                    else -> PermissionStatus.NOT_DETERMINED
                }
            }
            Permission.NOTIFICATIONS -> PermissionStatus.NOT_DETERMINED
            Permission.MICROPHONE -> PermissionStatus.NOT_DETERMINED
            Permission.LOCATION_WHEN_IN_USE -> {
                when (CLLocationManager.authorizationStatus()) {
                    kCLAuthorizationStatusAuthorizedWhenInUse,
                    kCLAuthorizationStatusAuthorizedAlways -> PermissionStatus.GRANTED
                    kCLAuthorizationStatusDenied,
                    kCLAuthorizationStatusRestricted -> PermissionStatus.DENIED
                    kCLAuthorizationStatusNotDetermined -> PermissionStatus.NOT_DETERMINED
                    else -> PermissionStatus.NOT_DETERMINED
                }
            }
            Permission.LOCATION_ALWAYS -> {
                when (CLLocationManager.authorizationStatus()) {
                    kCLAuthorizationStatusAuthorizedAlways -> PermissionStatus.GRANTED
                    kCLAuthorizationStatusDenied,
                    kCLAuthorizationStatusRestricted -> PermissionStatus.DENIED
                    kCLAuthorizationStatusNotDetermined,
                    kCLAuthorizationStatusAuthorizedWhenInUse -> PermissionStatus.NOT_DETERMINED
                    else -> PermissionStatus.NOT_DETERMINED
                }
            }
            Permission.HEALTH_KIT -> {
                if (!HKHealthStore.isHealthDataAvailable()) return PermissionStatus.DENIED
                // HealthKit doesn't provide a simple synchronous check for read authorization
                PermissionStatus.NOT_DETERMINED
            }
        }
    }
}
