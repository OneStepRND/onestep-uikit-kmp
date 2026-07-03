package co.onestep.kmp.uikit.features.permissions.ios

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
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.Foundation.NSURL
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Wraps native iOS framework calls for checking and requesting permissions.
 */
internal class IosPermissionChecker {

    private val locationManager = CLLocationManager()
    private val motionActivityManager = CMMotionActivityManager()

    fun checkStatus(type: IosPermissionType): IosPermissionStatus =
        when (type) {
            IosPermissionType.MOTION_FITNESS -> checkMotionStatus()
            IosPermissionType.LOCATION_WHILE_USING -> checkLocationWhileUsingStatus()
            IosPermissionType.LOCATION_ALWAYS -> checkLocationAlwaysStatus()
            IosPermissionType.HEALTH_KIT -> checkHealthKitStatus()
            IosPermissionType.MICROPHONE -> IosPermissionStatus.NOT_DETERMINED
        }

    /**
     * Check if the current location authorization might be "Allow Once".
     *
     * iOS does not provide a direct API to distinguish "Allow Once" from "While Using the App" —
     * both report as [kCLAuthorizationStatusAuthorizedWhenInUse] during the active session.
     * "Allow Once" reverts to [kCLAuthorizationStatusNotDetermined] after the app is terminated.
     *
     * PRD: "If user chooses allow only once, the flow should be stopped."
     * Since we cannot reliably detect this during the session, we rely on the fact that
     * denying the subsequent "Always" prompt will exit the flow (Fix 2), limiting the
     * impact to one extra screen shown.
     *
     * @return true if the authorization MIGHT be "Allow Once" (i.e., is WhenInUse
     *         and was not previously determined — meaning this is likely the first grant).
     *         This is a heuristic, not authoritative.
     */
    fun isLocationPossiblyAllowOnce(): Boolean {
        // If the status is WhenInUse and it was NOT previously requested/granted,
        // it's more likely to be "Allow Once" from this session.
        // However, this cannot be confirmed until the app restarts.
        return locationManager.authorizationStatus == kCLAuthorizationStatusAuthorizedWhenInUse
    }

    private fun checkMotionStatus(): IosPermissionStatus {
        if (!CMMotionActivityManager.isActivityAvailable()) return IosPermissionStatus.GRANTED
        return when (CMMotionActivityManager.authorizationStatus()) {
            CMAuthorizationStatusAuthorized -> IosPermissionStatus.GRANTED
            CMAuthorizationStatusDenied -> IosPermissionStatus.DENIED
            CMAuthorizationStatusRestricted -> IosPermissionStatus.RESTRICTED
            CMAuthorizationStatusNotDetermined -> IosPermissionStatus.NOT_DETERMINED
            else -> IosPermissionStatus.NOT_DETERMINED
        }
    }

    private fun checkLocationWhileUsingStatus(): IosPermissionStatus =
        when (locationManager.authorizationStatus) {
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways -> IosPermissionStatus.GRANTED
            kCLAuthorizationStatusDenied -> IosPermissionStatus.DENIED
            kCLAuthorizationStatusRestricted -> IosPermissionStatus.RESTRICTED
            kCLAuthorizationStatusNotDetermined -> IosPermissionStatus.NOT_DETERMINED
            else -> IosPermissionStatus.NOT_DETERMINED
        }

    private fun checkLocationAlwaysStatus(): IosPermissionStatus =
        when (locationManager.authorizationStatus) {
            kCLAuthorizationStatusAuthorizedAlways -> IosPermissionStatus.GRANTED
            kCLAuthorizationStatusDenied -> IosPermissionStatus.DENIED
            kCLAuthorizationStatusRestricted -> IosPermissionStatus.RESTRICTED
            kCLAuthorizationStatusNotDetermined,
            kCLAuthorizationStatusAuthorizedWhenInUse -> IosPermissionStatus.NOT_DETERMINED
            else -> IosPermissionStatus.NOT_DETERMINED
        }

    private fun checkHealthKitStatus(): IosPermissionStatus {
        if (!HKHealthStore.isHealthDataAvailable()) return IosPermissionStatus.RESTRICTED
        return IosPermissionStatus.NOT_DETERMINED
    }

    /**
     * Request motion permission by triggering a CoreMotion data query.
     * CoreMotion has no explicit "request" API — permission dialog shows on first data access.
     *
     * Non-suspending fire-and-forget: the persistent [motionActivityManager] keeps the manager
     * alive so ARC doesn't deallocate it before the system dialog appears.
     * Polling in the UI handles the permission result.
     */
    fun requestMotion() {
        if (!CMMotionActivityManager.isActivityAvailable()) return
        motionActivityManager.queryActivityStartingFromDate(
            start = NSDate.dateWithTimeIntervalSince1970(NSDate().timeIntervalSince1970 - 1.0),
            toDate = NSDate(),
            toQueue = NSOperationQueue.mainQueue,
        ) { _, _ ->
            // Query complete — polling in the UI handles the permission result.
        }
    }

    fun requestLocationWhenInUse() {
        locationManager.requestWhenInUseAuthorization()
    }

    fun requestLocationAlways() {
        locationManager.requestAlwaysAuthorization()
    }

    suspend fun requestHealthKit(): Boolean {
        if (!HKHealthStore.isHealthDataAvailable()) return false
        val store = HKHealthStore()
        val readTypes = setOf(
            HKQuantityType.quantityTypeForIdentifier(HKQuantityTypeIdentifierStepCount),
            HKQuantityType.quantityTypeForIdentifier(HKQuantityTypeIdentifierWalkingStepLength),
            HKQuantityType.quantityTypeForIdentifier(HKQuantityTypeIdentifierWalkingSpeed),
            HKQuantityType.quantityTypeForIdentifier(HKQuantityTypeIdentifierWalkingDoubleSupportPercentage),
        ).filterNotNull().toSet()

        return suspendCoroutine { continuation ->
            store.requestAuthorizationToShareTypes(
                typesToShare = null,
                readTypes = readTypes as Set<HKObjectType>,
            ) { success, _ ->
                continuation.resume(success)
            }
        }
    }

    fun openAppSettings() {
        val url = NSURL(string = UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(
            url,
            options = emptyMap<Any?, Any>(),
            completionHandler = null,
        )
    }
}
