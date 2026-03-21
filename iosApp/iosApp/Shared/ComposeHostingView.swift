import SwiftUI

/// Generic UIViewControllerRepresentable that hosts any UIViewController (e.g. ComposeUIViewController).
/// Use this to embed shared Compose Multiplatform screens inside SwiftUI.
struct ComposeHostingView: UIViewControllerRepresentable {
    let makeController: () -> UIViewController

    func makeUIViewController(context: Context) -> UIViewController {
        makeController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
