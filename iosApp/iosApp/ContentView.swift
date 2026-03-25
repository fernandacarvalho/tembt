import SwiftUI
import UIKit
import shared

struct ContentView: View {

    @StateObject private var appHost = AppViewModelHost()
    @State private var selectedTab = 0

    var body: some View {
        if appHost.showWelcome {
            ComposeHostingView {
                ViewControllersKt.welcomeViewController(onRegistered: { appHost.onRegistered() })
            }
            .ignoresSafeArea()
        } else {
            MainTabs(selectedTab: $selectedTab)
                .onOpenURL { url in
                    if url.scheme == "tembt", url.host == "schedule" {
                        selectedTab = 1  // Lista tab index
                    }
                }
        }
    }
}

private struct MainTabs: View {

    @Binding var selectedTab: Int

    init(selectedTab: Binding<Int>) {
        _selectedTab = selectedTab
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
        TabView(selection: $selectedTab) {
            MapScreen()
                .tabItem { Label("Quadra", systemImage: "map.fill") }
                .tag(0)
            ComposeHostingView { ViewControllersKt.scheduleViewController() }
                .ignoresSafeArea()
                .tabItem { Label("Lista", systemImage: "list.bullet") }
                .tag(1)
            ComposeHostingView { ViewControllersKt.tournamentViewController() }
                .ignoresSafeArea()
                .tabItem { Label("Torneios", systemImage: "trophy.fill") }
                .tag(2)
        }
        .tint(Color.appPrimary)
    }
}
