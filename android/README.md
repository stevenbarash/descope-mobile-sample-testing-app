# Android App

Android Compose app demonstrating a complete set of Descope authentication methods.

## Setup

### Prerequisites

- Android Studio (latest stable version)
- Android SDK 36 (or as configured)

### Configuration

1. **Clone the repository** and open the `android` folder in Android Studio.

2. **Descope Configuration** — Edit the values at the top of `app/build.gradle.kts`:

   - `descopeProjectId` — Your Descope project ID
   - `descopeBaseUrl` — The Descope API base URL (default: `https://api.descope.com`)
   - `deepLinkHost` — Your app's domain for deep link callbacks
   - `descopeFlowIds` — Comma-separated list of flow IDs for unauthenticated users
   - `descopeAuthenticatedFlowIds` — Comma-separated list of flow IDs for authenticated users

3. **App Identity** — Update the `namespace` and `applicationId` in `app/build.gradle.kts` with your own package name.

4. **Signing** — Configure the signing config in `app/build.gradle.kts` with your own keystore. Update the `.well-known/assetlinks.json` on your server with your signing certificate's SHA-256 fingerprint.

5. **Sync Gradle** and build the project.

## Makefile

From the repository root: `make android-build` to compile a debug APK,
`make android-test` to run unit tests, or `make android-open` to open the project in Android Studio.
Use `make android-install` to deploy the debug APK to a connected device or emulator.

## Architecture

```
app/src/main/java/com/descope/testapp/
├── DescopeTestApp.kt          # Application class, SDK initialization
├── MainActivity.kt            # Single activity, deep link handling
├── data/
│   └── LastUsedPreferences.kt # Persistence for last used auth data
└── ui/
    ├── App.kt                 # Root composable, navigation graph
    ├── components/            # Reusable UI components
    ├── theme/                 # Material3 theme, colors, typography
    └── screens/
        ├── login/             # Login UI with Flow + API options
        ├── home/              # User info display, session management
        ├── flow/              # Descope Flow WebView
        ├── otp/               # OTP authentication
        ├── magiclink/         # Magic Link authentication
        ├── enchantedlink/     # Enchanted Link authentication
        ├── password/          # Password authentication
        ├── passkey/           # Passkey (WebAuthn) authentication
        ├── totp/              # TOTP authenticator authentication
        ├── oauthnative/       # Native OAuth (Google) authentication
        └── oauthweb/          # Web-based OAuth authentication
```

## Usage

### Login Screen

Two authentication options:

1. **Sign in with Flow** — Launches the Descope Flow web-based authentication
2. **API Authentication** — Direct API methods: OTP, Magic Link, Enchanted Link, OAuth, Password, Passkey, TOTP

### Home Screen

After successful authentication:

- Displays user information (ID, name, email, phone, authentication methods)
- **Run Authenticated Flow** — Launches a Descope Flow with the current session
- **Update User via API** — Methods to update user data (OTP, Magic Link, Enchanted Link, Password, Passkey, TOTP)
- **Refresh Menu** — Refresh session or user data
- **Log out** — Clears the session and returns to login

## Deep Links

The app handles the following deep link paths:

| Path | Scheme | Purpose |
|------|--------|---------|
| `/flow` | `https://` | Flow authentication callback (App Link) |
| `/flow` | `descope://` | Flow authentication callback (Custom scheme fallback) |
| `/magiclink` | `https://` | Magic Link verification |
| `/oauth` | `https://` | OAuth/Social login callback |

## Troubleshooting

### Build Issues

1. **Gradle sync fails** — Ensure you have JDK 17+ configured in Android Studio
2. **Compose errors** — Clean build (`./gradlew clean`) and rebuild

### Deep Link Issues

1. **App Links not working** — Verify `assetlinks.json` is hosted correctly on your domain
2. **Use custom scheme fallback** — `descope://` scheme doesn't require domain verification

### SDK Issues

1. **Initialization fails** — Check that `descopeProjectId` is set correctly in `build.gradle.kts`
2. **Session not persisting** — Ensure app has proper storage permissions
