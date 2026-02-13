import XCTest

final class EnchantedLinkTests: AppUITests {
    override func setUp() async throws {
        try await super.setUp()
        // Navigate to EnchantedLinkView
        let enchantedLinkButton = app.buttons[AccessibilityID.AuthMenu.enchantedLinkButton]
        XCTAssertTrue(enchantedLinkButton.waitForExistence(timeout: 2))
        enchantedLinkButton.tap()
    }

    func testEmailFieldExists() {
        let emailField = app.textFields[AccessibilityID.EnchantedLink.emailField]
        XCTAssertTrue(emailField.waitForExistence(timeout: 2))
    }

    func testSendButtonExists() {
        let sendButton = app.buttons[AccessibilityID.EnchantedLink.sendButton]
        XCTAssertTrue(sendButton.waitForExistence(timeout: 2))
    }

    func testSendButtonDisabledWhenEmailEmpty() {
        let emailField = app.textFields[AccessibilityID.EnchantedLink.emailField]
        XCTAssertTrue(emailField.waitForExistence(timeout: 2))

        // Clear the email field if it has any text
        emailField.tap()
        if let text = emailField.value as? String, !text.isEmpty {
            emailField.clearText()
        }

        let sendButton = app.buttons[AccessibilityID.EnchantedLink.sendButton]
        XCTAssertTrue(sendButton.waitForExistence(timeout: 2))
        XCTAssertFalse(sendButton.isEnabled)
    }

    func testSendButtonEnabledWhenEmailEntered() {
        let emailField = app.textFields[AccessibilityID.EnchantedLink.emailField]
        XCTAssertTrue(emailField.waitForExistence(timeout: 2))

        emailField.tap()
        emailField.typeText("test@example.com")

        let sendButton = app.buttons[AccessibilityID.EnchantedLink.sendButton]
        XCTAssertTrue(sendButton.waitForExistence(timeout: 2))
        XCTAssertTrue(sendButton.isEnabled)
    }

    func testCanNavigateBack() {
        // Verify we can navigate back to auth menu
        app.navigationBars.buttons.element(boundBy: 0).tap()

        let flowButton = app.buttons[AccessibilityID.AuthMenu.flowButton]
        XCTAssertTrue(flowButton.waitForExistence(timeout: 2))
    }
}

extension XCUIElement {
    func clearText() {
        guard let stringValue = self.value as? String else { return }
        let deleteString = String(repeating: XCUIKeyboardKey.delete.rawValue, count: stringValue.count)
        self.typeText(deleteString)
    }
}
