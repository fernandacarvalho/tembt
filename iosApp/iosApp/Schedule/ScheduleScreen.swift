import SwiftUI
import shared

struct ScheduleScreen: View {

    @StateObject private var host = ScheduleViewModelHost()

    var body: some View {
        ZStack {
            Color.appBackground.ignoresSafeArea()

            if host.uiState.isLoading {
                ProgressView().tint(.appPrimary)

            } else if let error = host.uiState.error {
                ErrorView(message: error, onRetry: { host.loadWindow() })

            } else {
                let displayDate = host.uiState.date.map { formatPollDate($0) }
                PollListView(
                    displayDate: displayDate,
                    slots: host.uiState.slots,
                    checkedInSlotTime: host.uiState.checkedInSlotTime,
                    onCheckin: { [weak host] time in host?.checkin(slotTime: time) }
                )
            }
        }
    }
}

// MARK: - Error

private struct ErrorView: View {
    let message: String
    let onRetry: () -> Void
    var body: some View {
        VStack(spacing: 12) {
            Text("Erro ao carregar").font(.appH3).foregroundColor(.appTextDark)
            Text(message).font(.appBodySm).foregroundColor(.appTextMuted)
                .multilineTextAlignment(.center).padding(.horizontal, 32)
            Button("Tentar novamente", action: onRetry)
                .font(.appSubtitle).foregroundColor(.appPrimary)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

// MARK: - Poll list

private struct PollListView: View {

    let displayDate: String?
    let slots: [WindowSlotIos]
    let checkedInSlotTime: String?
    let onCheckin: (String) -> Void

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                if let d = displayDate {
                    EquatableView(content: PollHeader(formattedDate: d))
                        .padding(.bottom, 4)
                }
                ForEach(slots, id: \.time) { slot in
                    EquatableView(content: PollSlotCard(
                        slot: slot,
                        isCheckedIn: checkedInSlotTime == slot.time,
                        onTap: { onCheckin(slot.time) }
                    ))
                }
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 24)
        }
    }
}

// MARK: - Header

private struct PollHeader: View, Equatable {

    let formattedDate: String

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            // "Bora para o play" in black, date in orange
            (Text("Bora para o play ").foregroundColor(.appTextDark)
             + Text(formattedDate).foregroundColor(.appPrimary))
                .font(.appH2)

            HStack(spacing: 0) {
                Image(systemName: "checkmark")
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(.appTextMuted)
                Image(systemName: "checkmark")
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(.appTextMuted)
                    .offset(x: -5)
                Text("Selecione uma ou mais opções")
                    .font(.appBodySm)
                    .foregroundColor(.appTextMuted)
                    .padding(.leading, 4)
            }
        }
    }
}

// MARK: - Slot card

private struct PollSlotCard: View, Equatable {

    let slot: WindowSlotIos
    let isCheckedIn: Bool
    let onTap: () -> Void

    static func == (lhs: PollSlotCard, rhs: PollSlotCard) -> Bool {
        lhs.slot.time == rhs.slot.time &&
        lhs.isCheckedIn == rhs.isCheckedIn &&
        lhs.players.count == rhs.players.count
    }

    private var players: [SlotPlayerIos] {
        (slot.players as? [SlotPlayerIos]) ?? []
    }

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 14) {
                EquatableView(content: CheckCircle(isChecked: isCheckedIn))

                Text(slot.time)
                    .font(.custom("Helvetica Neue", size: 26).weight(.semibold))
                    .foregroundColor(.appTextDark)

                Spacer()

                if !players.isEmpty {
                    EquatableView(content: PlayerAvatarsCount(players: players))
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
            .background(Color.appSurface)
            .clipShape(RoundedRectangle(cornerRadius: 12))
            // Apricot left border when checked
            .overlay(alignment: .leading) {
                if isCheckedIn {
                    RoundedRectangle(cornerRadius: 2)
                        .fill(Color.appApricot)
                        .frame(width: 4)
                        .padding(.vertical, 10)
                }
            }
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Check circle

private struct CheckCircle: View, Equatable {

    let isChecked: Bool

    var body: some View {
        ZStack {
            Circle()
                .fill(isChecked ? Color.appPrimary : .clear)
                .frame(width: 28, height: 28)
            Circle()
                .strokeBorder(
                    isChecked ? Color.appPrimary : Color(r: 0xCC, g: 0xCC, b: 0xCC),
                    lineWidth: 2
                )
                .frame(width: 28, height: 28)
            if isChecked {
                Image(systemName: "checkmark")
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(.white)
            }
        }
    }
}

// MARK: - Player avatars

private let scheduleAvatarColors: [Color] = [
    Color(r: 0x48, g: 0x56, b: 0x96), // DuskBlue
    Color(r: 0xFC, g: 0x7A, b: 0x1E), // PumpkinSpice
    Color(r: 0xF2, g: 0x4C, b: 0x00), // MoltenOrange
    Color(r: 0x8B, g: 0x5E, b: 0x3C),
    Color(r: 0x27, g: 0xAE, b: 0x60),
]

private func scheduleAvatarColor(for name: String) -> Color {
    scheduleAvatarColors[abs(name.utf8.reduce(0) { (31 &* $0) &+ Int($1) }) % scheduleAvatarColors.count]
}

private struct PlayerAvatarsCount: View, Equatable {

    let players: [SlotPlayerIos]

    static func == (lhs: PlayerAvatarsCount, rhs: PlayerAvatarsCount) -> Bool {
        lhs.players.count == rhs.players.count &&
        zip(lhs.players, rhs.players).allSatisfy { $0.name == $1.name }
    }

    private var visible: [SlotPlayerIos] { Array(players.suffix(min(2, players.count))) }

    var body: some View {
        HStack(spacing: 0) {
            ZStack(alignment: .leading) {
                ForEach(Array(visible.enumerated()), id: \.element.name) { index, player in
                    Circle()
                        .fill(scheduleAvatarColor(for: player.name))
                        .frame(width: 30, height: 30)
                        .overlay(
                            Text(String(player.name.prefix(1)).uppercased())
                                .font(.appCaption)
                                .foregroundColor(.white)
                        )
                        .overlay(Circle().strokeBorder(Color.appSurface, lineWidth: 1.5))
                        .offset(x: CGFloat(index) * 22)
                }
            }
            .frame(width: visible.count == 1 ? 30 : 52, height: 30)

            Text(players.count.description)
                .font(.appSubtitle)
                .foregroundColor(.appTextMuted)
                .padding(.leading, 8)
        }
    }
}

// MARK: - Helpers

private func formatPollDate(_ date: String) -> String {
    let parts = date.split(separator: "-")
    guard parts.count == 3,
          let month = Int(parts[1]),
          let day = Int(parts[2]) else { return date }
    return "\(day)/\(month)"
}
