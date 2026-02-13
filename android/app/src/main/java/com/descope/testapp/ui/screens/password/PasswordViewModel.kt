package com.descope.testapp.ui.screens.password

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.descope.Descope
import com.descope.session.DescopeSession
import com.descope.testapp.data.LastUsedPreferences
import com.descope.testapp.ui.screens.AuthMode
import com.descope.types.PasswordPolicy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Represents the current action mode in the Password flow
 */
enum class PasswordAction(val displayName: String) {
    SIGN_IN("Sign In"),
    SIGN_UP("Sign Up"),
    REPLACE("Replace")
}

/**
 * ViewModel for the Password authentication screen.
 * Handles sign up, sign in, and password replacement.
 */
class PasswordViewModel(application: Application) : AndroidViewModel(application) {

    private val lastUsedPreferences = LastUsedPreferences(application)

    private val _mode = MutableStateFlow(AuthMode.SignIn)
    val mode: StateFlow<AuthMode> = _mode.asStateFlow()

    private val _currentAction = MutableStateFlow(PasswordAction.SIGN_IN)
    val currentAction: StateFlow<PasswordAction> = _currentAction.asStateFlow()

    private val _emailValue = MutableStateFlow("")
    val emailValue: StateFlow<String> = _emailValue.asStateFlow()

    private val _passwordValue = MutableStateFlow("")
    val passwordValue: StateFlow<String> = _passwordValue.asStateFlow()

    private val _oldPasswordValue = MutableStateFlow("")
    val oldPasswordValue: StateFlow<String> = _oldPasswordValue.asStateFlow()

    private val _newPasswordValue = MutableStateFlow("")
    val newPasswordValue: StateFlow<String> = _newPasswordValue.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    // Password policy from server
    private val _passwordPolicy = MutableStateFlow<PasswordPolicy?>(null)
    val passwordPolicy: StateFlow<PasswordPolicy?> = _passwordPolicy.asStateFlow()

    private val _isPolicyLoading = MutableStateFlow(false)
    val isPolicyLoading: StateFlow<Boolean> = _isPolicyLoading.asStateFlow()

    // Last used data for hint display
    private val _lastUsedEmail = MutableStateFlow<String?>(null)
    val lastUsedEmail: StateFlow<String?> = _lastUsedEmail.asStateFlow()

    init {
        loadLastUsedData()
        loadPasswordPolicy()
    }

    private fun loadLastUsedData() {
        val lastEmail = lastUsedPreferences.getLastUsedPasswordEmail()
        _lastUsedEmail.value = lastEmail
    }

    private fun loadPasswordPolicy() {
        viewModelScope.launch {
            _isPolicyLoading.value = true
            try {
                val policy = Descope.password.getPolicy()
                _passwordPolicy.value = policy
            } catch (e: Exception) {
                // Policy loading failed - continue without it
                _passwordPolicy.value = null
            } finally {
                _isPolicyLoading.value = false
            }
        }
    }

    fun setCurrentAction(action: PasswordAction) {
        _currentAction.value = action
        // Clear password fields when switching actions
        _passwordValue.value = ""
        _oldPasswordValue.value = ""
        _newPasswordValue.value = ""
        _error.value = null
    }

    fun setEmailValue(value: String) {
        _emailValue.value = value
        _error.value = null
    }

    fun setPasswordValue(value: String) {
        _passwordValue.value = value
        _error.value = null
    }

    fun setOldPasswordValue(value: String) {
        _oldPasswordValue.value = value
        _error.value = null
    }

    fun setNewPasswordValue(value: String) {
        _newPasswordValue.value = value
        _error.value = null
    }

    fun clearError() {
        _error.value = null
    }

    fun setMode(mode: AuthMode) {
        _mode.value = mode
        // In update mode, default to a simplified password update action
        if (mode == AuthMode.Update) {
            _currentAction.value = PasswordAction.REPLACE
        }
    }

    /**
     * Get the effective email value - either the user input or the last used value.
     */
    private fun getEffectiveEmail(): String? {
        val email = _emailValue.value.trim()
        if (email.isNotEmpty()) {
            return email
        }

        // Use last used value if available
        val lastEmail = _lastUsedEmail.value
        if (!lastEmail.isNullOrEmpty()) {
            return lastEmail
        }

        return null
    }

    /**
     * Validate password against policy
     */
    private fun validatePassword(password: String): String? {
        val policy = _passwordPolicy.value ?: return null

        if (password.length < policy.minLength) {
            return "Password must be at least ${policy.minLength} characters"
        }
        if (policy.lowercase && !password.any { it.isLowerCase() }) {
            return "Password must contain a lowercase letter"
        }
        if (policy.uppercase && !password.any { it.isUpperCase() }) {
            return "Password must contain an uppercase letter"
        }
        if (policy.number && !password.any { it.isDigit() }) {
            return "Password must contain a number"
        }
        if (policy.nonAlphanumeric && password.all { it.isLetterOrDigit() }) {
            return "Password must contain a special character"
        }

        return null
    }

    /**
     * Execute the current action (sign in, sign up, replace, or update)
     */
    fun executeAction() {
        if (_mode.value == AuthMode.Update) {
            // In update mode, use the update API
            updatePassword()
        } else {
            when (_currentAction.value) {
                PasswordAction.SIGN_IN -> signIn()
                PasswordAction.SIGN_UP -> signUp()
                PasswordAction.REPLACE -> replacePassword()
            }
        }
    }

    /**
     * Update password for an authenticated user using the SDK's update method.
     */
    private fun updatePassword() {
        val newPassword = _newPasswordValue.value
        if (newPassword.isEmpty()) {
            _error.value = "Please enter your new password"
            return
        }

        // Validate new password against policy
        val validationError = validatePassword(newPassword)
        if (validationError != null) {
            _error.value = validationError
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val session = Descope.sessionManager.session
                    ?: throw Exception("No active session")
                val loginId = session.user.loginIds.firstOrNull()
                    ?: throw Exception("No login ID available")

                Descope.password.update(loginId, newPassword, session.refreshJwt)

                // Refresh user to reflect the change
                val updatedUser = Descope.auth.me(session.refreshJwt)
                Descope.sessionManager.updateUser(updatedUser)

                // Save to prefs after successful update
                lastUsedPreferences.saveLastUsedPasswordEmail(loginId)
                _lastUsedEmail.value = loginId

                _isAuthenticated.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update password"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun signIn() {
        val effectiveEmail = getEffectiveEmail()
        if (effectiveEmail.isNullOrEmpty()) {
            _error.value = "Please enter your email address"
            return
        }

        val password = _passwordValue.value
        if (password.isEmpty()) {
            _error.value = "Please enter your password"
            return
        }

        // If using last used value, set it as the input value for display
        if (_emailValue.value.isEmpty()) {
            _emailValue.value = effectiveEmail
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val authResponse = Descope.password.signIn(effectiveEmail, password)

                // Save the last used email
                lastUsedPreferences.saveLastUsedPasswordEmail(effectiveEmail)
                _lastUsedEmail.value = effectiveEmail

                // Create and manage the session
                val session = DescopeSession(authResponse)
                Descope.sessionManager.manageSession(session)

                _isAuthenticated.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to sign in"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun signUp() {
        val effectiveEmail = getEffectiveEmail()
        if (effectiveEmail.isNullOrEmpty()) {
            _error.value = "Please enter your email address"
            return
        }

        val password = _passwordValue.value
        if (password.isEmpty()) {
            _error.value = "Please enter your password"
            return
        }

        // Validate password against policy
        val validationError = validatePassword(password)
        if (validationError != null) {
            _error.value = validationError
            return
        }

        // If using last used value, set it as the input value for display
        if (_emailValue.value.isEmpty()) {
            _emailValue.value = effectiveEmail
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val authResponse = Descope.password.signUp(effectiveEmail, password)

                // Save the last used email
                lastUsedPreferences.saveLastUsedPasswordEmail(effectiveEmail)
                _lastUsedEmail.value = effectiveEmail

                // Create and manage the session
                val session = DescopeSession(authResponse)
                Descope.sessionManager.manageSession(session)

                _isAuthenticated.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to sign up"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun replacePassword() {
        val effectiveEmail = getEffectiveEmail()
        if (effectiveEmail.isNullOrEmpty()) {
            _error.value = "Please enter your email address"
            return
        }

        val oldPassword = _oldPasswordValue.value
        if (oldPassword.isEmpty()) {
            _error.value = "Please enter your current password"
            return
        }

        val newPassword = _newPasswordValue.value
        if (newPassword.isEmpty()) {
            _error.value = "Please enter your new password"
            return
        }

        // Validate new password against policy
        val validationError = validatePassword(newPassword)
        if (validationError != null) {
            _error.value = validationError
            return
        }

        // If using last used value, set it as the input value for display
        if (_emailValue.value.isEmpty()) {
            _emailValue.value = effectiveEmail
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val authResponse = Descope.password.replace(effectiveEmail, oldPassword, newPassword)

                // Save the last used email
                lastUsedPreferences.saveLastUsedPasswordEmail(effectiveEmail)
                _lastUsedEmail.value = effectiveEmail

                // Create and manage the session
                val session = DescopeSession(authResponse)
                Descope.sessionManager.manageSession(session)

                _isAuthenticated.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to change password"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
