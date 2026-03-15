import Foundation
import shared

/// ObservableObject that bridges WelcomeViewModelIos to SwiftUI's state system.
/// Must be held as @StateObject in the consuming view.
@MainActor
final class WelcomeViewModelHost: ObservableObject {

    @Published private(set) var uiState = WelcomeUiStateIos(isLoading: false, error: nil)

    /// Set by the parent view (WelcomeScreen) in .onAppear so the host can
    /// trigger navigation without holding a direct reference to the parent.
    var onRegistered: (() -> Void)?

    private let vmIos: WelcomeViewModelIos

    init() {
        vmIos = KoinHelper.shared.getWelcomeViewModelIos()
        vmIos.startObserving(
            onStateChange: { [weak self] state in self?.uiState = state },
            onNavigateToMap: { [weak self] in self?.onRegistered?() }
        )
    }

    func onStartClicked(name: String) {
        vmIos.onStartClicked(name: name)
    }

    deinit {
        vmIos.clear()
    }
}
