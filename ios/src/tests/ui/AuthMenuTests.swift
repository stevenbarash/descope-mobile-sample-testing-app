import XCTest

final class AuthMenuTests: AppUITests {
    func testFlowButtonExists() {
        let flowButton = app.buttons[AccessibilityID.AuthMenu.flowButton]
        XCTAssertTrue(flowButton.waitForExistence(timeout: 2))
    }

    func testEnchantedLinkButtonExists() {
        let enchantedLinkButton = app.buttons[AccessibilityID.AuthMenu.enchantedLinkButton]
        XCTAssertTrue(enchantedLinkButton.waitForExistence(timeout: 2))
    }

    func testNavigateToEnchantedLink() {
        let enchantedLinkButton = app.buttons[AccessibilityID.AuthMenu.enchantedLinkButton]
        XCTAssertTrue(enchantedLinkButton.waitForExistence(timeout: 2))
        enchantedLinkButton.tap()

        let emailField = app.textFields[AccessibilityID.EnchantedLink.emailField]
        XCTAssertTrue(emailField.waitForExistence(timeout: 2))
    }

    func testNavigateToFlow() {
        let flowButton = app.buttons[AccessibilityID.AuthMenu.flowButton]
        XCTAssertTrue(flowButton.waitForExistence(timeout: 2))
        flowButton.tap()

        // FlowView should be displayed - verify navigation occurred
        XCTAssertTrue(app.navigationBars["Sign In"].waitForExistence(timeout: 2))
    }

    func testPasswordButtonExists() {
        let passwordButton = app.buttons[AccessibilityID.AuthMenu.passwordButton]
        XCTAssertTrue(passwordButton.waitForExistence(timeout: 2))
    }

    func testNavigateToPassword() {
        let passwordButton = app.buttons[AccessibilityID.AuthMenu.passwordButton]
        XCTAssertTrue(passwordButton.waitForExistence(timeout: 2))
        passwordButton.tap()

        let emailField = app.textFields[AccessibilityID.Password.emailField]
        XCTAssertTrue(emailField.waitForExistence(timeout: 2))
    }
}
