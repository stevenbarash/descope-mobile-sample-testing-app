import Foundation
import DescopeKit

@MainActor
@Observable
final class AppState {
    private(set) var session: DescopeSession?

    init() {
        session = Descope.sessionManager.session
    }

    var isAuthenticated: Bool {
        guard let session else { return false }
        return !session.refreshToken.isExpired
    }

    func setSession(_ session: DescopeSession) {
        Descope.sessionManager.manageSession(session)
        self.session = session
    }

    func logout() {
        Descope.sessionManager.clearSession()
        session = nil
    }
}
