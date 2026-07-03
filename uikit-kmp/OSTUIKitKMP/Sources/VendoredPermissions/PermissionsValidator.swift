//
//  PermissionsValidator.swift
//  OneStepUIKit
//
//  Created by David Havkin on 28/01/2025.
//

import HealthKit
import OneStepSDK
import CoreLocation
import CoreMotion
import AVFoundation

struct PermissionsValidator {
    static var healthStore = HKHealthStore()
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
    
    static func healthKitPermissionsRequested() async -> Bool {
        guard HKHealthStore.isHealthDataAvailable() else {
            return false
        }
        
        let stepsPermissions = await HealthKitManager.checkPermissionsWereAsked(for: .stepCount)
        let stepLengthPermissions = await HealthKitManager.checkPermissionsWereAsked(for: .walkingStepLength)
        let speedPermissions = await HealthKitManager.checkPermissionsWereAsked(for: .walkingSpeed)
        let doubleSupportPermissions = await HealthKitManager.checkPermissionsWereAsked(for: .walkingDoubleSupportPercentage)
        
        return stepsPermissions && stepLengthPermissions && speedPermissions && doubleSupportPermissions
    }
    
    static func allPermissionsGranted() async -> Bool {
        let locationAlways = locationManager.authorizationStatus == .authorizedAlways
        let motionAndFitness = CMMotionActivityManager.authorizationStatus() == .authorized
        let healthKit = await checkIfAllHealthKitPermissionsGranted()
        
        return locationAlways && motionAndFitness && healthKit
    }
    
    static func askForHealthKitAuthorizationFirstTime() async -> Bool {
        await withCheckedContinuation { continuation in
            guard HKHealthStore.isHealthDataAvailable() else {
                continuation.resume(returning: false)
                return
            }
            
            let typesToRead = Set([HKObjectType.quantityType(forIdentifier: .stepCount)!,
                                   HKObjectType.quantityType(forIdentifier: .walkingStepLength)!,
                                   HKObjectType.quantityType(forIdentifier: .walkingSpeed)!,
                                   HKObjectType.quantityType(forIdentifier: .walkingDoubleSupportPercentage)!])
            healthStore.requestAuthorization(toShare: [], read: typesToRead, completion: { _, _ in
                continuation.resume(returning: true)
            })
        }
    }
    
    
    static func checkIfAllHealthKitPermissionsGranted() async -> Bool {
        let typesToCheck: [HKQuantityTypeIdentifier] = [.stepCount, .walkingStepLength, .walkingSpeed, .walkingDoubleSupportPercentage]
        for type in typesToCheck {
            let success = await fetchWeeklyMetricAsync(identifier: type)
            if !success {
                // If any fails, just return false right away
                return false
            }
        }
        
        // 3. If all succeeded, return true
        return true
    }
    
    static func fetchWeeklyMetricAsync(identifier: HKQuantityTypeIdentifier) async -> Bool {
        do {
            return try await withCheckedThrowingContinuation { continuation in
                
                // 1. Create the quantity type from the identifier
                guard let quantityType = HKQuantityType.quantityType(forIdentifier: identifier) else {
                    let error = NSError(
                        domain: "HealthKitManager",
                        code: 0,
                        userInfo: [NSLocalizedDescriptionKey: LocalizedStrings.invalidQuantityType]
                    )
                    continuation.resume(throwing: error)
                    return
                }
                
                // 2. Decide the statistics options and unit based on the identifier
                let (options, unit) = chooseOptionsAndUnit(for: identifier)
                
                // 3. Compute date range: 7 days ago → now
                let now = Date()
                guard let oneWeekAgo = Calendar.current.date(byAdding: .day, value: -7, to: now) else {
                    let error = NSError(
                        domain: "HealthKitManager",
                        code: 1,
                        userInfo: [NSLocalizedDescriptionKey: LocalizedStrings.unableToComputeDateFor7DaysAgo]
                    )
                    continuation.resume(throwing: error)
                    return
                }
                
                let predicate = HKQuery.predicateForSamples(
                    withStart: oneWeekAgo,
                    end: now,
                    options: .strictStartDate
                )
                
                // 4. 1-day interval
                var interval = DateComponents()
                interval.day = 1
                
                let anchorDate = Calendar.current.startOfDay(for: oneWeekAgo)
                
                // 5. Create the query
                let query = HKStatisticsCollectionQuery(
                    quantityType: quantityType,
                    quantitySamplePredicate: predicate,
                    options: options,
                    anchorDate: anchorDate,
                    intervalComponents: interval
                )
                
                // 6. Initial results handler
                query.initialResultsHandler = { _, statsCollection, error in
                    if let error = error {
                        continuation.resume(throwing: error)
                        return
                    }
                    
                    guard let statsCollection = statsCollection else {
                        // No error, but no collection => treat as zero data
                        continuation.resume(returning: false)
                        return
                    }
                    
                    // 7. Enumerate to see if ANY day has a non-zero value
                    var foundData = false
                    statsCollection.enumerateStatistics(from: oneWeekAgo, to: now) { stats, stop in
                        switch options {
                        case .cumulativeSum:
                            if let sum = stats.sumQuantity() {
                                let value = sum.doubleValue(for: unit)
                                if value > 0 {
                                    foundData = true
                                    stop.pointee = true
                                }
                            }
                            
                        case .discreteAverage:
                            if let avg = stats.averageQuantity() {
                                let value = avg.doubleValue(for: unit)
                                if value > 0 {
                                    foundData = true
                                    stop.pointee = true
                                }
                            }
                            
                            // If you wanted to handle .discreteMin, .discreteMax, etc., you could add them here.
                        default:
                            // If it’s neither .cumulativeSum nor .discreteAverage, treat as no data or throw an error
                            stop.pointee = true
                        }
                    }
                    
                    continuation.resume(returning: foundData)
                }
                
                // 8. Execute the query
                HKHealthStore().execute(query)
            }
        } catch {
            // If any error is thrown => false
            return false
        }
    }
    
    static private func chooseOptionsAndUnit(
        for identifier: HKQuantityTypeIdentifier
    ) -> (HKStatisticsOptions, HKUnit) {
        switch identifier {
        case .stepCount:
            // Step count is cumulative data
            return (.cumulativeSum, .count())
            
        case .distanceWalkingRunning:
            // Distance is cumulative in meters
            return (.cumulativeSum, .meter())
            
        case .flightsClimbed:
            // Flights are also cumulative (count)
            return (.cumulativeSum, .count())
            
        case .walkingSpeed:
            // Speed is discrete, typically in m/s
            return (.discreteAverage, .meter().unitDivided(by: .second()))
            
        case .walkingStepLength:
            // Step length is discrete, in meters
            return (.discreteAverage, .meter())
            
        case .walkingDoubleSupportPercentage:
            // Percentage is discrete, in "percent" (0-100).
            // HKUnit.percent() is iOS 16+;
            // if older, you might do .count() and interpret 0.0–1.0 as fraction.
            if #available(iOS 16.0, *) {
                return (.discreteAverage, .percent())
            } else {
                return (.discreteAverage, .count())
            }
            
        default:
            // Fallback if unknown; .discreteAverage + .count
            return (.discreteAverage, .count())
        }
    }
}
