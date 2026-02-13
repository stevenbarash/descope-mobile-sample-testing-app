import XCTest

@MainActor
class AppUITests: XCTestCase, Sendable {
    var app: XCUIApplication!

    override func setUp() async throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }

    override func tearDown() async throws {
        app = nil
    }
}
