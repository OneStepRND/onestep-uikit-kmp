//
//  PermissionFlowScreen.swift
//
//
//  Created by Maor Duani on 11/09/2024.
//

import Foundation

enum PermissionFlowScreens: Equatable, Sendable {
    case locationScreen(maxMode: PermissionsNeeded)
    case motionAndFitnessScreen
    case healthKitScreen
    case permissionsRationalization
}

enum PermissionsNeeded: Sendable {
    case locationAlways
    case motionAndFitness
    case healthKit
    case locationWhileInUse
}
