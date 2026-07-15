import Foundation
import shared

/// ObservableObject that bridges AppViewModelIos to SwiftUI's state system.
/// Held as @StateObject in ContentView to manage root-level navigation.
@MainActor
final class AppViewModelHost: ObservableObject {

    @Published private(set) var showWelcome: Bool

    /// nil while the session tab config is being resolved (launch placeholder stays up)
    @Published private(set) var enabledTabs: [AppTab]? = nil

    private let vmIos: AppViewModelIos

    init() {
        // Read the initial value synchronously to avoid a layout flash
        showWelcome = !KoinHelper.shared.isPlayerRegistered()
        vmIos = KoinHelper.shared.getAppViewModelIos()
        vmIos.startObserving { [weak self] show in
            self?.showWelcome = show.boolValue
        }
        vmIos.startObservingTabs { [weak self] tabs in
            self?.enabledTabs = tabs
        }
    }

    func onRegistered() {
        vmIos.onRegistered()
    }

    deinit {
        vmIos.clear()
    }
}
