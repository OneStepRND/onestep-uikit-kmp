// swift-tools-version: 5.9
// Phase 0 spike: minimal @objc facade package to validate spm4Kmp -> Kotlin cinterop.
import PackageDescription

let package = Package(
    name: "SpikeObjCKit",
    platforms: [.iOS(.v16)],
    products: [
        .library(name: "SpikeObjCKit", targets: ["SpikeObjCKit"])
    ],
    targets: [
        .target(name: "SpikeObjCKit")
    ]
)
