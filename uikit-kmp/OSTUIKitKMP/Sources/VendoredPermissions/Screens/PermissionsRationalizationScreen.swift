//
//  PermissionsRationalizationScreen.swift
//  OneStepUIKit
//
//  Created by David Havkin on 30/01/2025.
//

import SwiftUI

struct PermissionsRationalizationScreen: View {
    @EnvironmentObject var coordinator: PermissionsFlowCoordinator
    @Environment(\.oneStepTheme) var theme
    
    // pull the app’s name from Info.plist (CFBundleDisplayName or CFBundleName)
    private var appName: String {
        (Bundle.main.object(forInfoDictionaryKey: "CFBundleDisplayName") as? String)
        ?? (Bundle.main.object(forInfoDictionaryKey: "CFBundleName") as? String)
        ?? LocalizedStrings.thisApp
    }
    
    var body: some View {
        GeometryReader { geometry in
            VStack() {
                ScrollView {
                    VStack(alignment: .center) {
                        Image(.dataSafe)
                            .frame(width: 105, height: 105)
                            .padding(.top, 24)
                        Text(LocalizedStrings.permYourDataIsSafeWithUs)
                            .font(.appFont(size: 28, type: .bold))
                            .padding(.top, 37)
                            .foregroundStyle(Color.primaryP3)
                            .multilineTextAlignment(.center)
                            .padding(.bottom, 2)
                        Text(LocalizedStrings.pleaseAllow + " \(appName) " + LocalizedStrings.toAnalyzeMovement)
                            .padding(.bottom, 20)
                        Text(LocalizedStrings.yourDataIsSecurelyStored)
                            
                        Spacer()
                    }
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(.horizontal, 16)
                    .multilineTextAlignment(.center)
                }
                .font(.appFont(size: 18, type: .regular))
                .foregroundStyle(Color.neutralP2)
                
                Button(LocalizedStrings.continueText) {
                    coordinator.nextScreen(currentScreen: .permissionsRationalization)
                }
                .buttonStyle(.onestep.fill.big)
                .padding(.horizontal, 16)
            }
        }
        .onAppear {
            // Mark that this screen has been shown
            UserDefaults.standard.set(true, forKey: UserDefaultsKeys.hasShownPermissionsRationalization)

            // Track screen view
            PermissionsFlowAnalytics.trackScreen(
                "data_safety",
                flowName: coordinator.getFlowName()
            )
        }
    }
}

#if DEBUG
#Preview {
    PermissionsRationalizationScreen()
        .oneStepTheme(.default)
}
#endif
