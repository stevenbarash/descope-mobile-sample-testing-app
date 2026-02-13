import SwiftUI
import DescopeKit

struct HomeView: View {
    @Environment(AppState.self) private var appState

    private var user: DescopeUser? { appState.session?.user }

    private var pictureURL: URL? { user?.picture }

    var body: some View {
        NavigationStack {
            List {
                Section {
                    HStack(spacing: 16) {
                        profileImage
                            .frame(width: 64, height: 64)
                            .clipShape(Circle())

                        VStack(alignment: .leading, spacing: 4) {
                            Text(user?.name ?? "User")
                                .font(.headline)
                                .accessibilityIdentifier(AccessibilityID.Home.userName)
                            if let email = user?.email {
                                Text(email)
                                    .font(.subheadline)
                                    .foregroundStyle(.secondary)
                                    .accessibilityIdentifier(AccessibilityID.Home.userEmail)
                            }
                        }
                    }
                    .padding(.vertical, 8)
                }

                Section("Details") {
                    LabeledContent("User ID", value: user?.userId ?? "-")
                        .lineLimit(1)
                    if let phone = user?.phone, !phone.isEmpty {
                        LabeledContent("Phone", value: phone)
                    }
                    LabeledContent("Verified Email", value: user?.isVerifiedEmail == true ? "Yes" : "No")
                }

                Section {
                    Button(role: .destructive) {
                        appState.logout()
                    } label: {
                        HStack {
                            Spacer()
                            Text("Sign Out")
                            Spacer()
                        }
                    }
                    .accessibilityIdentifier(AccessibilityID.Home.signOutButton)
                }
            }
            .navigationTitle("Account")
        }
    }

    @ViewBuilder
    private var profileImage: some View {
        if let pictureURL {
            AsyncImage(url: pictureURL) { image in
                image.resizable().scaledToFill()
            } placeholder: {
                ProgressView()
            }
        } else {
            Image(systemName: "person.circle.fill")
                .resizable()
                .foregroundStyle(.secondary)
        }
    }
}
