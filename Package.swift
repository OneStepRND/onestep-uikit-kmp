// swift-tools-version:5.9
import PackageDescription

// NOTE: The binaryTarget below is managed by scripts/publish-xcframework.sh (CI).
// Snapshot publishes rewrite it to a GitHub Release URL + checksum and commit the result.
// Until the first publish it points at a locally built XCFramework:
//   ./gradlew uikit-kmp:assembleOSTUIKitReleaseXCFramework
let package = Package(
    name: "OSTUIKitKMP",
    platforms: [.iOS(.v16)],
    products: [
        .library(name: "OSTUIKitKMP", targets: ["OSTUIKitKMP"])
    ],
    dependencies: [
        // Native OneStep iOS SDK. The permission flow is vendored so this package
        // has NO dependency on the native iOS UIKit.
        .package(url: "https://github.com/OneStepRND/onestep-sdk-ios", exact: "2.0.8-rc1")
    ],
    targets: [
        .binaryTarget(
            name: "OSTUIKit",
            path: "uikit-kmp/build/XCFrameworks/release/OSTUIKit.xcframework"
        ),
        .target(
            // Target name == Swift module name: must match the product so `import OSTUIKitKMP` works.
            name: "OSTUIKitKMP",
            dependencies: [
                "OSTUIKit",
                .product(name: "OneStepSDK", package: "onestep-sdk-ios")
            ],
            path: "uikit-kmp/OSTUIKitKMP/Sources",
            resources: [
                .copy("Resources/compose-resources"),
                .process("Resources/Media.xcassets"),
                .process("Resources/Fonts.xcassets"),
                .process("Resources/Localizable.xcstrings")
            ]
        )
    ]
)
