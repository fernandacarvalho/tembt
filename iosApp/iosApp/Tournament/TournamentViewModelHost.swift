import Foundation
import shared

/// ObservableObject that bridges the Kotlin TournamentViewModelIos to SwiftUI's state system.
/// Must be held as @StateObject (not @ObservedObject) in the consuming view so that
/// deinit is guaranteed to fire, which cancels the underlying Kotlin coroutine scope.
@MainActor
final class TournamentViewModelHost: ObservableObject {

    @Published private(set) var uiState = TournamentUiStateIos(
        isLoading: true,
        tournaments: [],
        isEmpty: false,
        error: nil
    )

    private let vmIos: TournamentViewModelIos

    init() {
        vmIos = KoinHelper.shared.getTournamentViewModelIos()
        vmIos.startObserving { [weak self] state in
            self?.uiState = state
        }
    }

    func load(city: String? = nil) {
        vmIos.load(city: city)
    }

    deinit {
        vmIos.clear()
    }
}
