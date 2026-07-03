//
//  LocationPermissionsView.swift
//  OneStepUIKit
//
//  Created by David Havkin on 05/05/2025.
//

import SwiftUI
import CoreLocation
import UIKit

class LocationPermissionsViewModel: NSObject, CLLocationManagerDelegate, ObservableObject {
    private let locationManager = CLLocationManager()
    @Published var permissionStatus: CLAuthorizationStatus
    @Published var everAskedAlways: Bool
    @Published var everHadAlways: Bool
    @Published var everDenied: Bool
    @Published var isAwaitingAlwaysResponse: Bool = false
    @Published var shouldShowSettingsView: Bool = false
    @Published var shouldPollForChanges: Bool = true
    @Published var isDetectingPopup: Bool = false
    @Published var alwaysPopupDidNotShow: Bool = false
    
    var onPermissionDenied: (() -> Void)?
    
    override init() {
        let currentStatus = CLLocationManager().authorizationStatus
        let hadAskedAlways = UserDefaults.standard.bool(forKey: SDKUIKitUserDefaultsKeys.hadAskedForLocationAlwaysOnce)
        let hadAlways = UserDefaults.standard.bool(forKey: SDKUIKitUserDefaultsKeys.hadLocationAlwaysOnce)
        let hadDenied = UserDefaults.standard.bool(forKey: SDKUIKitUserDefaultsKeys.hadLocationDeniedOnce)
        
        permissionStatus = currentStatus
        everAskedAlways = hadAskedAlways
        everHadAlways = hadAlways
        everDenied = hadDenied
        
        // Determine initial view state based on current permission status
        if currentStatus == .denied || currentStatus == .restricted {
            shouldShowSettingsView = true
            shouldPollForChanges = false
        } else if currentStatus == .authorizedAlways && hadAskedAlways {
            // If we previously had always permission, don't poll
            shouldPollForChanges = false
        } else {
            shouldShowSettingsView = false
            shouldPollForChanges = true
        }
        
        super.init()
        locationManager.delegate = self
    }
    
    func requestLocationWhenInUsePermission() {
        locationManager.requestWhenInUseAuthorization()
    }
    
    func requestLocationAlwaysPermission() {
        isAwaitingAlwaysResponse = true
        isDetectingPopup = true
        alwaysPopupDidNotShow = false

        // Call the Always authorization
        LocationAlwaysProviderRegistry.shared.requestAlwaysAuthorization()

        // Check after 0.5s if popup appeared
        // If iOS shows the system popup, the app will deactivate and isDetectingPopup will be cleared
        // If iOS ignores the request (e.g., user had "Allow Once"), nothing happens and we detect it here
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) { [weak self] in
            guard let self = self, self.isDetectingPopup else { return }

            // Still detecting means popup didn't show - iOS ignored the request
            print("Location Always popup did not appear - likely user had 'Allow Once'")
            self.alwaysPopupDidNotShow = true
            self.isDetectingPopup = false
            self.shouldShowSettingsView = true
            self.isAwaitingAlwaysResponse = false
        }
    }
    
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        let previousStatus = permissionStatus
        permissionStatus = manager.authorizationStatus
        
        print("Previous status was: previously \(previousStatus), current is: \(manager.authorizationStatus)")
        
        // Persist if 'Always' was granted at least once
        if manager.authorizationStatus == .authorizedAlways {
            //add additional parameter that will save specifically that we had permission always
            UserDefaults.standard.set(true, forKey: SDKUIKitUserDefaultsKeys.hadLocationAlwaysOnce)
            UserDefaults.standard.set(true, forKey: SDKUIKitUserDefaultsKeys.hadAskedForLocationAlwaysOnce)
            everAskedAlways = true
        }
        
        // Handle fresh denial - if we were polling and permission became denied
        if shouldPollForChanges && previousStatus == .notDetermined && 
           (manager.authorizationStatus == .denied || manager.authorizationStatus == .restricted) {
            shouldPollForChanges = false
            // Track that location was denied at some point
            everDenied = true
            UserDefaults.standard.set(true, forKey: SDKUIKitUserDefaultsKeys.hadLocationDeniedOnce)
            onPermissionDenied?()
        }
    }
    
    func handleReturnedFromAlwaysPrompt() {
        guard isAwaitingAlwaysResponse else { return }
        isAwaitingAlwaysResponse = false
        // mark that we've now asked Always
        everAskedAlways = true
        UserDefaults.standard.set(true, forKey: SDKUIKitUserDefaultsKeys.hadAskedForLocationAlwaysOnce)
        // refresh the status
        permissionStatus = locationManager.authorizationStatus
    }
}

struct LocationPermissionsView: View {
    @StateObject private var viewModel = LocationPermissionsViewModel()
    let neededPermissionsLevel: PermissionsNeeded
    @EnvironmentObject var coordinator: PermissionsFlowCoordinator

    private var permissionLevelReached: Bool {
        switch neededPermissionsLevel {
        case .locationAlways:
            // Only consider fully-authorized as "reached"
            return viewModel.permissionStatus == .authorizedAlways
        case .locationWhileInUse:
            // "Always" also counts as "While In Use"
            return viewModel.permissionStatus == .authorizedWhenInUse
            || viewModel.permissionStatus == .authorizedAlways
        default:
            return false
        }
    }

    private var shouldShowSettingsButton: Bool {
        viewModel.shouldShowSettingsView ||
        (neededPermissionsLevel == .locationAlways
            && viewModel.everAskedAlways
            && viewModel.permissionStatus != .authorizedAlways) ||
        (neededPermissionsLevel == .locationAlways
            && viewModel.permissionStatus == .authorizedWhenInUse
            && viewModel.everDenied) ||
        (neededPermissionsLevel == .locationAlways
            && viewModel.alwaysPopupDidNotShow)
    }

    private var permissionType: String {
        neededPermissionsLevel == .locationAlways ? PermissionType.locationAlways.rawValue : PermissionType.locationWhileUsing.rawValue
    }

    private var variant: String {
        coordinator.determineLocationVariant(forAlways: neededPermissionsLevel == .locationAlways)
    }

    private var scope: String {
        neededPermissionsLevel == .locationAlways ? "always" : "while_using"
    }
    
    var body: some View {
        VStack(spacing: 20) {
            if permissionLevelReached {
                EmptyView()
            } else if viewModel.permissionStatus == .notDetermined {
                // First, always request "when in use"
                LocationPermissionWhenInUseView(requestAction: {
                    // Track allow button click
                    PermissionsFlowAnalytics.trackClick(
                        "allow",
                        permission: permissionType,
                        variant: variant,
                        flowName: coordinator.getFlowName()
                    )
                    viewModel.requestLocationWhenInUsePermission()
                })
            } else if neededPermissionsLevel == .locationAlways
                      && viewModel.permissionStatus == .authorizedWhenInUse
                      && !viewModel.everAskedAlways
                      && !viewModel.everDenied
                      && !viewModel.alwaysPopupDidNotShow {
                // Next, request "always" only after in-use granted, and only if location was never denied
                LocationPermissionAlwaysView(requestAction: {
                    // Track allow button click
                    PermissionsFlowAnalytics.trackClick(
                        "allow",
                        permission: permissionType,
                        variant: variant,
                        flowName: coordinator.getFlowName()
                    )
                    viewModel.requestLocationAlwaysPermission()
                })
            } else if shouldShowSettingsButton {
                // If downgraded from always to while in use or to denied case show red alert )
                if viewModel.everHadAlways && neededPermissionsLevel == .locationAlways {
                    LocationPermissionSettingsDowngradedView(
                        backgroundNeeded: neededPermissionsLevel == .locationAlways && (viewModel.everAskedAlways || viewModel.permissionStatus == .authorizedWhenInUse),
                        settingsAction: {
                            // Track go to settings click
                            PermissionsFlowAnalytics.trackClick(
                                "go_to_settings",
                                permission: permissionType,
                                variant: variant,
                                flowName: coordinator.getFlowName()
                            )
                            guard let url = URL(string: UIApplication.openSettingsURLString) else { return }
                            UIApplication.shared.open(url)
                        },
                        onDataUsageClick: {
                            // Track how is my data used click
                            PermissionsFlowAnalytics.trackClick(
                                "how_is_my_data_used",
                                permission: permissionType,
                                variant: variant,
                                flowName: coordinator.getFlowName()
                            )
                        }
                    )
                } else {
                    //Else if rejected simply and never had, regular go to settings
                    LocationPermissionSettingsView(
                        backgroundNeeded: neededPermissionsLevel == .locationAlways && (viewModel.everAskedAlways || viewModel.permissionStatus == .authorizedWhenInUse),
                        settingsAction: {
                            // Track go to settings click
                            PermissionsFlowAnalytics.trackClick(
                                "go_to_settings",
                                permission: permissionType,
                                variant: variant,
                                flowName: coordinator.getFlowName()
                            )
                            guard let url = URL(string: UIApplication.openSettingsURLString) else { return }
                            UIApplication.shared.open(url)
                        },
                        onDataUsageClick: {
                            // Track how is my data used click
                            PermissionsFlowAnalytics.trackClick(
                                "how_is_my_data_used",
                                permission: permissionType,
                                variant: variant,
                                flowName: coordinator.getFlowName()
                            )
                        }
                    )
                }
            }
        }
        .id((viewModel.permissionStatus))
        .onAppear {
            // Track screen view
            PermissionsFlowAnalytics.trackScreen(
                "permission_request",
                permission: permissionType,
                variant: variant,
                flowName: coordinator.getFlowName()
            )

            // Set up the denial callback
            viewModel.onPermissionDenied = {
                coordinator.checkForCriticalPermissionDenials()
                coordinator.nextScreen(currentScreen: .locationScreen(maxMode: neededPermissionsLevel))
            }

            if permissionLevelReached {
                coordinator.nextScreen(currentScreen: .locationScreen(maxMode: neededPermissionsLevel))
            }
        }
        .onChange(of: viewModel.permissionStatus) { newStatus in
            // Track permission status change
            let status: String?
            switch newStatus {
            case .authorizedAlways, .authorizedWhenInUse:
                status = "granted"
            case .denied, .restricted:
                status = "denied"
            default:
                status = nil
            }

            if let status = status {
                PermissionsFlowAnalytics.trackPermissionStatus(
                    "location",
                    status: status,
                    scope: scope,
                    flowName: coordinator.getFlowName()
                )
            }

            // Always navigate if permission level is reached, regardless of polling state
            if permissionLevelReached {
                coordinator.nextScreen(currentScreen: .locationScreen(maxMode: neededPermissionsLevel))
            }
        }
        .onReceive(NotificationCenter.default.publisher(for: UIApplication.didBecomeActiveNotification)) { _ in
            viewModel.handleReturnedFromAlwaysPrompt()
        }
        .onReceive(NotificationCenter.default.publisher(for: UIApplication.willResignActiveNotification)) { _ in
            // If we're detecting popup and app resigns active, it means the popup showed
            if viewModel.isDetectingPopup {
                print("App will resign active - Location Always popup appeared")
                viewModel.isDetectingPopup = false
            }
        }
    }
}

struct LocationPermissionWhenInUseView: View {
    let requestAction: () -> Void

    var body: some View {
        PermissionBaseView(
            icon: .permLocation,
            title: LocalizedStrings.locationAccessRequired,
            primaryButtonTitle: LocalizedStrings.allow,
            primaryAction: { requestAction() },
            content: {
                VStack {
                    Text(LocalizedStrings.locationPermissionsDescription)
                        .padding(.top, 16)
                    Text(LocalizedStrings.allowWhileUsingAppSingleQuotes)
                        .bold()
                }
                .font(.appFont(size: 18, type: .regular))
                .frame(maxWidth: .infinity, alignment: .center)
                .multilineTextAlignment(.center)
                .foregroundStyle(Color.neutralP2)
            },
            dataUsageButtonTitle: LocalizedStrings.dataUsage,
            dataUsageContent: {
                Text(LocalizedStrings.dataUsageDescriptionLocation)
                    .font(.appFont(size: 16, type: .regular))
                    .foregroundStyle(Color.neutralP3)
                    .multilineTextAlignment(.center)
                    .fixedSize(horizontal: false, vertical: true)
            }
        )
    }
}

struct LocationPermissionAlwaysView: View {
    let requestAction: () -> Void

    var body: some View {
        PermissionBaseView(
            icon: .permLocation,
            title: LocalizedStrings.getBetterAssessments,
            primaryButtonTitle: LocalizedStrings.allow,
            primaryAction: { requestAction() },
            content: {
                VStack{
                    Text(LocalizedStrings.locationPermissionsDescriptionBackground)
                        .padding(.top, 5)
                        .padding(.bottom, 20)
                    Text(LocalizedStrings.whenPromptedSelect)
                    Text(LocalizedStrings.changeToAlwaysAllowSingleQuotes)
                        .bold()
                }
                .frame(maxWidth: .infinity, alignment: .center)
                .multilineTextAlignment(.center)
                .foregroundStyle(Color.neutralP2)
            },
            dataUsageButtonTitle: LocalizedStrings.dataUsage,
            dataUsageContent: {
                Text(LocalizedStrings.dataUsageDescriptionLocation)
                    .font(.appFont(size: 16, type: .regular))
                    .foregroundStyle(Color.neutralP3)
                    .multilineTextAlignment(.center)
                    .fixedSize(horizontal: false, vertical: true)
            }
        )
    }
}

struct LocationPermissionSettingsView: View {
    let backgroundNeeded: Bool
    let settingsAction: () -> Void
    let onDataUsageClick: (() -> Void)?

    var body: some View {
        PermissionBaseView(
            icon: .permLocation,
            title: backgroundNeeded ? LocalizedStrings.getBetterAssessments : LocalizedStrings.locationAccessRequired,
            primaryButtonTitle: LocalizedStrings.goToSettings,
            primaryAction: {
                guard let url = URL(string: UIApplication.openSettingsURLString) else { return }
                UIApplication.shared.open(url)
            },
            content: {
                VStack{
                    Text(backgroundNeeded ? LocalizedStrings.locationPermissionsDescriptionBackground : LocalizedStrings.locationPermissionsDescriptionForeground)
                        .padding(.top, 5)
                        .padding(.bottom, 16)
                        .fixedSize(horizontal: false, vertical: true)
                    Text("\(LocalizedStrings.goToDeviceSettingsAndThen)")
                        .fixedSize(horizontal: false, vertical: true)

                    HStack{
                        Image(.locationSymbol)
                            .resizable()
                            .frame(width: 24, height: 24)
                        Text("\(LocalizedStrings.selectLocation)")
                    }
                    HStack{
                        Image(.vSign)
                            .resizable()
                            .frame(width: 24, height: 24)
                        Text(backgroundNeeded ? LocalizedStrings.tapAlways : LocalizedStrings.tapWhileUsing)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .center)
                .multilineTextAlignment(.center)
                .foregroundStyle(Color.neutralP2)
            },
            dataUsageButtonTitle: LocalizedStrings.dataUsage,
            dataUsageContent: {
                Text(LocalizedStrings.dataUsageDescriptionLocation)
                    .font(.appFont(size: 16, type: .regular))
                    .foregroundStyle(Color.neutralP3)
                    .multilineTextAlignment(.center)
                    .fixedSize(horizontal: false, vertical: true)
            },
            onDataUsageClick: onDataUsageClick
        )
    }
}

struct LocationPermissionSettingsDowngradedView: View {
    let backgroundNeeded: Bool
    let settingsAction: () -> Void
    let onDataUsageClick: (() -> Void)?

    var body: some View {
        PermissionBaseView(
            icon: .locationAlwaysRedAlert,
            title: LocalizedStrings.locationAccessLimited,
            primaryButtonTitle: LocalizedStrings.goToSettings,
            primaryAction: {
                guard let url = URL(string: UIApplication.openSettingsURLString) else { return }
                UIApplication.shared.open(url)
            },
            content: {
                VStack{
                    Text(LocalizedStrings.locationPermissionsDescriptionTurnOn)
                        .padding(.top, 5)
                        .padding(.bottom, 16)
                    Text("\(LocalizedStrings.goToDeviceSettingsAndThen)")

                    HStack{
                        Image(.locationSymbol)
                            .resizable()
                            .frame(width: 24, height: 24)
                        Text("\(LocalizedStrings.selectLocation)")
                    }
                    HStack{
                        Image(.vSign)
                            .resizable()
                            .frame(width: 24, height: 24)
                        Text("\(backgroundNeeded ? LocalizedStrings.tapAlways : LocalizedStrings.tapWhileUsing)")
                    }
                }
                .frame(maxWidth: .infinity, alignment: .center)
                .multilineTextAlignment(.center)
                .foregroundStyle(Color.neutralP2)
            },
            dataUsageButtonTitle: LocalizedStrings.dataUsage,
            dataUsageContent: {
                Text(LocalizedStrings.dataUsageDescriptionLocation)
                    .font(.appFont(size: 16, type: .regular))
                    .foregroundStyle(Color.neutralP3)
                    .multilineTextAlignment(.center)
                    .fixedSize(horizontal: false, vertical: true)
            },
            onDataUsageClick: onDataUsageClick
        )
    }
}

struct SDKUIKitUserDefaultsKeys {
    static let hadAskedForLocationAlwaysOnce = "hadAskedForLocationAlwaysOnce"
    static let hadLocationAlwaysOnce = "hadLocationAlwaysOnce"
    static let hadLocationDeniedOnce = "hadLocationDeniedOnce"
}

#if DEBUG
struct LocationPermissionsView_Previews: PreviewProvider {
    static var previews: some View {
        // example preview for “always” mode
        LocationPermissionsView(neededPermissionsLevel: .locationAlways)
            .environmentObject(PermissionsFlowCoordinator(mode: .background))

        LocationPermissionSettingsView(backgroundNeeded: false, settingsAction: {}, onDataUsageClick: {})

        LocationPermissionSettingsDowngradedView(backgroundNeeded: true, settingsAction: {}, onDataUsageClick: {})
    }
}
#endif
