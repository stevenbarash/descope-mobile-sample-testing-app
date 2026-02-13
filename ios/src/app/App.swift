import SwiftUI
import DescopeKit

@main
struct DescopeTestApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    @State private var appState = AppState()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environment(appState)
                .onOpenURL { url in
                    print("[App] Universal Link received: \(url)")
                    Descope.handleURL(url)
                }
        }
    }
}
