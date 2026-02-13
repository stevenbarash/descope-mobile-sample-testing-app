import SwiftUI
import DescopeKit

struct PasswordView: View {
    @Environment(AppState.self) private var appState
    @State private var email = ""
    @State private var password = ""
    @State private var isSignUp = false
    @State private var isLoading = false
    @State private var error: String?
    @FocusState private var focusedField: Field?

    private enum Field {
        case email, password
    }

    private var canSubmit: Bool {
        !email.isEmpty && !password.isEmpty && !isLoading
    }

    var body: some View {
        Form {
            credentialsSection
            actionSection
        }
        .navigationTitle("Password")
        .navigationBarTitleDisplayMode(.inline)
        .alert("Error", isPresented: .init(get: { error != nil }, set: { if !$0 { error = nil } })) {
            Button("OK") { error = nil }
        } message: {
            Text(error ?? "")
        }
    }

    private var credentialsSection: some View {
        Section {
            TextField("Email", text: $email)
                .textContentType(.emailAddress)
                .keyboardType(.emailAddress)
                .autocorrectionDisabled()
                .textInputAutocapitalization(.never)
                .focused($focusedField, equals: .email)
                .submitLabel(.next)
                .onSubmit { focusedField = .password }
                .accessibilityIdentifier(AccessibilityID.Password.emailField)

            SecureField("Password", text: $password)
                .textContentType(isSignUp ? .newPassword : .password)
                .focused($focusedField, equals: .password)
                .submitLabel(.go)
                .onSubmit { if canSubmit { submit() } }
                .accessibilityIdentifier(AccessibilityID.Password.passwordField)
        }
    }

    private var actionSection: some View {
        Section {
            Button {
                submit()
            } label: {
                HStack {
                    Text(isSignUp ? "Sign Up" : "Sign In")
                    Spacer()
                    if isLoading {
                        ProgressView()
                    }
                }
            }
            .disabled(!canSubmit)
            .accessibilityIdentifier(AccessibilityID.Password.submitButton)

            Button {
                isSignUp.toggle()
            } label: {
                Text(isSignUp ? "Already have an account? Sign In" : "Don't have an account? Sign Up")
                    .font(.footnote)
            }
            .accessibilityIdentifier(AccessibilityID.Password.toggleModeButton)
        } footer: {
            Text(isSignUp ? "Create a new account with your email and password." : "Sign in with your existing account.")
        }
    }

    private func submit() {
        isLoading = true
        Task {
            do throws(DescopeError) {
                let authResponse: AuthenticationResponse
                if isSignUp {
                    authResponse = try await Descope.password.signUp(loginId: email, password: password, details: nil)
                } else {
                    authResponse = try await Descope.password.signIn(loginId: email, password: password)
                }
                appState.setSession(DescopeSession(from: authResponse))
            } catch let err {
                error = err.errorDescription
            }
            isLoading = false
        }
    }
}
