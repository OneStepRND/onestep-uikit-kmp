import SwiftUI

enum SDKEnvironment: String, CaseIterable {
    case production = "Production"
    case custom = "Custom URL"
}

struct SettingsView: View {
    @ObservedObject var appState: AppState

    // Non-nil only when SettingsView is presented as a sheet over the main screen; lets a
    // clinician login that happens while already inside the app dismiss back to it.
    @Environment(\.dismiss) private var dismiss

    @State private var selectedSDKEnvironment: SDKEnvironment = {
        let raw = UserDefaults.standard.string(forKey: "sdk_environment") ?? SDKEnvironment.production.rawValue
        return SDKEnvironment(rawValue: raw) ?? .production
    }()
    @State private var customURL: String = UserDefaults.standard.string(forKey: "sdk_customURL") ?? ""
    @State private var selectedOrgName: String = UserDefaults.standard.string(forKey: "sdk_orgName") ?? Organizations.default.name
    @State private var distinctId: String = UserDefaults.standard.string(forKey: "sdk_distinctId") ?? ""

    @StateObject private var clinicianLogin = ClinicianWebLoginController()

    private var selectedOrg: Organization {
        Organizations.find(byName: selectedOrgName) ?? Organizations.default
    }

    private var canIdentify: Bool {
        !distinctId.trimmingCharacters(in: .whitespaces).isEmpty
    }

    var body: some View {
        NavigationStack {
            Form {
                environmentSection
                organizationSection
                identifySection
                avatarSection
                clinicianWebLoginSection
            }
            .navigationTitle("Login")
            .sheet(isPresented: showClinicianResult) {
                ClinicianLoginResultView(controller: clinicianLogin, appState: appState) {
                    // Entered the app in clinician mode: close the result sheet, and if this
                    // Settings screen is itself a sheet over the main screen, dismiss it too.
                    clinicianLogin.reset()
                    dismiss()
                }
            }
        }
    }

    private var showClinicianResult: Binding<Bool> {
        Binding(
            get: {
                if case .idle = clinicianLogin.state { return false }
                return true
            },
            set: { presented in if !presented { clinicianLogin.reset() } }
        )
    }

    // MARK: - Sections

    private var environmentSection: some View {
        Section("Environment") {
            Picker("Environment", selection: $selectedSDKEnvironment) {
                ForEach(SDKEnvironment.allCases, id: \.self) { env in
                    Text(env.rawValue).tag(env)
                }
            }
            .pickerStyle(.segmented)

            if selectedSDKEnvironment == .custom {
                TextField("Base URL", text: $customURL)
                    .autocorrectionDisabled()
                    .textInputAutocapitalization(.never)
                    .keyboardType(.URL)
            }
        }
    }

    private var organizationSection: some View {
        Section("Organization") {
            Picker("Organization", selection: $selectedOrgName) {
                ForEach(Organizations.all, id: \.name) { org in
                    Text(org.displayName).tag(org.name)
                }
            }

            LabeledContent("App ID") {
                Text(selectedOrg.appId)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .lineLimit(1)
                    .truncationMode(.middle)
            }
        }
    }

    private var identifySection: some View {
        Section {
            TextField("Distinct ID", text: $distinctId)
                .autocorrectionDisabled()
                .textInputAutocapitalization(.never)
                .accessibilityIdentifier("settings.distinctId")

            Button {
                identify(distinctId: distinctId.trimmingCharacters(in: .whitespaces))
            } label: {
                Text("Identify")
                    .frame(maxWidth: .infinity)
                    .fontWeight(.semibold)
            }
            .disabled(!canIdentify)
            .accessibilityIdentifier("settings.identify")
        } header: {
            Text("Identity")
        }
    }

    private var avatarSection: some View {
        Section {
            HStack {
                Spacer()
                Text("or")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                Spacer()
            }
            .listRowBackground(Color.clear)

            Button {
                identify(distinctId: AppConstants.avatarAangDistinctId)
            } label: {
                HStack {
                    Image(systemName: "person.crop.circle.fill")
                    Text("Connect as Avatar")
                }
                .frame(maxWidth: .infinity)
                .fontWeight(.semibold)
            }
            .tint(.orange)
            .accessibilityIdentifier("settings.connectAvatar")
        }
    }

    // Clinician web login (Google + OTP on the hosted page), independent of SDK identification.
    private var clinicianWebLoginSection: some View {
        Section {
            Button {
                clinicianLogin.start(environment: selectedSDKEnvironment, customURL: customURL)
            } label: {
                HStack {
                    Image(systemName: "globe")
                    Text("Sign in with Clinician Web Login")
                }
                .frame(maxWidth: .infinity)
                .fontWeight(.semibold)
            }
            .accessibilityIdentifier("settings.clinicianWebLogin")
        } header: {
            Text("Clinician Web Login")
        } footer: {
            Text("Sign in via the hosted clinician page (Google + OTP). Returns a JWT.")
        }
    }

    // MARK: - Actions

    private func identify(distinctId: String) {
        let org = selectedOrg

        UserDefaults.standard.set(selectedSDKEnvironment.rawValue, forKey: "sdk_environment")
        UserDefaults.standard.set(customURL, forKey: "sdk_customURL")
        UserDefaults.standard.set(org.name, forKey: "sdk_orgName")
        UserDefaults.standard.set(distinctId, forKey: "sdk_distinctId")

        appState.initializeSDK()
    }
}

// MARK: - Clinician Web Login result

/// Displays the clinician web-login result. The JWT is shown on-screen (internal test harness);
/// it is never logged and no clinician PII/PHI is echoed (HIPAA).
struct ClinicianLoginResultView: View {
    @ObservedObject var controller: ClinicianWebLoginController
    @ObservedObject var appState: AppState
    /// Invoked after the user chooses to enter the app; the presenter tears down the login sheets.
    let onEnteredApp: () -> Void
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            Form {
                switch controller.state {
                case .idle, .inProgress:
                    progressRow("Waiting for browser sign-in… complete the login, then you'll return here.")
                case .exchanging:
                    progressRow("Exchanging one-time code for a session token…")
                case .success(let session):
                    successBody(session)
                case .failure(let message):
                    Text("Login failed: \(message)")
                        .foregroundStyle(.red)
                        .accessibilityIdentifier("clinicianLogin.error")
                }
            }
            .navigationTitle("Clinician Web Login")
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") { dismiss() }
                        .accessibilityIdentifier("clinicianLogin.done")
                }
            }
        }
    }

    private func progressRow(_ message: String) -> some View {
        HStack(spacing: 12) {
            ProgressView()
            Text(message).font(.subheadline).foregroundStyle(.secondary)
        }
    }

    @ViewBuilder
    private func successBody(_ session: ClinicianWebLogin.Session) -> some View {
        Section("Status") {
            Label("Signed in", systemImage: "checkmark.circle.fill")
                .foregroundStyle(.green)
        }
        Section("JWT") {
            Text(session.token)
                .font(.system(.caption, design: .monospaced))
                .textSelection(.enabled)
                .accessibilityIdentifier("clinicianLogin.token")
        }
        if let userUUID = session.userUUID {
            Section("User UUID") {
                Text(userUUID)
                    .font(.system(.caption, design: .monospaced))
                    .textSelection(.enabled)
            }
        }
        Section {
            Button {
                // Enter the app in clinician mode. If the SDK isn't up yet, this initializes it
                // and the root swaps to ContentView; onEnteredApp tears down the login sheets so
                // we land on the main screen in every case (including when already signed in).
                appState.completeClinicianLogin(session)
                onEnteredApp()
            } label: {
                Text("Continue to App")
                    .frame(maxWidth: .infinity)
                    .fontWeight(.semibold)
            }
            .accessibilityIdentifier("clinicianLogin.continue")
        }
    }
}
