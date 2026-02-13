package com.descope.testapp.ui.screens.passkey

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.descope.Descope
import com.descope.session.DescopeSession
import com.descope.testapp.data.LastUsedPreferences
import com.descope.testapp.ui.screens.AuthMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PasskeyViewModel(application: Application) : AndroidViewModel(application) {
    private val lastUsedPreferences = LastUsedPreferences(application)

    private val _mode = MutableStateFlow(AuthMode.SignIn)
    val mode: StateFlow<AuthMode> = _mode.asStateFlow()

    private val _loginId = MutableStateFlow("")
    val loginId: StateFlow<String> = _loginId.asStateFlow()

    // Last used data for hint display
    private val _lastUsedLoginId = MutableStateFlow<String?>(null)
    val lastUsedLoginId: StateFlow<String?> = _lastUsedLoginId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    init {
        // Load last used login ID
        _lastUsedLoginId.value = lastUsedPreferences.getLastUsedPasskeyLoginId()
    }

    fun setMode(mode: AuthMode) {
        _mode.value = mode
        if (mode == AuthMode.Update) {
            // Prefill from signed-in user in update mode
            Descope.sessionManager.session?.user?.loginIds?.firstOrNull()?.let { loginId ->
                _loginId.value = loginId
            }
        }
    }

    fun onLoginIdChanged(newLoginId: String) {
        _loginId.value = newLoginId
        _error.value = null
    }

    fun authenticate(context: Context) {
        // In update mode, always use the current user's login ID
        val loginIdToUse = if (_mode.value == AuthMode.Update) {
            Descope.sessionManager.session?.user?.loginIds?.firstOrNull()
                ?: run {
                    _error.value = "No active session"
                    return
                }
        } else {
            val inputLoginId = _loginId.value.trim()
            if (inputLoginId.isNotEmpty()) {
                inputLoginId
            } else {
                _lastUsedLoginId.value
            }
        }

        if (loginIdToUse.isNullOrEmpty()) {
            _error.value = "Please enter your Login ID"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    if (_mode.value == AuthMode.Update) {
                        // Add passkey to existing user
                        val session = Descope.sessionManager.session
                            ?: throw Exception("No active session")
                        Descope.passkey.add(context, loginIdToUse, session.refreshJwt)
                        // Refresh user to reflect the change
                        val updatedUser = Descope.auth.me(session.refreshJwt)
                        Descope.sessionManager.updateUser(updatedUser)
                        // Save to prefs after successful update
                        lastUsedPreferences.saveLastUsedPasskeyLoginId(loginIdToUse)
                        _lastUsedLoginId.value = loginIdToUse
                        _isAuthenticated.value = true
                    } else {
                        // Sign-in mode
                        val response = Descope.passkey.signUpOrIn(context, loginIdToUse, null)
                        val session = DescopeSession(response)
                        Descope.sessionManager.manageSession(session)
                        lastUsedPreferences.saveLastUsedPasskeyLoginId(loginIdToUse)
                        _lastUsedLoginId.value = loginIdToUse
                        _isAuthenticated.value = true
                    }
                } else {
                    _error.value = "Passkeys are not supported on this device (requires Android 9+)"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Authentication failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
