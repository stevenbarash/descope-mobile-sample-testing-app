import Foundation

enum AppConfig {
    /// Your Descope project ID from https://app.descope.com
    static let projectId = "YOUR_DESCOPE_PROJECT_ID"

    /// The Descope API base URL (usually "https://api.descope.com")
    static let baseURL = "https://api.descope.com"

    /// The flow ID to use for authentication (configured in Descope console)
    static let flowId = "sign-up-or-in"

    /// Your app's domain, used for deep links and enchanted link verification
    static let appDomain = "YOUR_APP_DOMAIN"
}
