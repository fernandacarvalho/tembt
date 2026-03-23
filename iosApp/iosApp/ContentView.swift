import SwiftUI
import UIKit
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

    init() {
        // iOS 26: no UITabBarAppearance — any appearance setting overrides the native
        // Liquid Glass material and makes the pill appear opaque. Let the system render it.
        if #available(iOS 26, *) { } else {
            let appearance = UITabBarAppearance()
            appearance.configureWithTransparentBackground()

            let item = UITabBarItemAppearance()
            item.normal.iconColor = UIColor(Color.appBackground).withAlphaComponent(0.6)
            item.normal.titleTextAttributes = [.foregroundColor: UIColor(Color.appBackground).withAlphaComponent(0.6)]
            item.selected.iconColor = UIColor(Color.appBackground)
            item.selected.titleTextAttributes = [.foregroundColor: UIColor(Color.appBackground)]
            appearance.stackedLayoutAppearance = item

            UITabBar.appearance().standardAppearance = appearance
            UITabBar.appearance().scrollEdgeAppearance = appearance
            UITabBar.appearance().backgroundColor = UIColor(Color.appPrimary)
            UITabBar.appearance().isTranslucent = true
        }
    }

    var body: some View {
        TabView {
            MapScreen()
                .tabItem { Label("Quadra", systemImage: "map.fill") }
            ComposeHostingView { ViewControllersKt.scheduleViewController() }
                .ignoresSafeArea()
                .tabItem { Label("Lista", systemImage: "list.bullet") }
            ComposeHostingView { ViewControllersKt.tournamentViewController() }
                .ignoresSafeArea()
                .tabItem { Label("Torneios", systemImage: "trophy.fill") }
        }
        .tint(Color.appPrimary)
    }
}
