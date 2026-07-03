//
//  SwiftUIView.swift
//  OneStepUIKit
//
//  Created by David Havkin on 24/04/2025.
//

import SwiftUI
import AVFoundation

struct MicPermissionsView: View {
    @State private var micStatus: AVAuthorizationStatus = AVCaptureDevice.authorizationStatus(for: .audio)
    @Environment(\.oneStepTheme) var theme
    @Environment(\.openURL) var openURL
    var onContinue: (() -> Void)?
    
    
    var body: some View {
        VStack(spacing: 0) {
            PermissionBaseView(
                icon: .permMic,
                title: LocalizedStrings.microphoneAccessTitle,
                primaryButtonTitle: "",
                primaryAction: {},
                content: {
                    VStack{
                        if micStatus == .notDetermined {
                            (Text(LocalizedStrings.whenPromptedSelect) +
                             Text(" ") +
                             Text(LocalizedStrings.allowQuotes).bold())
                            .padding(.top, 5)
                        } else if micStatus != .authorized {
                            Text(LocalizedStrings.microphoneAccessSettingsDescription)
                                .padding(.top, 5)
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .center)
                    .multilineTextAlignment(.center)
                    .foregroundStyle(Color.neutralP2)
                    .font(.appFont(size: 18, type: .regular))
                },
                dataUsageButtonTitle: nil,
                dataUsageContent: {}
            )

            Spacer()

            getTheButtons()
                .padding(.horizontal, 16)
                .padding(.bottom, 10)
        }
        .onChange(of: micStatus) { newStatus in
            onContinue?()
        }
        .onAppear {
            micStatus = AVCaptureDevice.authorizationStatus(for: .audio)
            if micStatus == .authorized {
                onContinue?()
            }
        }
    }
    
    func getTheButtons() -> some View {
        VStack {
            if micStatus == .notDetermined {
                regularButton(title: LocalizedStrings.allow)
                    .onTapGesture {
                        PermissionsValidator.requestMicPermission { _ in
                            micStatus = AVCaptureDevice.authorizationStatus(for: .audio)
                        }
                    }
                skipButton
                    .onTapGesture {
                        onContinue?()
                    }
            } else if micStatus != .authorized {
                regularButton(title: LocalizedStrings.goToSettings)
                    .onTapGesture {
                        if let url = URL(string: UIApplication.openSettingsURLString) {
                            openURL(url)
                        }
                    }
                skipButton
                    .onTapGesture {
                        onContinue?()
                    }
            }
        }
    }
        
    
    func regularButton(title: String) -> some View {
        RoundedRectangle(cornerRadius: 4)
            .fill(theme.primaryColor)
            .frame(maxWidth: .infinity)
            .overlay(alignment: .center) {
                VStack(spacing: 0) {
                    Text(title)
                        .multilineTextAlignment(.center)
                }
                .font(.appFont(size: 20, type: .bold))
                .foregroundStyle(Color.white)
            }
            .frame(height: 60)
    }
    
    var skipButton: some View {
        Text(LocalizedStrings.skip)
            .fixedSize(horizontal: true, vertical: false)
            .font(.appFont(size: 18, type: .bold))
            .frame(maxWidth: .infinity, maxHeight: 64)
            .frame(height: 60)
            .foregroundStyle(Color.neutralP3)
    }
        
}

#if DEBUG
#Preview {
    MicPermissionsView()
}
#endif
