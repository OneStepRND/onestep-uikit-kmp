//
//  PermissionsFlowAnalytics.swift
//  OneStepUIKit
//
//  Created by David Havkin Code on 06/01/2026.
//

import Foundation
import OneStepSDK

/// Helper class for tracking permission flow analytics
/// Publishes events to OneStepUIKit.shared.uiEventsPublisher for the hosting app to consume
class PermissionsFlowAnalytics {

    // shortcut: the native uikit forwards these to a Combine subject host apps may observe; no
    // KMP host consumes it, so this vendored copy drops them. Upgrade path: forward to the KMP
    // analyticsHandler passed to OSTUIKitIos.configure().
    private static func sendUIEvent(_ event: OneStepSDK.OSTEvent) {}

    /// Track a screen view event
    /// - Parameters:
    ///   - screenName: The screen identifier (e.g., "data_safety", "permission_request")
    ///   - permission: The permission type being requested
    ///   - variant: The variant (first_time or after_denied)
    ///   - flowName: The flow name
    static func trackScreen(
        _ screenName: String,
        permission: String? = nil,
        variant: String? = nil,
        flowName: String
    ) {
        var properties: [String: Any] = [:]
        properties["flow_name"] = flowName
        if let permission = permission {
            properties["permission"] = permission
        }
        if let variant = variant {
            properties["variant"] = variant
        }

        let event = OneStepSDK.OSTEvent(name: "screen: \(screenName)", properties: properties)
        sendUIEvent(event)
    }

    /// Track a click event
    /// - Parameters:
    ///   - action: The action identifier (e.g., "allow", "go_to_settings")
    ///   - permission: The permission type
    ///   - variant: The variant (first_time or after_denied)
    ///   - flowName: The flow name
    static func trackClick(
        _ action: String,
        permission: String,
        variant: String,
        flowName: String
    ) {
        var properties: [String: Any] = [:]
        properties["permission"] = permission
        properties["variant"] = variant
        properties["flow_name"] = flowName

        let event = OneStepSDK.OSTEvent(name: "clicked: \(action)", properties: properties)
        sendUIEvent(event)
    }

    /// Track a permission status event
    /// - Parameters:
    ///   - permissionName: The permission name (e.g., "physical_activity", "location")
    ///   - status: The status (granted, denied, or null)
    ///   - scope: The scope (while_using, always, or null) - only for location
    ///   - flowName: The flow name
    static func trackPermissionStatus(
        _ permissionName: String,
        status: String?,
        scope: String? = nil,
        flowName: String
    ) {
        var properties: [String: Any] = [:]
        properties["flow_name"] = flowName
        if let status = status {
            properties["status"] = status
        }
        if let scope = scope {
            properties["scope"] = scope
        }

        let event = OneStepSDK.OSTEvent(name: "\(permissionName)_status", properties: properties)
        sendUIEvent(event)
    }
}

/// Permission type identifiers for analytics
enum PermissionType: String {
    case locationWhileUsing = "location_while_using"
    case locationAlways = "location_always"
    case motionFitness = "motion_fitness"
    case healthkit = "healthkit"
    case microphone = "microphone"
    case physicalActivity = "physical_activity"
    case notifications = "notifications"
    case camera = "camera"
}

/// Permission variant identifiers for analytics
enum PermissionVariant: String {
    case firstTime = "first_time"
    case afterDenied = "after_denied"
    case restricted = "restricted"
}

/// Flow name identifiers for analytics
enum FlowName: String {
    case inApp = "in_app"
    case background = "background"
    case dualTask = "dual_task"
    case stepCounter = "step_counter"
    case full = "full"
    case healthKit = "healthkit"
}
