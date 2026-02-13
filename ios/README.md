# iOS App

SwiftUI app demonstrating Descope authentication flows.

## Requirements

- Xcode 16+
- iOS 18.0+ deployment target
- [xcodegen](https://github.com/yonaskolb/XcodeGen) (`brew install xcodegen`)

## Setup

1. **Configure your project** — Edit `src/app/AppConfig.swift`:
   - `projectId` — Your Descope project ID
   - `baseURL` — Descope API URL (default: `https://api.descope.com`)
   - `flowId` — The flow ID to use for authentication
   - `appDomain` — Your app's domain for deep links and enchanted link verification

2. **Set your Apple Team ID** — In `project.yml`, replace `YOUR_APPLE_TEAM_ID` with your Apple Developer Team ID (find it at [Apple Developer](https://developer.apple.com/account) → Membership)

3. **Set your bundle identifier** — In `project.yml`, replace `com.example.myapp` with your app's bundle ID

4. **Update entitlements** — In `etc/App.entitlements`, replace the placeholder domains with your Descope project's domain (e.g. `your-project.descope.io`):
   - `webcredentials:<your-domain>` — **Required for passkeys/biometrics**
   - `applinks:<your-domain>` — Required for deep links and enchanted link verification

   The `webcredentials` associated domain tells iOS which domain your app is allowed to use for passkey operations. Its value must match the top-level domain configured in your Descope project. See [Apple's Supporting Passkeys guide](https://developer.apple.com/documentation/authenticationservices/supporting-passkeys) and the [Descope passkeys mobile SDK docs](https://docs.descope.com/auth-methods/passkeys/with-sdks/mobile) for more details.

5. **Configure passkeys (if using passkeys/biometrics)** — In the [Descope console](https://app.descope.com/settings/authentication/webauthn), enable passkeys and configure:
   - **Top-level domain** — Must match the `webcredentials` domain in your entitlements
   - **Apple Team ID** — Your 10-character Team ID (find it at [Apple Developer](https://developer.apple.com/account) → Membership)
   - **Bundle ID** — Must match the `PRODUCT_BUNDLE_IDENTIFIER` in `project.yml`

   This ensures Descope serves the `apple-app-site-association` (AASA) file at `https://<your-domain>/.well-known/apple-app-site-association`, which iOS requires to verify the app-domain association for passkeys. Without this, passkey operations will fail with an `AuthorizationError` (code 1004).

   You can verify the AASA file is being served correctly:
   ```bash
   curl -sL "https://<your-domain>/.well-known/apple-app-site-association"
   ```
   The response should include your app identifier (`<TeamID>.<BundleID>`) under `webcredentials.apps`.

   For full details, see the [Descope passkeys documentation](https://docs.descope.com/auth-methods/passkeys/with-sdks/mobile).

6. **Generate and open the project**:
   ```bash
   # From repo root
   make ios-open

   # Or directly
   cd ios
   xcodegen generate
   open App.xcodeproj
   ```

## Features

- **Flow Auth** — Uses Descope Flow web component via `DescopeFlowViewController`
- **Enchanted Link** — Direct API example with email input and polling

## Structure

```
ios/
├── project.yml          # xcodegen configuration
├── etc/                 # Info.plist, entitlements
├── src/
│   ├── app/             # App entry point, configuration
│   ├── views/           # SwiftUI views
│   └── tests/           # UI tests
└── res/                 # Assets.xcassets
```
