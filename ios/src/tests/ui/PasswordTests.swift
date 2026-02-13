import XCTest

final class PasswordTests: AppUITests {
    override func setUp() async throws {
        try await super.setUp()
        // Navigate to PasswordView
        let passwordButton = app.buttons[AccessibilityID.AuthMenu.passwordButton]
        XCTAssertTrue(passwordButton.waitForExistence(timeout: 2))
        passwordButton.tap()
    }

    func testEmailFieldExists() {
        let emailField = app.textFields[AccessibilityID.Password.emailField]
        XCTAssertTrue(emailField.waitForExistence(timeout: 2))
    }

    func testPasswordFieldExists() {
        let passwordField = app.secureTextFields[AccessibilityID.Password.passwordField]
        XCTAssertTrue(passwordField.waitForExistence(timeout: 2))
    }

    func testSubmitButtonExists() {
        let submitButton = app.buttons[AccessibilityID.Password.submitButton]
        XCTAssertTrue(submitButton.waitForExistence(timeout: 2))
    }

    func testSubmitButtonDisabledWhenFieldsEmpty() {
        let submitButton = app.buttons[AccessibilityID.Password.submitButton]
        XCTAssertTrue(submitButton.waitForExistence(timeout: 2))
        XCTAssertFalse(submitButton.isEnabled)
    }

    func testSubmitButtonEnabledWhenFieldsFilled() {
        let emailField = app.textFields[AccessibilityID.Password.emailField]
        XCTAssertTrue(emailField.waitForExistence(timeout: 2))
        emailField.tap()
        emailField.typeText("test@example.com")

        let passwordField = app.secureTextFields[AccessibilityID.Password.passwordField]
        XCTAssertTrue(passwordField.waitForExistence(timeout: 2))
        passwordField.tap()
        passwordField.typeText("password123")

        let submitButton = app.buttons[AccessibilityID.Password.submitButton]
        XCTAssertTrue(submitButton.isEnabled)
    }

    func testToggleModeButton() {
        let toggleButton = app.buttons[AccessibilityID.Password.toggleModeButton]
        XCTAssertTrue(toggleButton.waitForExistence(timeout: 2))

        // Initially should show "Sign In" mode
        let submitButton = app.buttons[AccessibilityID.Password.submitButton]
        XCTAssertTrue(submitButton.label.contains("Sign In"))

        // Tap toggle to switch to Sign Up
        toggleButton.tap()
        XCTAssertTrue(submitButton.label.contains("Sign Up"))
    }

    func testCanNavigateBack() {
        app.navigationBars.buttons.element(boundBy: 0).tap()

        let flowButton = app.buttons[AccessibilityID.AuthMenu.flowButton]
        XCTAssertTrue(flowButton.waitForExistence(timeout: 2))
    }
}
