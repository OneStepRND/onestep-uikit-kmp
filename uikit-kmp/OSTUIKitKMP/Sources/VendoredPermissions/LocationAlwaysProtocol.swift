//
//  LocationAlwaysProtocol.swift
//  OneStepUIKit
//
//  Created by David Havkin on 20/08/2025.
//

import Foundation


/// Protocol for handling location "always" authorization requests
/// This allows Core to request always authorization without directly calling the forbidden APIs
protocol LocationAlwaysProvider {
    /// Request location "always" authorization
    func requestAlwaysAuthorization()
}

/// Registry for location always provider
/// This allows the Location target to register itself with Core
class LocationAlwaysProviderRegistry {
    static let shared = LocationAlwaysProviderRegistry()
    
    private var provider: LocationAlwaysProvider?
    
    private init() {}
    
    /// Register a provider (called by Location target)
    func register(provider: LocationAlwaysProvider) {
        self.provider = provider
    }
    
    /// Request always authorization (called by Core)
    func requestAlwaysAuthorization() {
        // Try to initialize provider if not already registered
        if provider == nil {
            tryInitializeLocationProvider()
        }
        provider?.requestAlwaysAuthorization()
    }
    
    private func tryInitializeLocationProvider() {
        // Use runtime to find LocationAlwaysManager if it exists (OneStepUIKit product)
        // Fails gracefully in App Clip builds where Location target doesn't exist
        guard let managerClass = NSClassFromString("OneStepUIKitLocation.LocationAlwaysManager") as? NSObject.Type else {
            return
        }
        
        let selector = NSSelectorFromString("shared")
        guard managerClass.responds(to: selector),
              let sharedInstance = managerClass.perform(selector)?.takeUnretainedValue() else {
            return
        }
        
        // Wrap the instance to conform to our protocol
        provider = LocationProviderWrapper(manager: sharedInstance)
    }
    
    /// Check if provider is available
    var isProviderAvailable: Bool {
        return provider != nil
    }
}

/// Simple wrapper to call the LocationAlwaysManager through runtime
private class LocationProviderWrapper: LocationAlwaysProvider {
    private let manager: AnyObject
    
    init(manager: AnyObject) {
        self.manager = manager
    }
    
    func requestAlwaysAuthorization() {
        let selector = NSSelectorFromString("requestAlwaysAuthorization")
        guard manager.responds(to: selector) else { return }
        _ = manager.perform(selector)
    }
}
