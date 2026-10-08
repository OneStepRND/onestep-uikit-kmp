//
//  PermissionsValidator.swift
//  OneStepUIKit
//
//  Created by David Havkin on 28/01/2025.
//
//  HEALTHKIT REMOVED (App Store guideline 2.5.1; see the HealthKit-removal commit).
//
//  This file used to carry ~200 lines of HealthKit: an `HKHealthStore`, an authorization request
//  for stepCount / walkingStepLength / walkingSpeed / walkingDoubleSupportPercentage, and a
//  weekly `HKStatisticsCollectionQuery` per type used to infer whether those reads were actually
//  granted. The package now pins the HealthKit-free `-core` flavour of OneStepSDK (see
//  gradle/libs.versions.toml → onestepSdkIos), whose binary has no `HealthKitManager`, and the
//  `.healthKit` / `.full` permission modes no longer request HealthKit — so this code could
//  neither compile nor run. A host that needs HealthKit permissions must own that flow itself.
//

import OneStepSDK
import CoreLocation
import CoreMotion
import AVFoundation

struct PermissionsValidator {
    static var locationManager = CLLocationManager()

    static func micPermissionInPlace() -> Bool {
        let status = AVCaptureDevice.authorizationStatus(for: .audio)
        return status == .authorized
    }

    static func requestMicPermission(completion: @escaping (Bool) -> Void) {
        AVCaptureDevice.requestAccess(for: .audio) { granted in
            DispatchQueue.main.async {
                completion(granted)
            }
        }
    }

    static func allPermissionsGranted() async -> Bool {
        let locationAlways = locationManager.authorizationStatus == .authorizedAlways
        let motionAndFitness = CMMotionActivityManager.authorizationStatus() == .authorized

        return locationAlways && motionAndFitness
    }
}
