import SwiftUI
import CoreLocation

struct PermissionScreen: View {

    let isDenied: Bool
    let onRequestPermission: () -> Void
    let onOpenSettings: () -> Void

    @State private var isAwaitingPermission = false
    @StateObject private var locationRequester = LocationPermissionRequester()

    var body: some View {
        ZStack {
            content

            if isAwaitingPermission {
                Color(.systemBackground)
                    .ignoresSafeArea()

                VStack(spacing: 16) {
                    ProgressView()
                        .scaleEffect(1.4)
                    Text("Verificando permissão...")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
            }
        }
        .onAppear {
            locationRequester.onAuthorizationChanged = {
                isAwaitingPermission = false
                onRequestPermission()
            }
        }
    }

    private var content: some View {
        VStack(spacing: 0) {
            Spacer()

            Text("📍")
                .font(.system(size: 64))

            Spacer().frame(height: 24)

            Text("Localização necessária")
                .font(.title2)
                .fontWeight(.semibold)
                .multilineTextAlignment(.center)

            Spacer().frame(height: 12)

            Text("Este app precisa da sua localização para mostrar o mapa e os pontos de interesse próximos a você.")
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)

            Spacer().frame(height: 40)

            if isDenied {
                Text("A permissão foi negada. Acesse as configurações para habilitá-la.")
                    .font(.footnote)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)

                Spacer().frame(height: 20)

                Button("Abrir configurações") {
                    if let url = URL(string: UIApplication.openSettingsURLString) {
                        UIApplication.shared.open(url)
                    }
                    onOpenSettings()
                }
                .buttonStyle(.borderedProminent)
            } else {
                Button("Permitir localização") {
                    isAwaitingPermission = true
                    locationRequester.requestPermission()
                }
                .buttonStyle(.borderedProminent)
            }

            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color(.systemBackground))
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
        manager.requestWhenInUseAuthorization()
    }

    nonisolated func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        Task { @MainActor in
            // Only notify if authorization is no longer undetermined
            // (avoids firing on initial load before any request is made)
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
