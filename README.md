# Descope Sample App

A complete mobile authentication sample app demonstrating [Descope](https://www.descope.com) SDK capabilities on iOS, Android, and Web.

## Setup

Before running the app, replace the placeholder values with your Descope project configuration. Get these from the [Descope Console](https://app.descope.com).

### Configuration

| Location | Value | Description |
|----------|-------|-------------|
| **iOS** `ios/src/app/AppConfig.swift` | `projectId` | Your Descope project ID |
| | `baseURL` | Descope API URL (default: `https://api.descope.com`) |
| | `flowId` | Flow ID configured in Descope console |
| | `appDomain` | Your app's domain for deep links |
| **Android** `android/app/build.gradle.kts` | `descopeProjectId` | Your Descope project ID |
| | `descopeBaseUrl` | Descope API URL (default: `https://api.descope.com`) |
| | `deepLinkHost` | Your app's domain for deep links |
| | `descopeFlowIds` | Comma-separated flow IDs for login |
| | `descopeAuthenticatedFlowIds` | Comma-separated flow IDs for authenticated actions |
| **Server** `server/public/flow/preset.js` | `projectId` | Your Descope project ID |
| **iOS** `ios/project.yml` | `DEVELOPMENT_TEAM` | Your Apple Team ID |
| | `PRODUCT_BUNDLE_IDENTIFIER` | Your app's bundle ID |
| **iOS** `ios/etc/App.entitlements` | `applinks:` | Your app's domain |
| **Android** `android/app/build.gradle.kts` | `applicationId` | Your app's package name |
| | Signing config | Your keystore file, passwords, and key alias |
| **Server** `.well-known/assetlinks.json` | `package_name` | Your Android package name |
| | `sha256_cert_fingerprints` | Your signing certificate SHA-256 fingerprint |
| **Server** `.well-known/apple-app-site-association` | App IDs | `YOUR_APPLE_TEAM_ID.your.bundle.id` |

### Deep Linking

For enchanted links and OAuth callbacks to work, you need to:

1. **Deploy the server** to a domain you control
2. **Update the `.well-known` files** with your app's identifiers
3. **Set the domain** in `AppConfig.swift` (iOS) and `build.gradle.kts` (Android)

## Components

- **[server/](./server/)** — Web server with flow testing page and enchanted link verification
- **[ios/](./ios/)** — iOS SwiftUI app with Flow and Enchanted Link auth examples
- **[android/](./android/)** — Android Compose app with a complete set of authentication methods
- **[terraform/](./terraform/)** — Terraform plan to provision a pre-configured Descope project

## Quick Start

```bash
# Server
make server-install      # Install dependencies
make server-dev          # Run dev server

# iOS
make ios-open            # Generate project and open in Xcode

# Android
make android-open        # Open the project in Android Studio
make android-install     # Install the app on a device or emulator
```

See component READMEs for detailed documentation.
