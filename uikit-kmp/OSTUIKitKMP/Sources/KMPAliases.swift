import OSTUIKit

// Disambiguation aliases for KMP types.
//
// The KMP framework module is named `OSTUIKit` and ALSO exports a class with the Swift name
// `OSTUIKit`, so module-qualified lookup (`OSTUIKit.Foo`) resolves to the class and fails.
// Many KMP type names additionally collide with native `OneStepSDK` / `OneStepUIKit` types
// (OSTMotionMeasurement, OSTEvent, OSTParamName, ...). This file imports ONLY the KMP module,
// where the names are unambiguous, and re-exposes them under KMP-prefixed aliases for files
// that must import both worlds.
public typealias KMPMotionMeasurement = OSTMotionMeasurement
typealias KMPUserInputMetaData = OSTUserInputMetaData
typealias KMPWalkCourseLength = OSTWalkCourseLength
typealias KMPEvent = OSTEvent
typealias KMPParamName = OSTParamName
typealias KMPNorm = OSTNorm
typealias KMPParameterMetadata = OSTParameterMetadata
typealias KMPDiscreteColor = OSTDiscreteColor
typealias KMPInsights = OSTInsights
typealias KMPDailyBackgroundMeasurement = OSTDailyBackgroundMeasurement
typealias KMPError = OSTError

// Public aliases for host apps that import both OSTUIKitKMP and the native OneStepUIKit
// (whose type names overlap, e.g. OSTRecordingConfiguration).
public typealias KMPRecordingConfiguration = OSTRecordingConfiguration
public typealias KMPPermissionMode = OSTPermissionMode
