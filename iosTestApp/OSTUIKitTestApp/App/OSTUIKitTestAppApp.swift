import SwiftUI
import UIKit
import AuthenticationServices
import OSTUIKit
import OSTUIKitKMP
import OneStepSDK

/// Thin iOS shell around the shared KMP test app (`TestAppRoot` in :testAppShared). All UI and
/// flow logic is shared with Android; this file only wires the native OneStepSDK, the
/// ASWebAuthenticationSession clinician login, and the Compose root view controller.
@main
struct OSTUIKitTestAppApp: App {
    private let shell: IosTestAppShell
    private let delegate: NativeShellDelegate

    init() {
        NetworkLogger.startLogging()
        let delegate = NativeShellDelegate()
        // App.init runs on the main thread; OneStep.initialize is @MainActor-isolated.
        MainActor.assumeIsolated {
            delegate.initializeSDK()
        }
        let shell = IosTestAppShell(delegate: delegate)
        self.delegate = delegate
        self.shell = shell
        delegate.autoLogin(into: shell)
    }

    var body: some Scene {
        WindowGroup {
            TestAppRootView(shell: shell)
                .ignoresSafeArea()
        }
    }
}

/// Hosts the shared Compose UI.
private struct TestAppRootView: UIViewControllerRepresentable {
    let shell: IosTestAppShell

    func makeUIViewController(context: Context) -> UIViewController {
        TestAppViewControllerKt.TestAppViewController(shell: shell)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

// MARK: - Mock recording (unchanged mechanism)

/// Drives the SDK's mock-recording path. When `MockRecordingName` is set in `UserDefaults` and a
/// recording finishes, the SDK substitutes the mock payload at S3 upload time, so the backend
/// analyzes the mock and returns a real, deterministic measurement — even on a stationary device.
private enum MockRecording {
    static let userDefaultsKey = "MockRecordingName"
    static let none = "None"

    /// "None" first: unlike Android's emulator-oriented harness, a real recording is the default
    /// on an iPhone and mocks are opt-in via the Configure Flow picker.
    static let options = [
        none,
        "successWalk", "stsSuccess", "tugSuccess", "dualTaskSuccess", "romSuccess",
        "error_curvy", "error_no_cycle", "error_position", "error_other", "error_short", "error_static",
    ]

    static func apply(_ selection: String) {
        if selection == none {
            UserDefaults.standard.removeObject(forKey: userDefaultsKey)
        } else {
            UserDefaults.standard.set(selection, forKey: userDefaultsKey)
        }
    }

    static func clear() {
        UserDefaults.standard.removeObject(forKey: userDefaultsKey)
    }
}

// MARK: - Native shell delegate

/// Implements the KMP `IosTestAppShellDelegate` against the native OneStepSDK.
private final class NativeShellDelegate: NSObject, IosTestAppShellDelegate {

    private(set) var initialized = false
    private var webLoginSession: ASWebAuthenticationSession?

    var sdkAvailable: Bool { initialized }

    var mockOptions: [String] { MockRecording.options }

    /// Initialize the native SDK and wire the uikit-kmp bridges. Safe to call once at launch:
    /// the bridges resolve `OneStep.shared()` lazily, and patient identification arrives later
    /// via `setPatient`.
    @MainActor
    func initializeSDK() {
        let initResult = initializeOSTUIKitKMPWithNativeSDK(onAuthLost: { error in
            NSLog("[TestApp] OneStep auth lost") // error detail may carry identifiers — not logged (HIPAA)
        })
        guard case .success = initResult else {
            NSLog("[TestApp] OneStep SDK initialization failed")
            initialized = false
            return
        }
        initialized = true
        NSLog("[TestApp] Consolidated KMP shell: SDK initialized and bridges configured")
    }

    /// Re-identify from stored credentials so a relaunch lands on the authenticated home screen
    /// (parity with the previous AppState behavior and with Android's persisted identification).
    func autoLogin(into shell: IosTestAppShell) {
        let defaults = UserDefaults.standard
        let orgName = defaults.string(forKey: "sdk_orgName") ?? ""
        let distinctId = defaults.string(forKey: "sdk_distinctId") ?? ""
        guard !distinctId.isEmpty, let org = Organizations.shared.find(byName: orgName) else { return }
        setPatient(org: org, distinctId: distinctId) { error in
            if error == nil {
                shell.setIdentifiedPatient(patientId: distinctId)
            }
        }
    }

    func setPatient(org: Organization, distinctId: String, completion: @escaping (String?) -> Void) {
        guard initialized, case .success(let onestep) = OneStep.shared() else {
            completion("OneStep SDK not initialized")
            return
        }
        Task { @MainActor in
            let result = await onestep.setPatient(
                apiKey: org.apiKey,
                customerPatientId: distinctId,
                identityVerification: org.signIdentity(distinctId: distinctId)
            )
            switch result {
            case .success:
                NSLog("[TestApp] SDK patient identified")
                completion(nil)
            case .failure(let error):
                NSLog("[TestApp] setPatient failed: \(error)")
                completion("setPatient failed: \(error)")
            }
        }
    }

    // Exported by Kotlin/Native as `clearPatient_()` (trailing underscore): the plain
    // `clearPatient` selector collides with the SDK-bridge protocols that also export a
    // `clearPatient` returning a value, so K/N disambiguates this Unit-returning one.
    func clearPatient_() {
        // The native SDK keeps its own session; the shared UI treats the shell state as logged out.
        MockRecording.clear()
    }

    func setMock(name: String) {
        NSLog("[TestApp][mock] setMock(\(name))")
        MockRecording.apply(name)
        NSLog("[TestApp][mock] after apply, MockRecordingName=\(UserDefaults.standard.string(forKey: MockRecording.userDefaultsKey) ?? "nil")")
    }

    func applyBaselineMock() {
        // On-device real recordings are the iOS baseline; mocks stay opt-in per run.
        NSLog("[TestApp][mock] applyBaselineMock -> clear")
        MockRecording.clear()
    }

    func openWebLogin(url: String, callbackScheme: String) {
        guard let loginUrl = URL(string: url) else { return }
        let session = ASWebAuthenticationSession(
            url: loginUrl,
            callbackURLScheme: callbackScheme
        ) { [weak self] callbackURL, error in
            self?.webLoginSession = nil
            if let callbackURL {
                // Push the `<scheme>://open/otp?...` callback into the shared UI (parsed there).
                TestAppDeepLinks.shared.onUrl(url: callbackURL.absoluteString)
            } else if let error {
                NSLog("[TestApp] Clinician web login cancelled/failed: \(error.localizedDescription)")
            }
        }
        session.presentationContextProvider = self
        session.prefersEphemeralWebBrowserSession = false
        webLoginSession = session
        session.start()
    }
}

extension NativeShellDelegate: ASWebAuthenticationPresentationContextProviding {
    func presentationAnchor(for session: ASWebAuthenticationSession) -> ASPresentationAnchor {
        UIApplication.shared.connectedScenes
            .compactMap { ($0 as? UIWindowScene)?.keyWindow }
            .first ?? ASPresentationAnchor()
    }
}
