import SwiftUI
import DescopeKit

struct PasskeyView: View {
    @Environment(AppState.self) private var appState
    @State private var email = ""
    @State private var isLoading = false
    @State private var error: String?
    @FocusState private var focusedField: Field?
    
    private enum Field {
        case email
    }
    
    var body: some View {
        Form {
            Section {
                TextField("Email", text: $email)
                    .textContentType(.emailAddress)
                    .keyboardType(.emailAddress)
                    .autocorrectionDisabled()
                    .textInputAutocapitalization(.never)
                    .focused($focusedField, equals: .email)
                    .submitLabel(.go)
                    .onSubmit { if !email.isEmpty && !isLoading { signInWithPasskey() } }
                    .accessibilityIdentifier("passkey.emailField")

                Button {
                    signInWithPasskey()
                } label: {
                    HStack {
                        Text("Sign in with Passkey")
                        Spacer()
                        if isLoading { ProgressView() }
                    }
                }
                .disabled(email.isEmpty || isLoading)
                .accessibilityIdentifier("passkey.signInButton")
                
                Button {
                    registerPasskey()
                } label: {
                    HStack {
                        Text("Sign Up with Passkey")
                        Spacer()
                        if isLoading { ProgressView() }
                    }
                }
                .disabled(email.isEmpty || isLoading)
                .accessibilityIdentifier("passkey.registerButton")
            }
        }
        .navigationTitle("Passkey")
        .navigationBarTitleDisplayMode(.inline)
        .alert("Error", isPresented: .init(get: { error != nil }, set: { if !$0 { error = nil } })) {
            Button("OK") { error = nil }
        } message: {
            Text(error ?? "")
        }
    }

    private func signInWithPasskey() {
        isLoading = true
        Task {
            do throws(DescopeError) {
                let authResponse = try await Descope.passkey.signIn(loginId: email, options: [])
                appState.setSession(DescopeSession(from: authResponse))
            } catch let err {
                error = err.description
            }
            isLoading = false
        }
    }

    private func registerPasskey() {
        isLoading = true
        Task {
            do throws(DescopeError) {
                let authResponse = try await Descope.passkey.signUp(loginId: email, details: nil)
                appState.setSession(DescopeSession(from: authResponse))
            } catch let err {
                error = err.description
            }
            isLoading = false
        }
    }
}
