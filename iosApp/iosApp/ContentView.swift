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
                    Label("Quadra", systemImage: "map.fill")
                }
            ScheduleScreen()
                .tabItem {
                    Label("Lista", systemImage: "list.bullet")
                }
            TournamentScreen()
                .tabItem {
                    Label("Torneios", systemImage: "trophy.fill")
                }
        }
    }
}
