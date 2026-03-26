import SwiftUI
import CoreLocation
import shared

struct MapScreen: View {

    @StateObject private var host = MapViewModelHost()
    @Environment(\.scenePhase) private var scenePhase

    // Permission requesting (moved from PermissionScreen.swift)
    @State private var isAwaitingPermission = false
    @StateObject private var locationRequester = LocationPermissionRequester()

    var body: some View {
        Group {
            if host.uiState.isLoading {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

            } else if host.uiState.isPermissionRequired {
                ZStack {
                    ComposeHostingView {
                        ViewControllersKt.permissionViewController(
                            onRequestPermission: { [self] in
                                isAwaitingPermission = true
                                locationRequester.requestPermission()
                            }
                        )
                    }
                    .ignoresSafeArea()

                    if isAwaitingPermission {
                        Color.appBackground.ignoresSafeArea()
                        VStack(spacing: 16) {
                            ProgressView().scaleEffect(1.4).tint(.appPrimary)
                            Text("Verificando permissão...")
                                .font(.appBodySm)
                                .foregroundColor(.appTextMuted)
                        }
                    }
                }

            } else if let error = host.uiState.error {
                VStack(spacing: 16) {
                    Text("Erro ao carregar")
                        .font(.appH3)
                        .foregroundColor(.appTextDark)
                    Text(error)
                        .font(.appBody)
                        .foregroundColor(.appTextMuted)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)
                    Button("Tentar novamente") { host.checkPermission() }
                        .buttonStyle(.borderedProminent)
                        .tint(.appPrimary)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)

            } else if let center = host.uiState.center {
                MapReadyView(
                    center: center,
                    courtName: host.uiState.courtName,
                    host: host
                )
            }
        }
        .onAppear {
            host.checkPermission()
            locationRequester.onAuthorizationChanged = { [weak host] in
                isAwaitingPermission = false
                host?.checkPermission()
            }
        }
        .onChange(of: scenePhase) { phase in
            if phase == .active {
                isAwaitingPermission = false
                host.checkPermission()
            }
        }
    }
}

// Extracted to avoid let-in-ViewBuilder ambiguity when casting the player list
private struct MapReadyView: View {

    let center: MapCoordinates
    let courtName: String
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

            courtInfo
            mapTarget
        }
    }

    private var courtInfo: some View {
        HStack(alignment: .center, spacing: 0) {
                VStack(alignment: .leading, spacing: 2) {
                    if !courtName.isEmpty {
                        Text(courtName)
                            .font(.appSubtitle)
                            .foregroundColor(.appTextDark)
                    }
                    Text("\(players.count) jogadores no local agora")
                        .font(.appBody)
                        .foregroundColor(.appTextDark)
                }
                Spacer()
                Button(action: { host.refreshPlayers() }) {
                    Image(systemName: "arrow.clockwise")
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(.appSecondary)
                }
                .padding(.leading, 12)
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 10)
            .background(Color.appBackground, in: Capsule())
            .shadow(color: .black.opacity(0.2), radius: 4, x: 0, y: 2)
            .padding(.top, 56)
            .padding(.horizontal, 16)
    }

    private var mapTarget: some View {
        Button(action: { centerTrigger += 1 }) {
                Image(systemName: "location.fill")
                    .font(.system(size: 18, weight: .medium))
                    .foregroundColor(.appTextMuted)
                    .frame(width: 44, height: 44)
                    .background(Color.appBackground)
                    .clipShape(Circle())
                    .shadow(color: .black.opacity(0.25), radius: 4, x: 0, y: 1)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottomTrailing)
            .padding(.trailing, 16)
            .padding(.bottom, 120)
            .ignoresSafeArea(edges: .bottom)
    }
}

// MARK: - Permission requester

@MainActor
final class LocationPermissionRequester: NSObject, ObservableObject, CLLocationManagerDelegate {

    var onAuthorizationChanged: (() -> Void)?

    private let manager = CLLocationManager()

    override init() {
        super.init()
        manager.delegate = self
    }

    func requestPermission() {
        let status = manager.authorizationStatus
        if status == .denied || status == .restricted {
            if let url = URL(string: UIApplication.openSettingsURLString) {
                UIApplication.shared.open(url)
            }
        } else {
            manager.requestWhenInUseAuthorization()
        }
    }

    nonisolated func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        Task { @MainActor in
            let status = manager.authorizationStatus
            if status != .notDetermined {
                onAuthorizationChanged?()
            }
        }
    }

    deinit {
        manager.delegate = nil
    }
}
