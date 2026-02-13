import Foundation

enum AccessibilityID {
    enum AuthMenu {
        static let flowButton = "authMenu.flowButton"
        static let enchantedLinkButton = "authMenu.enchantedLinkButton"
        static let magicLinkButton = "authMenu.magicLinkButton"
        static let passwordButton = "authMenu.passwordButton"
    }

    enum EnchantedLink {
        static let emailField = "enchantedLink.emailField"
        static let sendButton = "enchantedLink.sendButton"
        static let linkIdText = "enchantedLink.linkId"
        static let cancelButton = "enchantedLink.cancelButton"
    }

    enum MagicLink {
        static let emailField = "magicLink.emailField"
        static let sendButton = "magicLink.sendButton"
        static let waitingView = "magicLink.waitingView"
        static let cancelButton = "magicLink.cancelButton"
    }

    enum Password {
        static let emailField = "password.emailField"
        static let passwordField = "password.passwordField"
        static let submitButton = "password.submitButton"
        static let toggleModeButton = "password.toggleModeButton"
    }

    enum Home {
        static let userName = "home.userName"
        static let userEmail = "home.userEmail"
        static let signOutButton = "home.signOutButton"
    }
}
