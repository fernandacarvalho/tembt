import Foundation
import shared

/// ObservableObject that bridges the Kotlin MapViewModelIos to SwiftUI's state system.
/// Must be held as @StateObject (not @ObservedObject) in the consuming view so that
/// deinit is guaranteed to fire, which cancels the underlying Kotlin coroutine scope.
@MainActor
final class MapViewModelHost: ObservableObject {

    @Published private(set) var uiState = MapUiStateIos(
        isLoading: true,
        isPermissionRequired: false,
        isDenied: false,
        center: nil
    )

    private let vmIos: MapViewModelIos

    init() {
        vmIos = KoinHelper.shared.getMapViewModelIos()
        vmIos.startObserving { [weak self] state in
            self?.uiState = state
        }
    }

    func checkPermission() {
        vmIos.checkPermission()
    }

    deinit {
        vmIos.clear()
    }
}
