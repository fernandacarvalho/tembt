import SwiftUI
import shared

@main
struct iOSApp: App {

    init() {
        // Start Koin DI container once at app launch
        KoinHelperKt.startKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
