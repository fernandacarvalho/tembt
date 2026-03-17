import SwiftUI

struct ContentView: View {

    @StateObject private var appHost = AppViewModelHost()

    var body: some View {
        if appHost.showWelcome {
            WelcomeScreen(onRegistered: { appHost.onRegistered() })
        } else {
            MainTabs()
        }
    }
}

private struct MainTabs: View {
    var body: some View {
        TabView {
            MapScreen()
                .tabItem {
                    Label("Mapa", systemImage: "map.fill")
                }
            ScheduleScreen()
                .tabItem {
                    Label("Agenda", systemImage: "calendar")
                }
        }
    }
}
