//
//  File.swift
//  
//
//  Created by Maor Duani on 24/07/2024.
//

import SwiftUI

struct SizePreferenceKey: PreferenceKey {
    static var defaultValue: CGSize = .zero
    static func reduce(value: inout CGSize, nextValue: () -> CGSize) {}
}

extension View {
    
    /// A modifier for getting subview's size
    func readSize(onChange: @escaping (CGSize) -> Void) -> some View {
        background(
            GeometryReader { proxy in
                Color.clear
                    .preference(key: SizePreferenceKey.self, value: proxy.size)
            }
        )
        .onPreferenceChange(SizePreferenceKey.self, perform: onChange)
    }
}

// MARK: - Custom View Modifier


struct CustomNavigationBarModifier: ViewModifier {
    @Environment(\.dismiss) private var dismiss
    var onBack: (() -> Void)?
    var onClose: (() -> Void)?
    var rectangleInsteadOfBorder: Bool = false
    @Environment(\.colorScheme) var colorScheme
    var hidden = false
    var backButtonHidden = true

    func body(content: Content) -> some View {
        if hidden {
            content
                .navigationBarBackButtonHidden(backButtonHidden)
        } else {
            content
                .navigationBarBackButtonHidden(backButtonHidden)
                .toolbar {
                    //close button
                    ToolbarItem(placement: .topBarTrailing) {
                        Button {
                            onClose?()
                        } label: {
                            Image(.closeButton)
                                .foregroundStyle(Color.neutralP3)
                        }
                    }
                }
                .overlay(
                    viewForOverLay,
                    alignment: .top
                )
        }
    }
    
    var viewForOverLay: some View {
        VStack {
            if rectangleInsteadOfBorder {
                VStack {
                    Rectangle()
                        .fill(self.colorScheme == .light ? Color.neutralM2 : .black)
                        .frame(height: 4)
                    Spacer()
                }
            } else {
                VStack {
//                    Divider() // Adds the line
//                        .background(Color.white.opacity(0.5))
//                        .shadow(radius: 3) // Adds shadow to the divider
//                        .padding(.top, 5)
                    Spacer()
                }
            }
        }
    }
}

extension View {
    func customNavigationBarStyle(onBack: (() -> Void)? = nil, onClose: (() -> Void)? = nil, rectangleInsteadOfBorder: Bool = false, hidden: Bool = false, backButtonHidden: Bool = true) -> some View {
        self.modifier(CustomNavigationBarModifier(onBack: onBack, onClose: onClose, rectangleInsteadOfBorder: rectangleInsteadOfBorder, hidden: hidden, backButtonHidden: backButtonHidden))
    }
}
