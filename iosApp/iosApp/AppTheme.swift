import SwiftUI

// MARK: - Color palette

extension Color {
    /// Alabaster Grey — app background
    static let appBackground = Color(r: 0xE7, g: 0xE7, b: 0xE7)
    /// White — card / surface
    static let appSurface    = Color.white
    /// Pumpkin Spice — primary: buttons, checked states, highlighted text
    static let appPrimary    = Color(r: 0xFC, g: 0x7A, b: 0x1E)
    /// Molten Orange — secondary accent
    static let appSecondary  = Color(r: 0xF2, g: 0x4C, b: 0x00)
    /// Dusk Blue — dark accent, muted text, icons, graphic elements
    static let appBlue       = Color(r: 0x48, g: 0x56, b: 0x96)
    /// Apricot Cream — warm highlight / dividers
    static let appApricot    = Color(r: 0xF9, g: 0xC7, b: 0x84)
    /// Near-black for primary text on light backgrounds
    static let appTextDark   = Color(red: 0.1, green: 0.1, blue: 0.1)
    /// DuskBlue as secondary / muted text
    static let appTextMuted  = Color(r: 0x48, g: 0x56, b: 0x96)

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
