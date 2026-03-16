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
                MapReadyView(center: center, host: host)
            }
        }
        .onAppear { host.checkPermission() }
        .onChange(of: scenePhase) { phase in
            if phase == .active { host.checkPermission() }
        }
    }
}

// Extracted to avoid let-in-ViewBuilder ambiguity when casting the player list
private struct MapReadyView: View {

    let center: MapCoordinates
    @ObservedObject var host: MapViewModelHost
    @State private var centerTrigger = 0

    // Safe cast from Kotlin List<PlayerIos> (ObjC NSArray) to Swift [PlayerIos]
    private var players: [PlayerIos] {
        (host.uiState.players as? [PlayerIos]) ?? []
    }

    var body: some View {
        ZStack(alignment: .topLeading) {
            MapViewRepresentable(center: center, players: players, centerTrigger: centerTrigger)
                .ignoresSafeArea()

            HStack(spacing: 6) {
                Text("\(players.count) jogadores no local")
                Button(action: { host.refreshPlayers() }) {
                    Image(systemName: "arrow.clockwise")
                }
            }
            .font(.subheadline)
            .fontWeight(.medium)
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 8))
            .padding(.top, 56)
            .padding(.leading, 16)

            Button(action: { host.sendLocation() }) {
                Text("Ir")
                    .font(.headline)
                    .fontWeight(.bold)
                    .frame(width: 56, height: 56)
                    .background(Color.accentColor)
                    .foregroundColor(.white)
                    .clipShape(Circle())
                    .shadow(radius: 4)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottomLeading)
            .padding(.leading, 16)
            .padding(.bottom, 32)
            .ignoresSafeArea(edges: .bottom)

            Button(action: { centerTrigger += 1 }) {
                Image(systemName: "location.fill")
                    .font(.headline)
                    .frame(width: 56, height: 56)
                    .background(Color.accentColor)
                    .foregroundColor(.white)
                    .clipShape(Circle())
                    .shadow(radius: 4)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottomTrailing)
            .padding(.trailing, 16)
            .padding(.bottom, 32)
            .ignoresSafeArea(edges: .bottom)
        }
    }
}
