import SwiftUI

struct AuthMenuView: View {
    var body: some View {
        NavigationStack {
            VStack(spacing: 32) {
                Spacer()

                VStack(spacing: 12) {
                    Image(systemName: "person.badge.key.fill")
                        .font(.system(size: 64))
                        .foregroundStyle(.tint)

                    Text("Descope Sample")
                        .font(.largeTitle.bold())

                    Text("Choose an authentication method")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }

                Spacer()

                VStack(spacing: 16) {
                    NavigationLink {
                        FlowView()
                    } label: {
                        Label("Sign in with Flow", systemImage: "rectangle.stack.person.crop")
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.borderedProminent)
                    .controlSize(.large)
                    .accessibilityIdentifier(AccessibilityID.AuthMenu.flowButton)

                    NavigationLink {
                        EnchantedLinkView()
                    } label: {
                        Label("Sign in with Enchanted Link", systemImage: "wand.and.stars")
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.bordered)
                    .controlSize(.large)
                    .accessibilityIdentifier(AccessibilityID.AuthMenu.enchantedLinkButton)

                    NavigationLink {
                        MagicLinkView()
                    } label: {
                        Label("Sign in with Magic Link", systemImage: "link")
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.bordered)
                    .controlSize(.large)
                    .accessibilityIdentifier(AccessibilityID.AuthMenu.magicLinkButton)

                    NavigationLink {
                        PasskeyView()
                    } label: {
                        Label("Sign in with Passkey", systemImage: "faceid")
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.bordered)
                    .controlSize(.large)
                    .accessibilityIdentifier("authMenu.passkeyButton")

                    NavigationLink {
                        PasswordView()
                    } label: {
                        Label("Sign in with Password", systemImage: "key.fill")
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.bordered)
                    .controlSize(.large)
                    .accessibilityIdentifier(AccessibilityID.AuthMenu.passwordButton)
                }

                Spacer()
            }
            .padding(24)
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

