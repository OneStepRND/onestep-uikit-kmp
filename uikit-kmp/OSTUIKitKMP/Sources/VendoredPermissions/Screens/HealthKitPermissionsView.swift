//
//  HealthKitPermissionsView.swift
//  OneStepUIKit
//
//  Created by David Havkin on 05/05/2025.
//

import SwiftUI
import UIKit

struct HealthKitPermissionsView: View {
    @State private var showSettingsView = false
    @EnvironmentObject var coordinator: PermissionsFlowCoordinator
    @Environment(\.openURL) var openURL
    @Environment(\.scenePhase) var scenePhase

    // pull the app's name from Info.plist (CFBundleDisplayName or CFBundleName)
    private var appName: String {
        (Bundle.main.object(forInfoDictionaryKey: "CFBundleDisplayName") as? String)
        ?? (Bundle.main.object(forInfoDictionaryKey: "CFBundleName") as? String)
        ?? LocalizedStrings.thisApp
    }

    private var permissionType: String {
        PermissionType.healthkit.rawValue
    }

    private var variant: String {
        coordinator.determineHealthKitVariant()
    }

    @ViewBuilder
    private var initialRequestView: some View {
        PermissionBaseView(
            icon: .permHealthkit,
            title: LocalizedStrings.trackYourStepCount,
            primaryButtonTitle: LocalizedStrings.allow,
            primaryAction: {
                // Track allow button click
                PermissionsFlowAnalytics.trackClick(
                    "allow",
                    permission: permissionType,
                    variant: variant,
                    flowName: coordinator.getFlowName()
                )

                Task {
                    // Ask for HealthKit authorization the first time
                    _ = await PermissionsValidator.askForHealthKitAuthorizationFirstTime()

                    // Check the result and track status
                    let granted = await PermissionsValidator.checkIfAllHealthKitPermissionsGranted()
                    PermissionsFlowAnalytics.trackPermissionStatus(
                        "healthkit",
                        status: granted ? "granted" : "denied",
                        flowName: coordinator.getFlowName()
                    )

                    // After iOS popup closes, go to next screen regardless of outcome
                    DispatchQueue.main.async {
                        coordinator.nextScreen(currentScreen: .healthKitScreen)
                    }
                }
            },
            content: {
                VStack{
                    Text(LocalizedStrings.healthKitAccessDescription)
                        .padding(.top, 5)
                    Text(LocalizedStrings.whenPromptedSelect)
                    
                    Text(LocalizedStrings.turnOnAll)
                        .bold() +
                    Text(" and ") +
                    Text(LocalizedStrings.allowHealthKit)
                        .bold() +
                    Text(".")
                }
                .frame(maxWidth: .infinity, alignment: .center)
                .multilineTextAlignment(.center)
                .foregroundStyle(Color.neutralP2)
                .font(.appFont(size: 18, type: .regular))
            },
            dataUsageButtonTitle: LocalizedStrings.dataUsage,
            dataUsageContent: {
                Text(LocalizedStrings.dataUsageDescriptionHealthKit)
                    .font(.appFont(size: 16, type: .regular))
                    .foregroundStyle(Color.neutralP3)
                    .multilineTextAlignment(.center)
                    .fixedSize(horizontal: false, vertical: true)
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

    @ViewBuilder
    private var settingsPermissionView: some View {
        PermissionBaseView(
            icon: .permHealthkit,
            title: LocalizedStrings.trackYourStepCount,
            primaryButtonTitle: LocalizedStrings.goToSettings,
            primaryAction: {
                // Track go to settings click
                PermissionsFlowAnalytics.trackClick(
                    "go_to_settings",
                    permission: permissionType,
                    variant: variant,
                    flowName: coordinator.getFlowName()
                )

                if let url = URL(string: UIApplication.openSettingsURLString) {
                    openURL(url)
                }
            },
            content: {
                VStack{
                    Text(LocalizedStrings.healthKitAccessDescription)
                        .padding(.top, 5)
                        .padding(.bottom, 5)
                    Text("• \(LocalizedStrings.goToDeviceSettings)")
                    Text("• \(LocalizedStrings.findHealthApp)")
                    Text("• \(LocalizedStrings.goToDataAccessDevices)")
                    Text("• \(LocalizedStrings.choose)") + Text(" ") + Text("\(appName)")
                    Text("• \(LocalizedStrings.tap)") + Text(" ") + Text(LocalizedStrings.turnOnAll).bold()
                        
                }
                .frame(maxWidth: .infinity, alignment: .center)
                .multilineTextAlignment(.center)
                .foregroundStyle(Color.neutralP2)
                .font(.appFont(size: 18, type: .regular))
            },
            dataUsageButtonTitle: LocalizedStrings.dataUsage,
            dataUsageContent: {
                Text(LocalizedStrings.dataUsageDescriptionHealthKit)
                    .font(.appFont(size: 16, type: .regular))
                    .foregroundStyle(Color.neutralP3)
                    .multilineTextAlignment(.center)
                    .fixedSize(horizontal: false, vertical: true)
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

    var body: some View {
        VStack {
            if showSettingsView {
                settingsPermissionView
            } else {
                initialRequestView
            }
        }
        .onAppear {
            // Track screen view
            PermissionsFlowAnalytics.trackScreen(
                "permission_request",
                permission: permissionType,
                variant: variant,
                flowName: coordinator.getFlowName()
            )
        }
        .task {
            // Check whether HealthKit permissions have already been requested
            let haveAsked = await PermissionsValidator.healthKitPermissionsRequested()

            // Check if permissions are already granted - if so, navigate immediately
            let permissionsGranted = await PermissionsValidator.checkIfAllHealthKitPermissionsGranted()
            if permissionsGranted {
                // Track permission status
                PermissionsFlowAnalytics.trackPermissionStatus(
                    "healthkit",
                    status: "granted",
                    flowName: coordinator.getFlowName()
                )
                coordinator.nextScreen(currentScreen: .healthKitScreen)
                return
            }

            // If permissions were already asked before, show settings view
            if haveAsked {
                showSettingsView = true
            }
            // If never asked before, show initial view (default)
        }
        .onChange(of: scenePhase) { phase in
            if phase == .active && showSettingsView {
                // Check permissions when app becomes active (user returns from settings)
                Task {
                    let granted = await PermissionsValidator.checkIfAllHealthKitPermissionsGranted()
                    if granted {
                        // Track permission status
                        PermissionsFlowAnalytics.trackPermissionStatus(
                            "healthkit",
                            status: "granted",
                            flowName: coordinator.getFlowName()
                        )
                        coordinator.nextScreen(currentScreen: .healthKitScreen)
                    } else {
                        // Track permission status as denied if still not granted after settings
                        PermissionsFlowAnalytics.trackPermissionStatus(
                            "healthkit",
                            status: "denied",
                            flowName: coordinator.getFlowName()
                        )
                    }
                }
            }
        }
    }
}

#if DEBUG
struct HealthKitPermissionsView_Previews: PreviewProvider {
    static var previews: some View {
        HealthKitPermissionsView()
            .environmentObject(PermissionsFlowCoordinator(mode: .background))
    }
}
#endif
