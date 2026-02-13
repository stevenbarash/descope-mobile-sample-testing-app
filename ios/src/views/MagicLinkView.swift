import SwiftUI
import DescopeKit

struct MagicLinkView: View {
    @Environment(AppState.self) private var appState
    @State private var email = ""
    @State private var maskedAddress: String?
    @State private var isWaiting = false
    @State private var isLoading = false
    @State private var error: String?

    var body: some View {
        Form {
            if isWaiting {
                waitingSection
            } else {
                emailSection
            }
        }
        .navigationTitle("Magic Link")
        .navigationBarTitleDisplayMode(.inline)
        .onOpenURL { url in
            handleURL(url)
        }
        .alert("Error", isPresented: .init(get: { error != nil }, set: { if !$0 { error = nil } })) {
            Button("OK") { reset() }
        } message: {
            Text(error ?? "")
        }
    }

    private var emailSection: some View {
        Section {
            TextField("Email", text: $email)
                .textContentType(.emailAddress)
                .keyboardType(.emailAddress)
                .autocorrectionDisabled()
                .textInputAutocapitalization(.never)
                .submitLabel(.go)
                .onSubmit { if !email.isEmpty && !isLoading { sendLink() } }
                .accessibilityIdentifier(AccessibilityID.MagicLink.emailField)

            Button {
                sendLink()
            } label: {
                HStack {
                    Text("Send Magic Link")
                    Spacer()
                    if isLoading {
                        ProgressView()
                    }
                }
            }
            .disabled(email.isEmpty || isLoading)
            .accessibilityIdentifier(AccessibilityID.MagicLink.sendButton)
        } footer: {
            Text("We'll send a magic link to your email. Click the link to sign in.")
        }
    }

    private var waitingSection: some View {
        Section {
            VStack(spacing: 20) {
                ProgressView()
                    .scaleEffect(1.5)
                    .padding()

                Text("Check your email")
                    .font(.headline)

                if let maskedAddress {
                    Text("Sent to \(maskedAddress)")
                        .foregroundStyle(.secondary)
                }

                Text("Click the magic link to sign in")
                    .foregroundStyle(.secondary)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 32)
            .accessibilityIdentifier(AccessibilityID.MagicLink.waitingView)

            Button("Cancel", role: .cancel) {
                reset()
            }
            .frame(maxWidth: .infinity)
            .accessibilityIdentifier(AccessibilityID.MagicLink.cancelButton)
        }
    }

    private func sendLink() {
        isLoading = true
        Task {
            do throws(DescopeError) {
                maskedAddress = try await Descope.magicLink.signUpOrIn(with: .email, loginId: email, redirectURL: nil, options: [])
                isWaiting = true
                isLoading = false
            } catch let err {
                error = err.description
                isLoading = false
            }
        }
    }

    private func handleURL(_ url: URL) {
        guard isWaiting else { return }
        guard let components = URLComponents(url: url, resolvingAgainstBaseURL: false),
              let token = components.queryItems?.first(where: { $0.name == "t" })?.value else {
            print("[App] Invalid URL: \(url)")
            return
        }

        isLoading = true
        Task {
            do throws(DescopeError) {
                let authResponse = try await Descope.magicLink.verify(token: token)
                appState.setSession(DescopeSession(from: authResponse))
            } catch let err {
                error = err.description
                isLoading = false
            }
        }
    }

    private func reset() {
        maskedAddress = nil
        isWaiting = false
        isLoading = false
        error = nil
    }
}
