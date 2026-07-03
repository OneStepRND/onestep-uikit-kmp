//
//  PermissionsFlowCoordinator.swift
//
//
//  Created by Maor Duani on 11/09/2024.
//

import SwiftUI
import CoreMotion
import CoreLocation
import OneStepSDK
import HealthKit

@MainActor
class PermissionsFlowCoordinator: NSObject, ObservableObject {
    @Published private(set) var currentScreen: PermissionFlowScreens?
    @Published var flowCompleted: Bool = false
    @Published var criticalPermissionsDenied: Bool = false
    private let motionManager = CMMotionActivityManager()
    private var locationManager = CLLocationManager()
    private let healthStore = HKHealthStore()
    var mode: PermissionFlowMode
    var screensToBeShown = [PermissionFlowScreens]()

    init(mode: PermissionFlowMode) {
        self.mode = mode

        super.init()

        //configure the flow, decide from beginning which screens should be shown,
        //build an array of screens to be shown
        //run initial screen in the array with nil value in nextScreen function
        Task {
            await configureTheFlow(mode: mode)
            self.nextScreen(currentScreen: nil)
        }
    }
    
    //any view calls this function to move to the next screen and provides itself as the current screen
    func nextScreen(currentScreen: PermissionFlowScreens?) {
        //case we are at the beginning of the flow
        guard let currentScreen else {
            //if we have no screens to show, we are done
            if self.screensToBeShown.count == 0 {
                self.checkForCriticalPermissionDenials()
                self.flowCompleted = true
            } else {
                //or we show the first screen
                self.currentScreen = self.screensToBeShown.first
            }
            return
        }
        
        //show next screen for all other cases
        if let index = self.screensToBeShown.firstIndex(of: currentScreen), index + 1 < self.screensToBeShown.count {
            self.currentScreen = self.screensToBeShown[index + 1]
        } else {
            // Before marking flow as completed, check for critical permission denials
            self.checkForCriticalPermissionDenials()
            self.flowCompleted = true
        }
    }
    
    private func configureTheFlow(mode: PermissionFlowMode) async {
        var permissionsByMode = [PermissionsNeeded]()
        
        //configure which permissions we require for every mode
        switch mode {
        case .healthKit:
            permissionsByMode = [.healthKit]
        case .inApp:
            permissionsByMode = [.motionAndFitness, .locationWhileInUse]
        case .background:
            permissionsByMode = [.motionAndFitness, .locationAlways]
        case .full:
            permissionsByMode = [.motionAndFitness, .locationAlways, .healthKit]
        }
        
        //filter out the permissions we already have
        await filterOutPermissionsWeAlreadyHave(&permissionsByMode)
        
        //set the screens to be shown
        await permissionsNeededConvertToScreensToShow(permissionsByMode)

        let hasShownPermissions = UserDefaults.standard.bool(forKey: UserDefaultsKeys.hasShownPermissionsRationalization)

        //if we have no screens to show, we are done
        if screensToBeShown.isEmpty {
            // Check for critical permission denials even when no screens are shown
            self.checkForCriticalPermissionDenials()
            self.flowCompleted = true
        } else if screensToBeShown.count >= 2 && !onlyTwoOfLocationPermissionsInside() && !hasShownPermissions {
            // if we have more than two screens, we need to show the rationalization screen,
            // in case it's not two of location permissions, and it hasn't been shown before
            screensToBeShown.insert(.permissionsRationalization, at: 0)
        }
    }
    
    private func onlyTwoOfLocationPermissionsInside() -> Bool {
        //check if we are in background mode and have any of location permissions screens
        screensToBeShown.filter({ String(describing: $0).contains("location") }).count >= 2 && screensToBeShown.count == 2
    }
    
    private func permissionsNeededConvertToScreensToShow(_ permissionsByMode: [PermissionsNeeded]) async {
        //append screens to be shown based on the array and the screen types
        //motion and fitness
        if permissionsByMode.contains(.motionAndFitness) {
            screensToBeShown.append(.motionAndFitnessScreen)
        }
        //location
        if permissionsByMode.contains(.locationWhileInUse) {
            screensToBeShown.append(.locationScreen(maxMode: .locationWhileInUse))
        } else if permissionsByMode.contains(.locationAlways) {
            screensToBeShown.append(.locationScreen(maxMode: .locationAlways))
        }
        
        //healthKit
        if permissionsByMode.contains(.healthKit) {
            screensToBeShown.append(.healthKitScreen)
        }
    }
    
    private func filterOutPermissionsWeAlreadyHave(_ permissionsLacking: inout [PermissionsNeeded]) async {
        if permissionsLacking.contains(.motionAndFitness) && CMMotionActivityManager.authorizationStatus() == .authorized {
            permissionsLacking.removeAll { $0 == .motionAndFitness }
        }
        
        if permissionsLacking.contains(.locationWhileInUse) && (locationManager.authorizationStatus == .authorizedWhenInUse || locationManager.authorizationStatus == .authorizedAlways) {
            permissionsLacking.removeAll { $0 == .locationWhileInUse }
        }
        
        if permissionsLacking.contains(.locationAlways) && locationManager.authorizationStatus == .authorizedAlways {
            permissionsLacking.removeAll { $0 == .locationAlways }
        }
        
        if permissionsLacking.contains(.healthKit) {
            if !(await healthkitShouldBeRequested()) {
                permissionsLacking.removeAll { $0 == .healthKit }
            }
        }
    }
    
    func healthkitShouldBeRequested() async -> Bool {
        let healthKitPermissionsRequested = await PermissionsValidator.healthKitPermissionsRequested()
        let healthKitPermissionsGranted = await PermissionsValidator.checkIfAllHealthKitPermissionsGranted()
        
        if !healthKitPermissionsRequested || !healthKitPermissionsGranted {
            return true
        }
        
        return false
    }
    
    func requestLocationAlways() {
        LocationAlwaysProviderRegistry.shared.requestAlwaysAuthorization()
    }
    
    func requestLocationWhileInUse() {
        locationManager.requestWhenInUseAuthorization()
    }
    
    func requestMotionAndFitnessPermission() {
        motionManager.queryActivityStarting(
            from: .now,
            to: .now,
            to: .main
        ) { _, _ in
            self.motionManager.stopActivityUpdates()
        }
    }
    
    func checkForCriticalPermissionDenials() {
        // Check if motion and fitness permission was denied
        let motionStatus = CMMotionActivityManager.authorizationStatus()
        let locationStatus = locationManager.authorizationStatus
        
        let motionDenied = motionStatus == .denied || motionStatus == .restricted
        let locationDenied = locationStatus == .denied || locationStatus == .restricted
        
        // For inApp mode, we need both motion and location (when in use)
        if mode == .inApp && (motionDenied || locationDenied) {
            criticalPermissionsDenied = true
        }
        // For background and full modes, we need both motion and location (always)
        else if (mode == .background || mode == .full) && (motionDenied || locationDenied) {
            criticalPermissionsDenied = true
        }
    }
    
    func areRequiredPermissionsMissing() async -> Bool {
        let motionStatus = CMMotionActivityManager.authorizationStatus()
        let locationStatus = locationManager.authorizationStatus

        switch mode {
        case .inApp:
            // For inApp mode, we need motion and location when in use
            let motionMissing = motionStatus != .authorized
            let locationMissing = locationStatus != .authorizedWhenInUse && locationStatus != .authorizedAlways
            return motionMissing || locationMissing

        case .background:
            // For background mode, we need motion and location always
            let motionMissing = motionStatus != .authorized
            let locationMissing = locationStatus != .authorizedAlways
            return motionMissing || locationMissing

        case .full:
            // For full mode, we need motion, location always, AND healthKit
            let motionMissing = motionStatus != .authorized
            let locationMissing = locationStatus != .authorizedAlways
            let healthKitMissing = await healthkitShouldBeRequested()
            return motionMissing || locationMissing || healthKitMissing

        case .healthKit:
            // For healthKit mode, we primarily need HealthKit permissions
            let healthKitMissing = await healthkitShouldBeRequested()
            return healthKitMissing
        }
    }

    // MARK: - Analytics Helper Methods

    /// Get the flow name string for analytics
    func getFlowName() -> String {
        switch mode {
        case .inApp:
            return FlowName.inApp.rawValue
        case .background:
            return FlowName.background.rawValue
        case .full:
            return FlowName.full.rawValue
        case .healthKit:
            return FlowName.healthKit.rawValue
        }
    }

    /// Track close button click with appropriate permission and variant
    func trackCloseButtonClick(on screen: PermissionFlowScreens) {
        let flowName = getFlowName()
        let (permission, variant) = getPermissionAndVariantForScreen(screen)

        PermissionsFlowAnalytics.trackClick(
            "permission_close",
            permission: permission,
            variant: variant,
            flowName: flowName
        )
    }

    /// Determine permission and variant from the current screen
    private func getPermissionAndVariantForScreen(_ screen: PermissionFlowScreens) -> (permission: String, variant: String) {
        switch screen {
        case .locationScreen(maxMode: .locationAlways):
            let variant = determineLocationVariant(forAlways: true)
            return (PermissionType.locationAlways.rawValue, variant)
        case .locationScreen(maxMode: .locationWhileInUse):
            let variant = determineLocationVariant(forAlways: false)
            return (PermissionType.locationWhileUsing.rawValue, variant)
        case .motionAndFitnessScreen:
            let variant = determineMotionVariant()
            return (PermissionType.motionFitness.rawValue, variant)
        case .healthKitScreen:
            let variant = determineHealthKitVariant()
            return (PermissionType.healthkit.rawValue, variant)
        case .permissionsRationalization:
            // For the rationalization screen, use the first permission that will be asked
            if let firstScreen = screensToBeShown.first(where: { $0 != .permissionsRationalization }) {
                return getPermissionAndVariantForScreen(firstScreen)
            }
            return (PermissionType.motionFitness.rawValue, PermissionVariant.firstTime.rawValue)
        default:
            return (PermissionType.motionFitness.rawValue, PermissionVariant.firstTime.rawValue)
        }
    }

    /// Determine variant for location permissions
    func determineLocationVariant(forAlways: Bool) -> String {
        let status = locationManager.authorizationStatus

        // If denied or restricted, it's after_denied
        if status == .denied || status == .restricted {
            return PermissionVariant.afterDenied.rawValue
        }

        // For location always permission, check if we're showing settings screen (after denied)
        if forAlways {
            // If user previously had always but it was downgraded, or if they denied always before
            let viewModel = LocationPermissionsViewModel()
            if viewModel.everDenied || (status == .authorizedWhenInUse && viewModel.everAskedAlways) {
                return PermissionVariant.afterDenied.rawValue
            }
        }

        return PermissionVariant.firstTime.rawValue
    }

    /// Determine variant for motion and fitness
    func determineMotionVariant() -> String {
        let status = CMMotionActivityManager.authorizationStatus()
        if status == .denied {
            return PermissionVariant.afterDenied.rawValue
        } else if status == .restricted {
            return PermissionVariant.restricted.rawValue
        }
        return PermissionVariant.firstTime.rawValue
    }

    /// Determine variant for HealthKit
    func determineHealthKitVariant() -> String {
        // HealthKit doesn't have a simple denied state, so we default to first_time
        // Could be enhanced based on UserDefaults tracking if needed
        return PermissionVariant.firstTime.rawValue
    }

}


