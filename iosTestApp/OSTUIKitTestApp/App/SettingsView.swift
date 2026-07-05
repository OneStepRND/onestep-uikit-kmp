import SwiftUI

enum SDKEnvironment: String, CaseIterable {
    case production = "Production"
    case custom = "Custom URL"
}

struct SettingsView: View {
    @ObservedObject var appState: AppState

    @State private var selectedSDKEnvironment: SDKEnvironment = {
        let raw = UserDefaults.standard.string(forKey: "sdk_environment") ?? SDKEnvironment.production.rawValue
        return SDKEnvironment(rawValue: raw) ?? .production
    }()
    @State private var customURL: String = UserDefaults.standard.string(forKey: "sdk_customURL") ?? ""
    @State private var selectedOrgName: String = UserDefaults.standard.string(forKey: "sdk_orgName") ?? Organizations.default.name
    @State private var distinctId: String = UserDefaults.standard.string(forKey: "sdk_distinctId") ?? ""

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
            }
            .navigationTitle("Login")
        }
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
