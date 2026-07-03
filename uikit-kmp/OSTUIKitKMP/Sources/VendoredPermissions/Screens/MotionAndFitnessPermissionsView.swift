//
//  MotionAndFitnessPermissionsView.swift
//  OneStepUIKit
//
//  Created by David Havkin on 05/05/2025.
//


import SwiftUI
import CoreMotion
import UIKit
import Combine

struct PermissionBaseView<Content: View, SheetContent: View>: View {
    let icon: ImageResource
    let title: String
    let primaryButtonTitle: String
    let primaryAction: () -> Void
    @ViewBuilder let content: () -> Content

    // Optional "How is my data used?" support
    let dataUsageButtonTitle: String?
    @ViewBuilder let dataUsageContent: () -> SheetContent
    let onDataUsageClick: (() -> Void)?

    @State private var showDataUsageSheet = false

    init(
        icon: ImageResource,
        title: String,
        primaryButtonTitle: String,
        primaryAction: @escaping () -> Void,
        @ViewBuilder content: @escaping () -> Content,
        dataUsageButtonTitle: String? = nil,
        @ViewBuilder dataUsageContent: @escaping () -> SheetContent = { EmptyView() },
        onDataUsageClick: (() -> Void)? = nil
    ) {
        self.icon = icon
        self.title = title
        self.primaryButtonTitle = primaryButtonTitle
        self.primaryAction = primaryAction
        self.content = content
        self.dataUsageButtonTitle = dataUsageButtonTitle
        self.dataUsageContent = dataUsageContent
        self.onDataUsageClick = onDataUsageClick
    }

    var body: some View {
        VStack(spacing: 0) {
            ScrollView{
                VStack(spacing: 16) {
                    Image(icon)
                        .frame(width: 105, height: 105)

                    Text(title)
                        .font(.appFont(size: 28, type: .bold))
                        .padding(.top, 37)
                        .foregroundStyle(Color.primaryP3)
                        .multilineTextAlignment(.center)
                        .fixedSize(horizontal: false, vertical: true)

                    // Custom per-screen content
                    content()
                        .font(.appFont(size: 18, type: .regular))
                }
            }

            Spacer()

            if !primaryButtonTitle.isEmpty {
                Button(primaryButtonTitle, action: primaryAction)
                    .buttonStyle(.onestep.fill.big)
            }

            if let dataUsageButtonTitle {
                Button(action: {
                    onDataUsageClick?()
                    showDataUsageSheet = true
                }, label: {
                    Text(dataUsageButtonTitle)
                        .foregroundStyle(Color.neutralP3)
                        .font(.appFont(size: 18, type: .bold))
                        .padding()
                })
                .sheet(isPresented: $showDataUsageSheet) {
                    PopOverResizableViewContainer {
                        VStack{
                            Text(LocalizedStrings.permYourDataIsSafeWithUs)
                                .font(.appFont(size: 24, type: .bold))
                                .multilineTextAlignment(.center)
                                .padding(.top, 20)
                            dataUsageContent()
                        }
                        .foregroundStyle(Color.neutralP3)
                    }
                }
            }
        }
        .padding(.horizontal, 16)
        .padding(.top, 24)
    }
}

struct MotionAndFitnessPermissionsView: View {
    @State private var showSettingsView = false
    @State private var shouldPoll = true
    @EnvironmentObject var coordinator: PermissionsFlowCoordinator
    @Environment(\.openURL) var openURL
    @Environment(\.scenePhase) var scenePhase
    @State private var timerPublisher = Timer.publish(every: 0.5, on: .main, in: .common).autoconnect()
    @State private var showDataUsageSheet = false

    private var permissionType: String {
        PermissionType.motionFitness.rawValue
    }

    private var variant: String {
        coordinator.determineMotionVariant()
    }

    @ViewBuilder
    private var initialRequestView: some View {
        PermissionBaseView(
            icon: .permMotionAndFitness,
            title: coordinator.mode == .background ? LocalizedStrings.getDeeperInsights : LocalizedStrings.motionAndFitnessActivityAccessRequired,
            primaryButtonTitle: LocalizedStrings.allow,
            primaryAction: {
                // Track allow button click
                PermissionsFlowAnalytics.trackClick(
                    "allow",
                    permission: permissionType,
                    variant: variant,
                    flowName: coordinator.getFlowName()
                )
                coordinator.requestMotionAndFitnessPermission()
            },
            content: {
                if coordinator.mode == .background {
                    VStack{
                        Text(LocalizedStrings.pleaseAlloMotionAndFitnessPermissionsDescription)
                            .multilineTextAlignment(.center)
                            .font(.appFont(size: 18, type: .regular))
                            .foregroundStyle(Color.neutralP2)
                            .padding(.top, 5)
                            .padding(.horizontal, 16)
                    }
                }
            },
            dataUsageButtonTitle: LocalizedStrings.dataUsage,
            dataUsageContent: {
                Text(LocalizedStrings.dataUsageDescriptionMF)
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
    private var settingsRequestView: some View {
        PermissionBaseView(
            icon: .permMotionAndFitness,
            title: coordinator.mode == .background ? LocalizedStrings.getDeeperInsights : LocalizedStrings.motionAndFitnessActivityAccessRequired,
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
                    if coordinator.mode == .background {
                        Text(LocalizedStrings.pleaseAlloMotionAndFitnessPermissionsDescription)
                            .multilineTextAlignment(.center)
                            .font(.appFont(size: 18, type: .regular))
                            .foregroundStyle(Color.neutralP2)
                            .padding(.top, 5)
                    }
                    Text(LocalizedStrings.goToDeviceSettingsAndThenToggleOn)
                        .padding(.top, 5)
                    HStack{
                        Image(.motionFitnessIcon)
                            .resizable()
                            .frame(width: 24, height: 24)
                        Text(LocalizedStrings.motionAndFitness)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .center)
                .multilineTextAlignment(.center)
                .foregroundStyle(Color.neutralP2)
                .font(.appFont(size: 18, type: .regular))
                .padding(.horizontal, 16)
                .padding(.top, 5)
            },
            dataUsageButtonTitle: LocalizedStrings.dataUsage,
            dataUsageContent: {
                Text(LocalizedStrings.dataUsageDescriptionMF)
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
                settingsRequestView
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

            let status = CMMotionActivityManager.authorizationStatus()
            if status == .authorized {
                coordinator.nextScreen(currentScreen: .motionAndFitnessScreen)
            } else if status == .denied || status == .restricted {
                // Permission was already asked and denied/restricted - show settings view
                showSettingsView = true
                shouldPoll = false // Don't poll when showing settings view
            }
            // If status == .notDetermined, show initial view (default) and keep polling
        }
        .onReceive(timerPublisher) { _ in
            guard shouldPoll else { return }

            let status = CMMotionActivityManager.authorizationStatus()
            switch status {
            case .authorized:
                // Track permission status
                PermissionsFlowAnalytics.trackPermissionStatus(
                    "physical_activity",
                    status: "granted",
                    flowName: coordinator.getFlowName()
                )

                // Stop polling and navigate
                shouldPoll = false
                timerPublisher.upstream.connect().cancel()
                coordinator.nextScreen(currentScreen: .motionAndFitnessScreen)
            case  .restricted:
                // Track permission status
                PermissionsFlowAnalytics.trackPermissionStatus(
                    "physical_activity",
                    status: "restricted",
                    flowName: coordinator.getFlowName()
                )

                // Stop polling and proceed to next screen (don't switch UI)
                shouldPoll = false
                timerPublisher.upstream.connect().cancel()
                coordinator.checkForCriticalPermissionDenials()
                coordinator.nextScreen(currentScreen: .motionAndFitnessScreen)
            case .denied:
                // Track permission status
                PermissionsFlowAnalytics.trackPermissionStatus(
                    "physical_activity",
                    status: "denied",
                    flowName: coordinator.getFlowName()
                )

                // Stop polling and proceed to next screen (don't switch UI)
                shouldPoll = false
                timerPublisher.upstream.connect().cancel()
                coordinator.checkForCriticalPermissionDenials()
                coordinator.nextScreen(currentScreen: .motionAndFitnessScreen)
            default:
                // .notDetermined, keep polling
                break
            }
        }
        .onChange(of: scenePhase) { newPhase in
            if CMMotionActivityManager.authorizationStatus() == .authorized {
                coordinator.nextScreen(currentScreen: .motionAndFitnessScreen)
            }
        }
    }
}

#if DEBUG
struct MotionAndFitnessPermissionsView_Previews: PreviewProvider {
    static var previews: some View {
        MotionAndFitnessPermissionsView()
            .environmentObject(PermissionsFlowCoordinator(mode: .inApp))

    }
}
#endif
