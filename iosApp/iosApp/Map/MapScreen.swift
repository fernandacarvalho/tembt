import SwiftUI
import shared

struct MapScreen: View {

    @StateObject private var host = MapViewModelHost()
    @Environment(\.scenePhase) private var scenePhase

    var body: some View {
        Group {
            if host.uiState.isLoading {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

            } else if host.uiState.isPermissionRequired {
                PermissionScreen(
                    isDenied: host.uiState.isDenied,
                    onRequestPermission: { host.checkPermission() },
                    onOpenSettings: { host.checkPermission() }
                )

            } else if let error = host.uiState.error {
                VStack(spacing: 16) {
                    Text("Erro ao carregar")
                        .font(.headline)
                    Text(error)
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)
                    Button("Tentar novamente") { host.checkPermission() }
                        .buttonStyle(.borderedProminent)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)

            } else if let center = host.uiState.center {
                MapViewRepresentable(center: center)
                    .ignoresSafeArea()
            }
        }
        .onAppear { host.checkPermission() }
        .onChange(of: scenePhase) { phase in
            if phase == .active { host.checkPermission() }
        }
    }
}
