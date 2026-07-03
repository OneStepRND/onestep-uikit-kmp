//
//  File.swift
//  
//
//  Created by Maor Duani on 26/06/2024.
//

import SwiftUI

// MARK: - Base
extension Color {
    static var primaryP3: Color {
        Color(hex: "#0F3157")
    }
    
    static var primary0: Color {
        Color(hex: "#1B81DC")
    }

    static var healthConcernP1: Color {
        Color(hex: "#FB5E1B")
    }

    static var healthCautionP1: Color {
        Color(hex: "#F5960B")
    }
    
    static var healthCautionP2: Color {
        Color(hex: "#F5960B")
    }

    static var healthyP1: Color {
        Color(hex: "#2C9D72")
    }
    
    static var warningP2: Color {
        Color(hex: "#F5960B")
    }

    static var warningM2: Color {
        Color(hex: "#FFFAEB")
    }

    // TODO: update M4 to hex: #FBFBFB
    static var neutralM4: Color {
        return Color.white
    }

    // TODO: update M5 to hex: #FFFFFF
    static var neutralM5: Color {
        Color(hex: "#FBFBFB")
    }

    static var neutralM2: Color {
        Color(hex: "#E7E6E6")
    }
    
    static var neutralM3: Color {
        Color(hex: "#FBFBFB")
    }
    
    static var neutralP3: Color {
        Color(hex: "#3E3D3B")
    }
    
    static var successP3: Color {
        Color(hex: "#2C9D72")
    }
    
    static var errorP2: Color {
        Color(hex: "#B00404")
    }
    
    static var infoP1: Color {
        Color(hex: "#0D5097")
    }
    
    static var neutral0: Color {
        Color(hex: "#B3B0AD")
    }
    
    static var neutralP1: Color {
        Color(hex: "#8C8884")
    }

    static var neutralP2: Color {
        Color(hex: "#716D69")
    }
    
    static var neutralM1: Color {
        Color(hex: "#D2D0CF")
    }
    
    static var primaryM3: Color {
        Color(hex: "#F1F7FE")
    }
}


extension Color {
    init(hex: String) {
        let scanner = Scanner(string: hex.replacingOccurrences(of: "#", with: ""))
        var rgbValue: UInt64 = 0
        scanner.scanHexInt64(&rgbValue)
        
        let r = (rgbValue & 0xff0000) >> 16
        let g = (rgbValue & 0xff00) >> 8
        let b = rgbValue & 0xff
        
        self.init(red: Double(r) / 0xff, green: Double(g) / 0xff, blue: Double(b) / 0xff)
        
    }
}
