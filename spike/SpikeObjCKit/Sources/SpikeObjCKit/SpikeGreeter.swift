import Foundation

/// Phase 0 spike: the shape of the @objc facade we'd ask the SDK team to ship.
/// Exercises the three interop patterns the real bridges need:
/// 1. sync call, 2. completion-handler -> Kotlin suspend, 3. callback registration -> Kotlin Flow.
@objc public enum SpikeState: Int {
    case idle
    case recording
    case done
}

@objcMembers public final class SpikeMeasurement: NSObject {
    public let identifier: String
    public let steps: Int

    public init(identifier: String, steps: Int) {
        self.identifier = identifier
        self.steps = steps
    }
}

@objcMembers public final class SpikeGreeter: NSObject {
    private var tickHandler: ((Int) -> Void)?

    /// Pattern 1: plain sync call.
    public func greet(name: String) -> String {
        "Hello, \(name), from a Swift package via spm4Kmp"
    }

    /// Pattern 2: Swift async function. The generated ObjC header gets swift_async
    /// attributes, which Kotlin/Native cinterop surfaces as a suspend function.
    public func fetchMeasurement() async throws -> SpikeMeasurement {
        try await Task.sleep(nanoseconds: 50_000_000)
        return SpikeMeasurement(identifier: "spike-1", steps: 42)
    }

    /// Pattern 3: block-based callback registration (state-stream stand-in).
    public func onTick(_ handler: @escaping (Int) -> Void) {
        tickHandler = handler
    }

    public func emitTick(_ value: Int) {
        tickHandler?(value)
    }

    public func currentState() -> SpikeState { .idle }
}
