import SwiftUI

// MARK: - Color palette

extension Color {
    /// Alabaster Grey #E7E7E7 — app background, disabled text
    static let appBackground = Color(r: 0xE7, g: 0xE7, b: 0xE7)
    /// White — card / surface
    static let appSurface    = Color.white
    /// Amaranth #CE4257 — primary brand colour, CTAs, tab bar background
    static let appPrimary    = Color(r: 0xCE, g: 0x42, b: 0x57)
    /// Spicy Paprika #DB5316 — secondary accent, gradients, refresh button
    static let appSecondary  = Color(r: 0xDB, g: 0x53, b: 0x16)
    /// Pitch Black #141204 — primary text
    static let appTextDark   = Color(r: 0x14, g: 0x12, b: 0x04)
    /// Deep Mocha #433633 — secondary text, placeholders, muted icons
    static let appTextMuted  = Color(r: 0x43, g: 0x36, b: 0x33)

    init(r: UInt8, g: UInt8, b: UInt8) {
        self.init(red: Double(r) / 255, green: Double(g) / 255, blue: Double(b) / 255)
    }
}

// MARK: - Typography — Helvetica Neue

extension Font {
    // Display
    static let appLogo     = Font.custom("Helvetica Neue", size: 52).weight(.black)
    // Headings
    static let appH1       = Font.custom("Helvetica Neue", size: 28).weight(.bold)
    static let appH2       = Font.custom("Helvetica Neue", size: 22).weight(.bold)
    static let appH3       = Font.custom("Helvetica Neue", size: 18).weight(.semibold)
    // Title / label
    static let appTitle    = Font.custom("Helvetica Neue", size: 16).weight(.semibold)
    static let appSubtitle = Font.custom("Helvetica Neue", size: 15).weight(.medium)
    // Body
    static let appBody     = Font.custom("Helvetica Neue", size: 15)
    static let appBodySm   = Font.custom("Helvetica Neue", size: 13)
    // Caption / footnote
    static let appCaption  = Font.custom("Helvetica Neue", size: 12).weight(.medium)
    static let appFootnote = Font.custom("Helvetica Neue", size: 11)
}
