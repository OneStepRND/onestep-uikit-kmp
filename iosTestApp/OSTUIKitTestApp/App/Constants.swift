import Foundation
import CryptoKit

struct Organization {
    let name: String
    let displayName: String
    let appId: String
    let apiKey: String
    let identityVerificationSecret: String

    /// HMAC-SHA256 sign the distinctId with this org's secret as raw UTF-8 bytes, returning a hex string.
    func signIdentity(distinctId: String) -> String? {
        guard let keyData = identityVerificationSecret.data(using: .utf8),
              let messageData = distinctId.data(using: .utf8) else {
            return nil
        }
        let key = SymmetricKey(data: keyData)
        let signature = HMAC<SHA256>.authenticationCode(for: messageData, using: key)
        return signature.map { String(format: "%02x", $0) }.joined()
    }
}

enum Organizations {
    static let sdkTesting = Organization(
        name: "sdk_testing",
        displayName: "SDK Testing",
        appId: "3cd1bc3b-51b0-4cda-89e3-d25398c7a52e",
        apiKey: "V97CL6uOrvxVMGrRxPM3pslkBzmDcXD0tL26vDgI4OI",
        identityVerificationSecret: "Rc6xWFBDQxGe7frJhhQ3f9Fm9b44ZnCsQ4QOGxLh0X0"
    )

    static let zimmerDev = Organization(
        name: "zimmer_dev",
        displayName: "Zimmer (Dev)",
        appId: "147c6678-4faa-49eb-a4a6-5ab92627b203",
        apiKey: "6VwvGvmajB9oiOvaQ1AsuMw4gyHvj66wm_C2CAQVHsM",
        identityVerificationSecret: "i67VcE-mFZACKbKOBXYWuNDszpwVGp5encz3mnFONRU"
    )

    static let appClip = Organization(
        name: "app_clip",
        displayName: "OneStep App Clip",
        appId: "4486cfd2-9beb-4d46-8d9f-713ea88e5e87",
        apiKey: "yYKhzrVbUvkFBXJUXPSLRMNmNuAVI7XqjmCFClrh1wc",
        identityVerificationSecret: "415KbT_RjVFJUlT0haajcJCOOqJv_10PhKyf1oIfNzg"
    )

    static let all: [Organization] = [sdkTesting, zimmerDev, appClip]

    static let `default` = sdkTesting

    static func find(byName name: String) -> Organization? {
        all.first { $0.name == name }
    }
}

enum AppConstants {
    static let releaseBaseURL = "https://app.onestep.co/api/"
    static let avatarAangDistinctId = "018fb9ec-d44b-7232-927b-a9e3612321a3"
}
