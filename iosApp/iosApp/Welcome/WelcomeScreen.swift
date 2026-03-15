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
                Color(.systemBackground)
                    .ignoresSafeArea()

                VStack(spacing: 16) {
                    ProgressView()
                        .scaleEffect(1.4)
                    Text("Entrando...")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
            }
        }
        .onAppear {
            host.onRegistered = onRegistered
        }
    }

    private var content: some View {
        ScrollView {
            VStack(spacing: 0) {
                Spacer().frame(height: 80)

                // Logo
                Text("TEMBT")
                    .font(.system(size: 52, weight: .black, design: .rounded))
                    .foregroundColor(.accentColor)

                Spacer().frame(height: 4)

                Text("Beach Tennis")
                    .font(.title3)
                    .foregroundColor(.secondary)

                Spacer().frame(height: 56)

                Text("Como você quer ser chamado?")
                    .font(.title2)
                    .fontWeight(.semibold)
                    .multilineTextAlignment(.center)

                Spacer().frame(height: 8)

                Text("Digite seu nome para entrar no app.")
                    .font(.body)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)

                Spacer().frame(height: 32)

                TextField("Seu nome", text: $name)
                    .textFieldStyle(.roundedBorder)
                    .textContentType(.name)
                    .autocapitalization(.words)
                    .disableAutocorrection(true)
                    .focused($nameFieldFocused)
                    .submitLabel(.done)
                    .onSubmit { submit() }
                    .padding(.horizontal, 32)

                if let error = host.uiState.error {
                    Spacer().frame(height: 8)
                    Text(error)
                        .font(.footnote)
                        .foregroundColor(.red)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)
                }

                Spacer().frame(height: 24)

                Button(action: submit) {
                    Text("Começar")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
                .disabled(name.trimmingCharacters(in: .whitespaces).isEmpty)
                .padding(.horizontal, 32)

                Spacer().frame(height: 80)
            }
            .frame(maxWidth: .infinity)
        }
        .background(Color(.systemBackground))
        .onTapGesture { nameFieldFocused = false }
    }

    private func submit() {
        nameFieldFocused = false
        host.onStartClicked(name: name)
    }
}
