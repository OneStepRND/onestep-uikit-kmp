//
//  PermissionsFlow.swift
//
//
//  Created by Maor Duani on 11/09/2024.
//

import SwiftUI

enum PermissionFlowMode: String {
    case inApp
    case background
    case healthKit
    case full
}

struct OSTPermissionsFlow: View {
    @Environment(\.dismiss) private var dismiss
    @StateObject private var coordinator: PermissionsFlowCoordinator
    @Environment(\.colorScheme) var colorScheme

    init(mode: PermissionFlowMode = .inApp) {
        _coordinator = StateObject(wrappedValue: PermissionsFlowCoordinator(mode: mode))
    }
    
    var body: some View {
        NavigationStack {
            if let currentScreen = coordinator.currentScreen {
                view(for: currentScreen)
                    .customNavigationBarStyle(onClose: {
                        // Track close button click
                        coordinator.trackCloseButtonClick(on: currentScreen)

                        // Check if required permissions are still missing when user taps close
                        Task {
                            let permissionsMissing = await coordinator.areRequiredPermissionsMissing()
                            if permissionsMissing {
                                // Post notification that permissions flow was dismissed without granting required permissions
                                NotificationCenter.default.post(
                                    name: NSNotification.Name("PermissionsFlowDismissedWithoutPermissions"),
                                    object: nil
                                )
                            }
                        }
                        dismiss()
                    })
            } else {
                EmptyView()
            }
        }
        .accentColor(.neutralP3)
        .navigationBarBackButtonHidden()
        .environmentObject(coordinator)
        .onChange(of: coordinator.flowCompleted) { _ in
            if coordinator.flowCompleted {
                dismiss()
            }
        }
        .onChange(of: coordinator.criticalPermissionsDenied) { denied in
            if denied {
                // Post notification that critical permissions were denied
                NotificationCenter.default.post(
                    name: NSNotification.Name("CriticalPermissionsDenied"),
                    object: nil
                )
                dismiss()
            }
        }
        .onAppear {
            //for the case when the user has all the permissions already
            //and when coordinator is initialized it decides that the flow is completed and sets the flowCompleted to true before the view loaded yet
            if coordinator.flowCompleted {
                dismiss()
            }
        }
        .preferredColorScheme(.light)
        .dynamicTypeSize(...DynamicTypeSize.xxLarge)
    }
    
    static func haveAllPermissionsAlready() async -> Bool {
        return await PermissionsValidator.allPermissionsGranted()
    }
}

extension OSTPermissionsFlow {
    @ViewBuilder
    private func view(for screen: PermissionFlowScreens) -> some View {
        switch screen {
        case .locationScreen(maxMode: .locationAlways):
            LocationPermissionsView(neededPermissionsLevel: .locationAlways)
        case .locationScreen(maxMode: .locationWhileInUse):
            LocationPermissionsView(neededPermissionsLevel: .locationWhileInUse)
        case .motionAndFitnessScreen:
            MotionAndFitnessPermissionsView()
        case .healthKitScreen:
            HealthKitPermissionsView()
        case .permissionsRationalization:
            PermissionsRationalizationScreen()
        default:
            EmptyView()
        }
    }
}


#if DEBUG
#Preview {
    OSTPermissionsFlow()
}
#endif
