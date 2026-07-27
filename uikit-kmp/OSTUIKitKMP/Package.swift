// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "OSTUIKitKMP",
    platforms: [.iOS(.v16)],
    products: [
        .library(name: "OSTUIKitKMP", targets: ["OSTUIKitKMP"])
    ],
    dependencies: [
        // Native OneStep iOS SDK. The permission flow (VendoredPermissions/) is vendored from
        // onestep-uikit-ios-spm so this package has NO dependency on the native iOS UIKit.
        .package(url: "https://github.com/OneStepRND/onestep-sdk-ios", exact: "2.1.0-rc3")
    ],
    targets: [
        .binaryTarget(
            name: "OSTUIKit",
            path: "../build/XCFrameworks/debug/OSTUIKit.xcframework"
        ),
        .target(
            // Target name == Swift module name: must match the product so `import OSTUIKitKMP` works.
            name: "OSTUIKitKMP",
            dependencies: [
                "OSTUIKit",
                .product(name: "OneStepSDK", package: "onestep-sdk-ios")
            ],
            path: "Sources",
            resources: [
                .copy("Resources/compose-resources"),
                .process("Resources/Media.xcassets"),
                .process("Resources/Fonts.xcassets"),
                .process("Resources/Localizable.xcstrings")
            ]
        )
    ]
)
