//
//  Theme.swift
//
//
//  Created by Maor Duani on 30/06/2024.
//

import SwiftUI

class OSTTheme: ObservableObject {
    @Published var primaryColor: Color = .primaryP3
    @Published var customFont: OSTCustomFont? = nil {
        didSet {
            Self.customFontInternal = customFont
        }
    }

    static var customFontInternal: OSTCustomFont?

    init() {
        Font.registerAllFonts()
    }
}

extension OSTTheme {
    static let `default` = OSTTheme()
}

private struct ThemeEnvironmentKey: EnvironmentKey {
  static var defaultValue: OSTTheme = OSTTheme.default
}

extension EnvironmentValues {
    var oneStepTheme: OSTTheme {
    get { self[ThemeEnvironmentKey.self] }
    set { self[ThemeEnvironmentKey.self] = newValue }
  }
}

extension View {
    func oneStepTheme(_ theme: OSTTheme) -> some View  {
        environment(\.oneStepTheme, theme)
    }
}
