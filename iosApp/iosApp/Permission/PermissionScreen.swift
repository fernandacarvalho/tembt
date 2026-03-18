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
                Color.appBackground.ignoresSafeArea()
                VStack(spacing: 16) {
                    ProgressView().scaleEffect(1.4).tint(.appPrimary)
                    Text("Verificando permissão...")
                        .font(.appBodySm)
                        .foregroundColor(.appTextMuted)
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
        ScrollView {
            VStack(spacing: 0) {
                Spacer().frame(height: 60)

                Text("📍")
                    .font(.system(size: 64))

                Spacer().frame(height: 24)

                Text(isDenied ? "Permissão negada" : "Permissão de localização")
                    .font(.appH3)
                    .foregroundColor(.appTextDark)
                    .multilineTextAlignment(.center)

                Spacer().frame(height: 12)

                Text(
                    isDenied
                        ? "Você negou o acesso à localização. Para usar o TEMBT você precisa habilitá-la nas configurações do iPhone."
                        : "O TEMBT precisa da sua localização para funcionar. Veja abaixo o que é solicitado e por quê."
                )
                .font(.appBody)
                .foregroundColor(.appTextMuted)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)

                Spacer().frame(height: 28)

                if isDenied {
                    SettingsPathCard()
                        .padding(.horizontal, 24)
                } else {
                    PermissionExplanationCard()
                        .padding(.horizontal, 24)
                }

                Spacer().frame(height: 32)

                Button(isDenied ? "Abrir configurações" : "Permitir localização") {
                    if isDenied {
                        if let url = URL(string: UIApplication.openSettingsURLString) {
                            UIApplication.shared.open(url)
                        }
                        onOpenSettings()
                    } else {
                        isAwaitingPermission = true
                        locationRequester.requestPermission()
                    }
                }
                .buttonStyle(.borderedProminent)
                .tint(.appPrimary)
                .font(.appTitle)
                .padding(.horizontal, 32)
                .frame(maxWidth: .infinity)

                Spacer().frame(height: 60)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.appBackground)
    }
}

// MARK: - Explanation card (not denied)

private struct PermissionExplanationCard: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("PERMISSÃO NECESSÁRIA")
                .font(.system(size: 11, weight: .bold))
                .foregroundColor(.appPrimary)
                .tracking(0.8)

            Text("Localização — \"Ao usar o app\"")
                .font(.system(size: 15, weight: .semibold))
                .foregroundColor(.appTextDark)

            VStack(alignment: .leading, spacing: 12) {
                PermissionReasonRow(
                    icon: "🗺️",
                    text: "Mostrar você e outros jogadores no mapa da quadra em tempo real."
                )
                PermissionReasonRow(
                    icon: "📅",
                    text: "Detectar automaticamente se você está na quadra nos fins de semana e feriados, e atualizar sua presença em segundo plano."
                )
            }
        }
        .padding(20)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(Color(.secondarySystemBackground))
        )
    }
}

// MARK: - Settings path card (denied)

private struct SettingsPathCard: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("COMO HABILITAR")
                .font(.system(size: 11, weight: .bold))
                .foregroundColor(.appPrimary)
                .tracking(0.8)

            Text("Ajustes  →  TEMBT  →  Localização  →  \"Ao usar o app\"")
                .font(.system(size: 14))
                .foregroundColor(.appTextMuted)
        }
        .padding(20)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(Color(.secondarySystemBackground))
        )
    }
}

// MARK: - Reason row

private struct PermissionReasonRow: View {
    let icon: String
    let text: String

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            Text(icon)
                .font(.system(size: 18))
                .frame(width: 24)
            Text(text)
                .font(.appBodySm)
                .foregroundColor(.appTextMuted)
                .fixedSize(horizontal: false, vertical: true)
        }
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
