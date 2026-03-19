import SwiftUI
import shared

struct TournamentScreen: View {

    @StateObject private var host = TournamentViewModelHost()

    var body: some View {
        Group {
            if host.uiState.isLoading {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

            } else if let error = host.uiState.error {
                VStack(spacing: 16) {
                    Text("Erro ao carregar torneios")
                        .font(.headline)
                    Text(error)
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)
                    Button("Tentar novamente") { host.load() }
                        .buttonStyle(.borderedProminent)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)

            } else if host.uiState.isEmpty {
                VStack(spacing: 12) {
                    Image(systemName: "trophy")
                        .font(.system(size: 48))
                        .foregroundColor(.secondary)
                    Text("Nenhum torneio nos próximos 40 dias")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)

            } else {
                TournamentListView(
                    tournaments: (host.uiState.tournaments as? [TournamentIos]) ?? []
                )
            }
        }
    }
}

private struct TournamentListView: View {

    let tournaments: [TournamentIos]

    var body: some View {
        List(tournaments, id: \.id) { tournament in
            TournamentRow(tournament: tournament)
        }
        .listStyle(.plain)
    }
}

private struct TournamentRow: View {

    let tournament: TournamentIos

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text(tournament.name)
                    .font(.headline)
                    .lineLimit(2)
                Spacer()
                StatusBadge(status: tournament.status)
            }

            Label(tournament.venue, systemImage: "mappin.circle")
                .font(.subheadline)
                .foregroundColor(.secondary)

            HStack(spacing: 16) {
                Label(formatDateRange(tournament.startDate, tournament.endDate), systemImage: "calendar")
                Label("R$ \(String(format: "%.2f", tournament.priceMain))", systemImage: "brazilianrealsign.circle")
            }
            .font(.caption)
            .foregroundColor(.secondary)

            if let url = URL(string: tournament.registrationUrl) {
                Link("Inscrever-se", destination: url)
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .padding(.top, 2)
            }
        }
        .padding(.vertical, 6)
    }

    private func formatDateRange(_ start: String, _ end: String) -> String {
        let parts = start.split(separator: "-")
        let endParts = end.split(separator: "-")
        guard parts.count == 3, endParts.count == 3 else { return start }
        let startFormatted = "\(parts[2])/\(parts[1])"
        let endFormatted = "\(endParts[2])/\(endParts[1])"
        return startFormatted == endFormatted ? startFormatted : "\(startFormatted) – \(endFormatted)"
    }
}

private struct StatusBadge: View {

    let status: String

    var body: some View {
        Text(status)
            .font(.caption2)
            .fontWeight(.semibold)
            .padding(.horizontal, 8)
            .padding(.vertical, 3)
            .background(color.opacity(0.15))
            .foregroundColor(color)
            .clipShape(Capsule())
    }

    private var color: Color {
        switch status {
        case "Aberto", "Inscrições abertas": return .green
        case "Confirmado": return .blue
        default: return .secondary
        }
    }
}
