import SwiftUI
import UIKit
import shared

struct ContentView: View {

    @StateObject private var appHost = AppViewModelHost()
    @State private var selectedTab: AppTab = .map

    var body: some View {
        if appHost.showWelcome {
            ComposeHostingView {
                ViewControllersKt.welcomeViewController(onRegistered: { appHost.onRegistered() })
            }
            .ignoresSafeArea()
        } else if let tabs = appHost.enabledTabs {
            if tabs.count == 1 {
                // Only the home (map) is enabled — no tab bar at all
                MapScreen()
            } else {
                MainTabs(tabs: tabs, selectedTab: $selectedTab)
                    .onOpenURL { url in
                        if url.scheme == "tembt", url.host == "schedule", tabs.contains(.schedule) {
                            selectedTab = .schedule
                        }
                    }
            }
        } else {
            // Session tab config still resolving — mirror LaunchScreen.storyboard so
            // the transition from the launch screen is seamless
            LaunchPlaceholder()
        }
    }
}

private struct LaunchPlaceholder: View {
    var body: some View {
        GeometryReader { geometry in
            Image("welcome_player")
                .resizable()
                .scaledToFill()
                .frame(width: geometry.size.width, height: geometry.size.height)
                .clipped()
        }
        .background(Color.black)
        .ignoresSafeArea()
    }
}

private struct MainTabs: View {

    let tabs: [AppTab]
    @Binding var selectedTab: AppTab

    init(tabs: [AppTab], selectedTab: Binding<AppTab>) {
        self.tabs = tabs
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
            ForEach(tabs, id: \.self) { tab in
                screen(for: tab)
                    .tabItem { label(for: tab) }
                    .tag(tab)
            }
        }
        .tint(Color.appPrimary)
    }

    @ViewBuilder
    private func screen(for tab: AppTab) -> some View {
        switch tab {
        case .map:
            MapScreen()
        case .schedule:
            ComposeHostingView { ViewControllersKt.scheduleViewController() }
                .ignoresSafeArea()
        case .tournaments:
            ComposeHostingView { ViewControllersKt.tournamentViewController() }
                .ignoresSafeArea()
        default:
            EmptyView()
        }
    }

    @ViewBuilder
    private func label(for tab: AppTab) -> some View {
        switch tab {
        case .map:
            Label(String(localized: "tab_court"), systemImage: "map.fill")
        case .schedule:
            Label(String(localized: "tab_schedule"), systemImage: "list.bullet")
        case .tournaments:
            Label(String(localized: "tab_tournaments"), systemImage: "trophy.fill")
        default:
            Label("", systemImage: "questionmark")
        }
    }
}
