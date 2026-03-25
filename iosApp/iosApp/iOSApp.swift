import SwiftUI
import BackgroundTasks
import shared

@main
struct iOSApp: App {

    init() {
        // Register the BGTask handler BEFORE startKoin schedules the task
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: "com.tembt.court.monitoring",
            using: nil
        ) { task in
            guard let refreshTask = task as? BGAppRefreshTask else {
                task.setTaskCompleted(success: false)
                return
            }
            refreshTask.expirationHandler = {
                refreshTask.setTaskCompleted(success: false)
            }
            KoinHelper.shared.handleBackgroundMonitoringTask {
                refreshTask.setTaskCompleted(success: true)
            }
        }

        // Start Koin DI container once at app launch
        KoinHelperKt.startKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
