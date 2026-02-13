import XCTest

final class HomeViewTests: AppUITests {
    // Note: These tests require an authenticated state.
    // In a real test environment, you would either:
    // 1. Mock the authentication state via launch arguments
    // 2. Use a test account to authenticate before running tests
    // 3. Skip these tests when not authenticated

    func testSignOutButtonIdentifierIsCorrect() {
        // This test verifies the accessibility identifier constant is set correctly
        // The actual button may not be visible without authentication
        XCTAssertEqual(AccessibilityID.Home.signOutButton, "home.signOutButton")
    }

    func testUserNameIdentifierIsCorrect() {
        XCTAssertEqual(AccessibilityID.Home.userName, "home.userName")
    }

    func testUserEmailIdentifierIsCorrect() {
        XCTAssertEqual(AccessibilityID.Home.userEmail, "home.userEmail")
    }
}
