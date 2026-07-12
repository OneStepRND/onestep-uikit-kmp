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
    /// The clinician web-login session (JWT), if the user entered via clinician mode.
    /// Independent of SDK patient identification; JWT is never logged (HIPAA).
    @Published private(set) var clinicianSession: ClinicianWebLogin.Session?

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

    /// Enter the app via a clinician web-login session. Unlike `initializeSDK()`, this does NOT
    /// call `setPatient` — clinician mode keeps the SDK identification `.unidentified` (see
    /// docs/patient-scope-clinician-mode-design.md). We still `initialize()` the SDK and wire the
    /// KMP framework so `OneStep.shared()`-backed bridges on the main screen resolve; per-flow
    /// patient scoping is threaded via a `patientId` argument (that wiring lands separately).
    func completeClinicianLogin(_ session: ClinicianWebLogin.Session) {
        clinicianSession = session
        // Clinician mode operates on the avatar patient: identify the SDK as the avatar so a real,
        // authenticated MotionLab backs the flows. (The web-login JWT is not an SDK session, and
        // OneStep.withPatient needs a clinician session we don't have in this harness.) Reuse the
        // standard identify path (setPatient) by pointing the stored distinct id at the avatar.
        UserDefaults.standard.set(AppConstants.avatarAangDistinctId, forKey: "sdk_distinctId")
        if (UserDefaults.standard.string(forKey: "sdk_orgName") ?? "").isEmpty {
            UserDefaults.standard.set(Organizations.default.name, forKey: "sdk_orgName")
        }
        // No PII/PHI: the JWT and clinician identity are never logged (HIPAA).
        NSLog("[TestApp] Clinician session established; entering app as avatar patient")
        initializeSDK()
    }

    func logout() {
        isSDKReady = false
        isInitializing = false
        initError = nil
        clinicianSession = nil
    }
}
