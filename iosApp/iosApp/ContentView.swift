import SwiftUI

struct ContentView: View {

    @StateObject private var appHost = AppViewModelHost()

    var body: some View {
        if appHost.showWelcome {
            WelcomeScreen(onRegistered: { appHost.onRegistered() })
        } else {
            MapScreen()
        }
    }
}
