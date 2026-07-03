//
//  File.swift
//
//
//  Created by Maor Duani on 08/07/2024.
//

import SwiftUI

extension Font {
    static func appFont(size: CGFloat, type: OSTCustomFontWeight = .regular) -> Font {
        // Priority 1: Custom font set via OSTTheme
        if let customFont = OSTTheme.customFontInternal, let _  = UIFont(name: customFont.byWeight(type), size: 15) {
            return Font.custom(customFont.byWeight(type), size: size)
        }
        // Priority 2: Hebrew font for Hebrew locale (with fallback to default if not found)
        else if isHebrewLocale() {
            let hebrewFontName = hebrewFont.byWeight(type)
            if let _ = UIFont(name: hebrewFontName, size: 15) {
                return Font.custom(hebrewFontName, size: size)
            } else {
                print("⚠️ Hebrew font '\(hebrewFontName)' not found, using default font")
                return Font.custom(defaultFont.byWeight(type), size: size)
            }
        }
        // Priority 3: Default font (Nunito Sans)
        else {
            return Font.custom(defaultFont.byWeight(type), size: size)
        }
    }
}

extension Font {
    internal static let defaultFont: OSTCustomFont = {
        let regular = "Nunito Sans 7pt Regular"
        let semibold = "Nunito Sans 7pt SemiBold"
        let bold = "Nunito Sans 7pt Bold"
        return OSTCustomFont(regular: regular, semibold: semibold, bold: bold)
    }()

    // Hebrew font (Assistant) - used automatically when device language is Hebrew
    internal static let hebrewFont: OSTCustomFont = {
        let regular = "Assistant-Regular"
        let semibold = "Assistant-SemiBold"
        let bold = "Assistant-Bold"
        return OSTCustomFont(regular: regular, semibold: semibold, bold: bold)
    }()
    
    // Helper to detect Hebrew locale
    private static func isHebrewLocale() -> Bool {
        return Locale.current.language.languageCode?.identifier == "he"
    }

    static func registerAllFonts() {
        // Register default fonts (Nunito Sans)
        registerFont(named: "NunitoSans_7pt-Regular")
        registerFont(named: "NunitoSans_7pt-SemiBold")
        registerFont(named: "NunitoSans_7pt-Bold")

        // Register Hebrew fonts (Assistant)
        registerFont(named: "Assistant-Regular")
        registerFont(named: "Assistant-SemiBold")
        registerFont(named: "Assistant-Bold")
    }
    
    private static func getFontPostScriptName(fontName: String) -> String {
        guard let asset = NSDataAsset(name: "Fonts/\(fontName)", bundle: Bundle.module),
                      let provider = CGDataProvider(data: asset.data as NSData),
              let font = CGFont(provider), let fontName = font.postScriptName as String? else {
            return ""
        }
        return fontName
    }
        
    private static func registerFont(named name: String) {
        //dump(UIFont.familyNames)
        guard let asset = NSDataAsset(name: "Fonts/\(name)", bundle: Bundle.module),
                      let provider = CGDataProvider(data: asset.data as NSData),
                      let font = CGFont(provider),
              CTFontManagerRegisterGraphicsFont(font, nil) else {
            //print("FONTTT - failed")
            return
        }
        //print("FONTTT - success. name: \(name)")
    }
}

enum OSTCustomFontWeight: Sendable {
    case regular
    case semibold
    case bold
}

struct OSTCustomFont: Sendable {
    let regular: String
    let semibold: String
    let bold: String
    
    init(regular: String, semibold: String, bold: String) {
        self.regular = regular
        self.semibold = semibold
        self.bold = bold
    }
    
    func byWeight(_ weight: OSTCustomFontWeight) -> String {
        switch weight {
        case .regular:
            return regular
        case .semibold:
            return semibold
        case .bold:
            return bold
        }
    }
}
