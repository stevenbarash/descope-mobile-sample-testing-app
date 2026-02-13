import SwiftUI
import DescopeKit

struct EnchantedLinkView: View {
    @Environment(AppState.self) private var appState
    @State private var email = ""
    @State private var linkId: String?
    @State private var maskedEmail: String?
    @State private var isLoading = false
    @State private var error: String?
    @State private var pollingTask: Task<Void, Never>?

    private let redirectURL = "https://\(AppConfig.appDomain)/verify/\(AppConfig.projectId)"

    var body: some View {
        Form {
            if let linkId {
                waitingSection(linkId: linkId)
            } else {
                emailSection
            }
        }
        .navigationTitle("Enchanted Link")
        .navigationBarTitleDisplayMode(.inline)
        .onDisappear { pollingTask?.cancel() }
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
                .accessibilityIdentifier(AccessibilityID.EnchantedLink.emailField)

            Button {
                sendLink()
            } label: {
                HStack {
                    Text("Send Enchanted Link")
                    Spacer()
                    if isLoading {
                        ProgressView()
                    }
                }
            }
            .disabled(email.isEmpty || isLoading)
            .accessibilityIdentifier(AccessibilityID.EnchantedLink.sendButton)
        } footer: {
            Text("We'll send an enchanted link to your email. Click the link that matches the code shown.")
        }
    }

    private func waitingSection(linkId: String) -> some View {
        Section {
            VStack(spacing: 20) {
                ProgressView()
                    .scaleEffect(1.5)
                    .padding()

                Text("Check your email")
                    .font(.headline)

                if let maskedEmail {
                    Text("Sent to \(maskedEmail)")
                        .foregroundStyle(.secondary)
                }

                Text("Click the link that shows")
                    .foregroundStyle(.secondary)

                Text(linkId)
                    .font(.system(.largeTitle, design: .rounded, weight: .bold))
                    .foregroundStyle(.tint)
                    .padding()
                    .background(.tint.opacity(0.1), in: RoundedRectangle(cornerRadius: 12))
                    .accessibilityIdentifier(AccessibilityID.EnchantedLink.linkIdText)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 32)

            Button("Cancel", role: .cancel) {
                reset()
            }
            .frame(maxWidth: .infinity)
            .accessibilityIdentifier(AccessibilityID.EnchantedLink.cancelButton)
        }
    }

    private func sendLink() {
        isLoading = true
        pollingTask = Task {
            do throws(DescopeError) {
                let response = try await Descope.enchantedLink.signUpOrIn(loginId: email, redirectURL: redirectURL, options: [])
                linkId = response.linkId
                maskedEmail = response.maskedEmail
                let authResponse = try await Descope.enchantedLink.pollForSession(pendingRef: response.pendingRef, timeout: nil)
                appState.setSession(DescopeSession(from: authResponse))
            } catch let err  {
                error = err.description
            }
            isLoading = false
        }
    }

    private func reset() {
        pollingTask?.cancel()
        pollingTask = nil
        linkId = nil
        maskedEmail = nil
        isLoading = false
        error = nil
    }
}
