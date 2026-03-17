import Foundation
import shared

/// ObservableObject that bridges the Kotlin ScheduleViewModelIos to SwiftUI's state system.
/// Must be held as @StateObject (not @ObservedObject) in the consuming view so that
/// deinit is guaranteed to fire, which cancels the underlying Kotlin coroutine scope.
@MainActor
final class ScheduleViewModelHost: ObservableObject {

    @Published private(set) var uiState = ScheduleUiStateIos(
        isLoading: true,
        date: nil,
        slots: [],
        checkedInSlotTime: nil,
        isCheckingIn: false,
        error: nil
    )

    private let vmIos: ScheduleViewModelIos

    init() {
        vmIos = KoinHelper.shared.getScheduleViewModelIos()
        vmIos.startObserving { [weak self] state in
            self?.uiState = state
        }
    }

    func loadWindow() {
        vmIos.loadWindow()
    }

    func checkin(slotTime: String) {
        vmIos.checkin(slotTime: slotTime)
    }

    deinit {
        vmIos.clear()
    }
}
