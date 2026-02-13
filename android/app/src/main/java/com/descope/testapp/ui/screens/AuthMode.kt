package com.descope.testapp.ui.screens

/**
 * Represents the mode in which an authentication screen operates.
 */
enum class AuthMode {
    /**
     * Standard sign-in/sign-up mode for authenticating users.
     */
    SignIn,

    /**
     * Update mode for adding/updating authentication methods for an existing user.
     * Requires an active session.
     */
    Update;

    companion object {
        fun fromString(value: String?): AuthMode {
            return when (value?.lowercase()) {
                "update" -> Update
                else -> SignIn
            }
        }
    }
}
