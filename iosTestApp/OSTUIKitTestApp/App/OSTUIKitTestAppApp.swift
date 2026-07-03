import SwiftUI
import OSTUIKitKMP
import OneStepSDK

@main
struct OSTUIKitTestAppApp: App {
    @StateObject private var appState = AppState()

    var body: some Scene {
        WindowGroup {
            Group {
                if appState.isSDKReady {
                    ContentView()
                        .environmentObject(appState)
                        .toolbar {
                            ToolbarItem(placement: .navigationBarTrailing) {
                                Button {
                                    appState.logout()
                                } label: {
                                    Image(systemName: "gearshape")
                                }
                            }
                        }
                } else if appState.isInitializing {
                    VStack(spacing: 16) {
                        ProgressView()
                            .scaleEffect(1.5)
                        Text("Initializing SDK...")
                            .font(.headline)
                        if let error = appState.initError {
                            Text(error)
                                .font(.caption)
                                .foregroundStyle(.red)
                                .multilineTextAlignment(.center)
                                .padding(.horizontal)
                        }
                    }
                } else {
                    SettingsView(appState: appState)
                }
            }
        }
    }
}

// MARK: - App State

class AppState: ObservableObject {
    @Published var isSDKReady = false
    @Published var isInitializing = false
    @Published var initError: String?

    var hasCredentials: Bool {
        let orgName = UserDefaults.standard.string(forKey: "sdk_orgName") ?? ""
        let distinctId = UserDefaults.standard.string(forKey: "sdk_distinctId") ?? ""
        return !orgName.isEmpty && !distinctId.isEmpty && Organizations.find(byName: orgName) != nil
    }

    init() {
        NetworkLogger.startLogging()
        if hasCredentials {
            initializeSDK()
        }
    }

    func initializeSDK() {
        guard !isInitializing else { return }

        let orgName = UserDefaults.standard.string(forKey: "sdk_orgName") ?? ""
        let distinctId = UserDefaults.standard.string(forKey: "sdk_distinctId") ?? ""

        guard let org = Organizations.find(byName: orgName), !distinctId.isEmpty else { return }

        isInitializing = true
        initError = nil

        Task { @MainActor [weak self] in
            let initResult = OneStep.initialize(onAuthLost: { error in
                print("[TestApp] OneStep auth lost: \(error)")
            })
            guard case .success = initResult, case .success(let onestep) = OneStep.shared() else {
                self?.initError = "SDK initialization failed"
                self?.isInitializing = false
                return
            }

            let patientResult = await onestep.setPatient(
                apiKey: org.apiKey,
                customerPatientId: distinctId,
                identityVerification: org.signIdentity(distinctId: distinctId)
            )

            guard let self else { return }
            switch patientResult {
            case .success:
                // Wire the uikit-kmp framework to the native SDK + register the native permission flow.
                configureOSTUIKitKMPWithNativeSDK()
                self.isSDKReady = true
                self.isInitializing = false
                print("[TestApp] SDK initialized and patient identified")
            case .failure(let error):
                self.initError = "setPatient failed: \(error)"
                self.isInitializing = false
                print("[TestApp] setPatient failed: \(error)")
            }
        }
    }

    func logout() {
        isSDKReady = false
        isInitializing = false
        initError = nil
    }
}
