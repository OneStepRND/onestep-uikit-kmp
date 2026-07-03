//
//  BottomSheetView.swift
//  OneStepUIKit
//
//  Created by David Havkin on 20/05/2025.
//


import SwiftUI

struct PopOverResizableViewContainer<Content : View>: View {
    @Environment(\.dismiss) private var dismiss
    @State private var sheetContentHeight = CGFloat(0)
    private let alignment: Alignment
    private let fixedDetents: Set<PresentationDetent>?
    private let content: Content

    /// - Parameter fixedDetents: When non-nil, the sheet uses these detents instead of
    ///   self-sizing to the content height. Pass `[.large]` for tall, scrollable content
    ///   (e.g. measurement instructions) that would otherwise lock to a partial height
    ///   because a `ScrollView` reports only the height the sheet offers it. Leave `nil`
    ///   (default) for short popovers that should size to their content.
    init(alignment: Alignment = .center,
         fixedDetents: Set<PresentationDetent>? = nil,
         @ViewBuilder contentBuilder: () -> Content){
        self.alignment = alignment
        self.fixedDetents = fixedDetents
        self.content = contentBuilder()
    }

    private var presentationDetents: Set<PresentationDetent> {
        if let fixedDetents { return fixedDetents }
        return sheetContentHeight > 0 ? [.height(sheetContentHeight)] : [.medium]
    }

    var body: some View {
        NavigationStack {
            bodyContent
        }
        .presentationDetents(presentationDetents)
        .presentationDragIndicator(.visible)
    }
    
    var bodyContent: some View {
        content
            .frame(maxWidth: .infinity, alignment: alignment)
            .padding(20)
            .background(
                GeometryReader { geometryProxy in
                    Color.clear
                        .preference(key: SizePreferenceKey.self, value: geometryProxy.size)
                }
            )
            .onPreferenceChange(SizePreferenceKey.self) { newSize in
                sheetContentHeight = newSize.height + 44.0 // account for navigation bar
            }
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        dismiss()
                    } label: {
                        Image(systemName: "xmark")
                            .font(.system(size: 24, weight: .medium))
                            .foregroundColor(.neutralP2)
                    }
                }
            }
    }
}



private struct PopOverText: View {
    private let text: String
    private let size: CGFloat
    private let weight: OSTCustomFontWeight
    private let alignment: TextAlignment

    init(_ text: String, size: CGFloat = 18, weight: OSTCustomFontWeight = .regular, alignment: TextAlignment = .leading) {
        self.text = text
        self.size = size
        self.weight = weight
        self.alignment = alignment
    }

    var body: some View {
        Text(text)
            .font(.appFont(size: size, type: weight))
            .foregroundColor(.neutralP3)
            .fixedSize(horizontal: false, vertical: true)
            .multilineTextAlignment(alignment)
    }
}

struct PopOverTextTitle: View {
    let text: String

    init(_ text: String) {
        self.text = text
    }

    var body: some View {
        PopOverText(text, size: 20, weight: .bold)
    }
}

struct PopOverTextBody: View {
    let text: String

    init(_ text: String) {
        self.text = text
    }

    var body: some View {
        PopOverText(text, size: 18, weight: .regular)
    }
}


#if DEBUG
#Preview {
    PopOverResizableViewContainer {
        PopOverTextTitle("Title")
        PopOverTextBody("Main text")
    }
}
#endif
