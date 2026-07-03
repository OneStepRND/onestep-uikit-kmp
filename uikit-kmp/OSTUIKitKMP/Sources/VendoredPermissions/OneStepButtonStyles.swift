//
//  OneStepButtonStyles.swift
//  OneStepUIKit
//

import SwiftUI

// MARK: - Button Size

enum OneStepButtonSize {
    case big
    case small
    case mini

    var fontSize: CGFloat {
        switch self {
        case .big: 18
        case .small: 18
        case .mini: 14
        }
    }

    var verticalPadding: CGFloat {
        switch self {
        case .big: 16
        case .small: 8
        case .mini: 8
        }
    }

    var horizontalPadding: CGFloat {
        switch self {
        case .big: 16
        case .small: 16
        case .mini: 8
        }
    }

    var isFullWidth: Bool {
        switch self {
        case .big: true
        case .small: true
        case .mini: false
        }
    }

    var fontWeight: OSTCustomFontWeight {
        switch self {
        case .big: .bold
        case .small: .bold
        case .mini: .semibold
        }
    }
}

// MARK: - Fill Button Style

struct OneStepFillButtonStyle: ButtonStyle {
    @Environment(\.oneStepTheme) var theme
    @Environment(\.isEnabled) var isEnabled

    let size: OneStepButtonSize

    var big: OneStepFillButtonStyle { .init(size: .big) }
    var small: OneStepFillButtonStyle { .init(size: .small) }
    var mini: OneStepFillButtonStyle { .init(size: .mini) }

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .frame(maxWidth: size.isFullWidth ? .infinity : nil)
            .padding(.horizontal, size.horizontalPadding)
            .padding(.vertical, size.verticalPadding)
            .font(.appFont(size: size.fontSize, type: .bold))
            .foregroundStyle(isEnabled ? Color.neutralM4 : Color.neutralP2)
            .background(
                RoundedRectangle(cornerRadius: 4)
                    .fill(isEnabled ? theme.primaryColor : Color.neutralM2)
            )
            .opacity(configuration.isPressed ? 0.6 : 1)
    }
}

// MARK: - Outline Button Style

struct OneStepOutlineButtonStyle: ButtonStyle {
    @Environment(\.isEnabled) var isEnabled

    let size: OneStepButtonSize

    var big: OneStepOutlineButtonStyle { .init(size: .big) }
    var small: OneStepOutlineButtonStyle { .init(size: .small) }
    var mini: OneStepOutlineButtonStyle { .init(size: .mini) }

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .frame(maxWidth: size.isFullWidth ? .infinity : nil)
            .padding(.horizontal, size.horizontalPadding)
            .padding(.vertical, size.verticalPadding)
            .font(.appFont(size: size.fontSize, type: size.fontWeight))
            .foregroundStyle(isEnabled ? Color.neutralP3 : Color.neutralP2)
            .background(
                RoundedRectangle(cornerRadius: 4)
                    .fill(isEnabled ? Color.clear : Color.neutralM2)
            )
            .overlay(
                RoundedRectangle(cornerRadius: 4)
                    .inset(by: 0.5)
                    .stroke(isEnabled ? Color.neutralP3 : Color.neutralP1, lineWidth: 1)
            )
            .opacity(configuration.isPressed ? 0.6 : 1)
    }
}

// MARK: - Text Button Style

struct OneStepTextButtonStyle: ButtonStyle {
    @Environment(\.isEnabled) var isEnabled

    let size: OneStepButtonSize

    var big: OneStepTextButtonStyle { .init(size: .big) }
    var small: OneStepTextButtonStyle { .init(size: .small) }
    var mini: OneStepTextButtonStyle { .init(size: .mini) }

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .padding(.horizontal, size.horizontalPadding)
            .padding(.vertical, size.verticalPadding)
            .font(.appFont(size: size.fontSize, type: .bold))
            .foregroundStyle(isEnabled ? Color.neutralP3 : Color.neutralP1)
            .opacity(configuration.isPressed ? 0.6 : 1)
    }
}

// MARK: - Namespace

struct OneStepButtonStyleNamespace: ButtonStyle {
    @Environment(\.oneStepTheme) var theme
    @Environment(\.isEnabled) var isEnabled

    var fill: OneStepFillButtonStyle { .init(size: .big) }
    var outline: OneStepOutlineButtonStyle { .init(size: .big) }
    var text: OneStepTextButtonStyle { .init(size: .big) }

    func makeBody(configuration: Configuration) -> some View {
        OneStepFillButtonStyle(size: .big)
            .makeBody(configuration: configuration)
    }
}

extension ButtonStyle where Self == OneStepButtonStyleNamespace {
    static var onestep: OneStepButtonStyleNamespace { .init() }
}

// MARK: - Previews

#if DEBUG
private struct ButtonGrid: View {
    let size: OneStepButtonSize
    let label: String

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text(label)
                .font(.appFont(size: 14, type: .bold))
                .foregroundStyle(Color.neutralP3)

            HStack(alignment: .top, spacing: 24) {
                VStack(spacing: 8) {
                    Button("Text") {}
                        .buttonStyle(OneStepFillButtonStyle(size: size))
                    Button("Text") {}
                        .buttonStyle(OneStepFillButtonStyle(size: size))
                        .disabled(true)
                }

                VStack(spacing: 8) {
                    Button("Text") {}
                        .buttonStyle(OneStepOutlineButtonStyle(size: size))
                    Button("Text") {}
                        .buttonStyle(OneStepOutlineButtonStyle(size: size))
                        .disabled(true)
                }

                VStack(spacing: 8) {
                    Button("Text") {}
                        .buttonStyle(OneStepTextButtonStyle(size: size))
                    Button("Text") {}
                        .buttonStyle(OneStepTextButtonStyle(size: size))
                        .disabled(true)
                }
            }
        }
    }
}

#Preview("Button Styles") {
    ScrollView {
        VStack(alignment: .leading, spacing: 32) {
            HStack(spacing: 24) {
                Color.clear.frame(width: 40)
                Text("Fill").font(.appFont(size: 12))
                    .frame(maxWidth: .infinity)
                Text("Outline").font(.appFont(size: 12))
                    .frame(maxWidth: .infinity)
                Text("Text").font(.appFont(size: 12))
                    .frame(maxWidth: .infinity)
            }

            ButtonGrid(size: .big, label: "Big")
            ButtonGrid(size: .small, label: "Small")
            ButtonGrid(size: .mini, label: "Mini")
        }
        .padding(20)
    }
    .background(Color.neutralM4)
    .oneStepTheme(.default)
}
#endif
