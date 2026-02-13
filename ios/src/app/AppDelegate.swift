import UIKit
import DescopeKit

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        Descope.setup(projectId: AppConfig.projectId) { config in
            config.baseURL = AppConfig.baseURL
            config.logger = .debugLogger
        }
        return true
    }
}

