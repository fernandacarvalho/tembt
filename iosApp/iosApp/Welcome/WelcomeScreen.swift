import SwiftUI

struct WelcomeScreen: View {

    let onRegistered: () -> Void

    @StateObject private var host = WelcomeViewModelHost()
    @State private var name = ""
    @FocusState private var nameFieldFocused: Bool

    var body: some View {
        ZStack {
            content

            if host.uiState.isLoading {
                Color.appBackground.ignoresSafeArea()
                VStack(spacing: 16) {
                    ProgressView().scaleEffect(1.4).tint(.appPrimary)
                    Text("Entrando...")
                        .font(.appBodySm)
                        .foregroundColor(.appTextMuted)
                }
            }
        }
        .onAppear { host.onRegistered = onRegistered }
    }

    private var content: some View {
        ScrollView {
            VStack(spacing: 0) {
                Spacer().frame(height: 80)

                // Logo
                Text("TEMBT")
                    .font(.appLogo)
                    .foregroundColor(.appPrimary)

                Spacer().frame(height: 4)

                Text("Beach Tennis")
                    .font(.appSubtitle)
                    .foregroundColor(.appTextMuted)

                Spacer().frame(height: 56)

                Text("Como você quer ser chamado?")
                    .font(.appH3)
                    .foregroundColor(.appTextDark)
                    .multilineTextAlignment(.center)

                Spacer().frame(height: 8)

                Text("Digite seu nome para entrar no app.")
                    .font(.appBody)
                    .foregroundColor(.appTextMuted)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)

                Spacer().frame(height: 32)

                TextField("Seu nome", text: $name)
                    .font(.appBody)
                    .textFieldStyle(.roundedBorder)
                    .textContentType(.name)
                    .autocapitalization(.words)
                    .disableAutocorrection(true)
                    .focused($nameFieldFocused)
                    .submitLabel(.done)
                    .onSubmit { submit() }
                    .padding(.horizontal, 32)
                    .tint(.appPrimary)

                if let error = host.uiState.error {
                    Spacer().frame(height: 8)
                    Text(error)
                        .font(.appFootnote)
                        .foregroundColor(.appSecondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)
                }

                Spacer().frame(height: 24)

                Button(action: submit) {
                    Text("Começar")
                        .font(.appTitle)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 4)
                }
                .buttonStyle(.borderedProminent)
                .tint(.appPrimary)
                .disabled(name.trimmingCharacters(in: .whitespaces).isEmpty)
                .padding(.horizontal, 32)

                Spacer().frame(height: 80)
            }
            .frame(maxWidth: .infinity)
        }
        .background(Color.appBackground)
        .onTapGesture { nameFieldFocused = false }
    }

    private func submit() {
        nameFieldFocused = false
        host.onStartClicked(name: name)
    }
}
