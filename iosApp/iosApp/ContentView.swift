import SwiftUI
import shared

struct ContentView: View {

    @StateObject private var appHost = AppViewModelHost()

    var body: some View {
        if appHost.showWelcome {
            ComposeHostingView {
                ViewControllersKt.welcomeViewController(onRegistered: { appHost.onRegistered() })
            }
            .ignoresSafeArea()
        } else {
            MainTabs()
        }
    }
}

private struct MainTabs: View {
    var body: some View {
        TabView {
            MapScreen()
                .tabItem { Label("Quadra", systemImage: "map.fill") }
            ComposeHostingView { ViewControllersKt.scheduleViewController() }
                .tabItem { Label("Lista", systemImage: "list.bullet") }
            ComposeHostingView { ViewControllersKt.tournamentViewController() }
                .tabItem { Label("Torneios", systemImage: "trophy.fill") }
        }
    }
}
