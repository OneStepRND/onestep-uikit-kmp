import Foundation
import AuthenticationServices
import UIKit

/// Clinician "web login" flow, ported from the legacy clinician app (onestep-sdk-android
/// `NavigatorActivity` + `AuthRepository`). Independent of the OneStep SDK's patient identification.
///
/// Mechanism (Google sign-in + OTP happen entirely on the hosted web page, "behind the scenes"):
///  1. `ASWebAuthenticationSession` opens `<clinicBase>login?m=2`.
///  2. The clinician signs in with Google on that page; the backend issues a one-time code.
///  3. The page redirects to `<scheme>://open/otp?uuid=&otp=`, which the session intercepts by
///     matching `callbackURLScheme` (no Info.plist URL types needed for ASWebAuthenticationSession).
///  4. `exchangeOTP` POSTs the one-time code to the backend and receives a clinician JWT.
///
/// The single runtime unknown is whether the hosted login page redirects to the scheme this app
/// passes as `callbackURLScheme` (`onestep-prod` for Production, `onestep-dev` otherwise).
enum ClinicianWebLogin {

    /// Result of a successful clinician web login. JWT + opaque user uuid only — no PII/PHI (HIPAA).
    struct Session {
        let token: String
        let userUUID: String?
    }

    enum LoginError: LocalizedError {
        case invalidCallback
        case missingCode
        case http(Int)
        case malformedResponse

        var errorDescription: String? {
            switch self {
            case .invalidCallback: return "The login callback URL was invalid."
            case .missingCode: return "The login callback did not contain a one-time code."
            case .http(let code): return "OTP exchange failed: HTTP \(code)"
            case .malformedResponse: return "The server response could not be parsed."
            }
        }
    }

    /// Clinician web base URL (always ends with '/'), derived from the test app's environment.
    ///
    /// The clinician web app is served from its OWN host (`clinic.onestep.co`) — NOT the SDK API
    /// host (`app.onestep.co`). Deriving it from the API base by stripping `api/` produced
    /// `app.onestep.co/login?m=2`, which 404s. Production uses the dedicated clinic host; Custom
    /// treats the typed URL as the clinic web base verbatim.
    static func clinicBaseURL(environment: SDKEnvironment, customURL: String) -> String {
        let raw: String
        switch environment {
        case .production: raw = AppConstants.clinicianWebBaseURL
        case .custom: raw = customURL.isEmpty ? AppConstants.clinicianWebBaseURL : customURL
        }
        let trimmed = raw.trimmingCharacters(in: .whitespaces)
        return trimmed.hasSuffix("/") ? trimmed : trimmed + "/"
    }

    /// Callback scheme the hosted login page redirects back to.
    static func redirectScheme(environment: SDKEnvironment) -> String {
        switch environment {
        case .production: return "onestep-prod"
        case .custom: return "onestep-dev"
        }
    }

    /// Exchange the one-time (uuid, otp) for a clinician JWT via the backend.
    static func exchangeOTP(
        environment: SDKEnvironment,
        customURL: String,
        uuid: String,
        otp: String
    ) async throws -> Session {
        let endpoint = clinicBaseURL(environment: environment, customURL: customURL)
            + "api/clinician/v1/auth/login/otp/"
        guard let url = URL(string: endpoint) else { throw LoginError.invalidCallback }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.timeoutInterval = 30
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("application/json", forHTTPHeaderField: "Accept")

        let body: [String: Any] = [
            "uuid": uuid,
            "otp": otp,
            "deviceId": "uikit-kmp-test-app",
            "flavour": 1,
            "localeTimezone": TimeZone.current.identifier,
            "manufacturer": "Apple",
            "model": UIDevice.current.model,
            "version": "1.0",
            "versionCode": 1,
        ]
        request.httpBody = try JSONSerialization.data(withJSONObject: body)

        let (data, response) = try await URLSession.shared.data(for: request)
        let status = (response as? HTTPURLResponse)?.statusCode ?? -1
        guard (200..<300).contains(status) else { throw LoginError.http(status) }

        guard let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
              let token = json["token"] as? String else {
            throw LoginError.malformedResponse
        }
        let userUUID = (json["user"] as? [String: Any])?["uuid"] as? String
        return Session(token: token, userUUID: userUUID)
    }

    /// Parse (uuid, otp) out of the `<scheme>://open/otp?uuid=&otp=` callback URL.
    static func parseCallback(_ url: URL) throws -> (uuid: String, otp: String) {
        guard let components = URLComponents(url: url, resolvingAgainstBaseURL: false) else {
            throw LoginError.invalidCallback
        }
        let items = components.queryItems ?? []
        guard let uuid = items.first(where: { $0.name == "uuid" })?.value,
              let otp = items.first(where: { $0.name == "otp" })?.value else {
            throw LoginError.missingCode
        }
        return (uuid, otp)
    }
}

// MARK: - Observable controller for SwiftUI

@MainActor
final class ClinicianWebLoginController: NSObject, ObservableObject,
    ASWebAuthenticationPresentationContextProviding {

    enum State {
        case idle
        case inProgress
        case exchanging
        case success(ClinicianWebLogin.Session)
        case failure(String)
    }

    @Published private(set) var state: State = .idle

    private var session: ASWebAuthenticationSession?

    func start(environment: SDKEnvironment, customURL: String) {
        let base = ClinicianWebLogin.clinicBaseURL(environment: environment, customURL: customURL)
        guard let loginURL = URL(string: base + "login?m=2") else {
            state = .failure("Invalid login URL")
            return
        }
        let scheme = ClinicianWebLogin.redirectScheme(environment: environment)
        state = .inProgress

        let session = ASWebAuthenticationSession(
            url: loginURL,
            callbackURLScheme: scheme
        ) { [weak self] callbackURL, error in
            guard let self else { return }
            if let error {
                // User cancellation is a normal dismissal, not an error state.
                if (error as? ASWebAuthenticationSessionError)?.code == .canceledLogin {
                    self.state = .idle
                } else {
                    self.state = .failure(error.localizedDescription)
                }
                return
            }
            guard let callbackURL else {
                self.state = .failure("No callback URL returned.")
                return
            }
            self.exchange(callbackURL: callbackURL, environment: environment, customURL: customURL)
        }
        session.presentationContextProvider = self
        session.prefersEphemeralWebBrowserSession = false
        self.session = session
        session.start()
    }

    func reset() {
        state = .idle
        session = nil
    }

    private func exchange(callbackURL: URL, environment: SDKEnvironment, customURL: String) {
        state = .exchanging
        Task { @MainActor in
            do {
                let (uuid, otp) = try ClinicianWebLogin.parseCallback(callbackURL)
                let result = try await ClinicianWebLogin.exchangeOTP(
                    environment: environment,
                    customURL: customURL,
                    uuid: uuid,
                    otp: otp
                )
                state = .success(result)
            } catch {
                state = .failure(error.localizedDescription)
            }
        }
    }

    func presentationAnchor(for session: ASWebAuthenticationSession) -> ASPresentationAnchor {
        ASPresentationAnchor()
    }
}
