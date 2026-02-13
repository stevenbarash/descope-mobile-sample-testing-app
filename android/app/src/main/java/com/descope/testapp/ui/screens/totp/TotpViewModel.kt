package com.descope.testapp.ui.screens.totp

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.descope.Descope
import com.descope.session.DescopeSession
import com.descope.testapp.data.LastUsedPreferences
import com.descope.testapp.ui.screens.AuthMode
import com.descope.types.TotpResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TotpStep {
    INPUT,
    SETUP,
    VERIFY
}

class TotpViewModel(application: Application) : AndroidViewModel(application) {
    private val lastUsedPreferences = LastUsedPreferences(application)

    private val _mode = MutableStateFlow(AuthMode.SignIn)
    val mode: StateFlow<AuthMode> = _mode.asStateFlow()

    private val _loginId = MutableStateFlow("")
    val loginId: StateFlow<String> = _loginId.asStateFlow()

    private val _lastUsedLoginId = MutableStateFlow<String?>(null)
    val lastUsedLoginId: StateFlow<String?> = _lastUsedLoginId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _currentStep = MutableStateFlow(TotpStep.INPUT)
    val currentStep: StateFlow<TotpStep> = _currentStep.asStateFlow()

    private val _totpResponse = MutableStateFlow<TotpResponse?>(null)
    val totpResponse: StateFlow<TotpResponse?> = _totpResponse.asStateFlow()

    private val _qrCodeBitmap = MutableStateFlow<Bitmap?>(null)
    val qrCodeBitmap: StateFlow<Bitmap?> = _qrCodeBitmap.asStateFlow()

    private val _totpCode = MutableStateFlow("")
    val totpCode: StateFlow<String> = _totpCode.asStateFlow()

    init {
        _lastUsedLoginId.value = lastUsedPreferences.getLastUsedTotpLoginId()
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

    fun setTotpCode(code: String) {
        _totpCode.value = code
    }

    fun goBackToInput() {
        _currentStep.value = TotpStep.INPUT
        _error.value = null
        _totpCode.value = ""
    }

    fun signUp() {
        val inputLoginId = _loginId.value.trim()
        val effectiveLoginId = if (inputLoginId.isNotEmpty()) inputLoginId else _lastUsedLoginId.value

        // In update mode, use current user's login ID if not provided
        val loginIdToUse = if (_mode.value == AuthMode.Update && effectiveLoginId.isNullOrEmpty()) {
            Descope.sessionManager.session?.user?.loginIds?.firstOrNull()
        } else {
            effectiveLoginId
        }

        if (loginIdToUse.isNullOrEmpty()) {
            _error.value = "Please enter your Login ID"
            return
        }
        if (inputLoginId.isEmpty()) _loginId.value = loginIdToUse

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val response = if (_mode.value == AuthMode.Update) {
                    // Update mode - add TOTP to existing user
                    val session = Descope.sessionManager.session
                        ?: throw Exception("No active session")
                    Descope.totp.update(loginIdToUse, session.refreshJwt)
                } else {
                    // Sign-up mode
                    Descope.totp.signUp(loginIdToUse)
                }
                _totpResponse.value = response

                // Convert bytes to Bitmap (Response comes as Base64 encoded bytes)
                val decodedBytes = android.util.Base64.decode(response.image, android.util.Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                _qrCodeBitmap.value = bitmap

                _currentStep.value = TotpStep.SETUP
                lastUsedPreferences.saveLastUsedTotpLoginId(loginIdToUse)
                _lastUsedLoginId.value = loginIdToUse
            } catch (e: Exception) {
                _error.value = e.message ?: "Sign up failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun prepareForVerify() {
        val inputLoginId = _loginId.value.trim()
        val effectiveLoginId = if (inputLoginId.isNotEmpty()) inputLoginId else _lastUsedLoginId.value

        if (effectiveLoginId.isNullOrEmpty()) {
            _error.value = "Please enter your Login ID"
            return
        }
        if (inputLoginId.isEmpty()) _loginId.value = effectiveLoginId

        lastUsedPreferences.saveLastUsedTotpLoginId(effectiveLoginId)
        _lastUsedLoginId.value = effectiveLoginId
        _currentStep.value = TotpStep.VERIFY
        _error.value = null
    }

    fun goToVerifyFromSetup() {
        _currentStep.value = TotpStep.VERIFY
        _error.value = null
    }

    fun verify() {
        val inputLoginId = _loginId.value.trim()
        val code = _totpCode.value

        if (inputLoginId.isEmpty()) {
             _error.value = "Login ID missing"
             return
        }
        if (code.length != 6) { return }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                if (_mode.value == AuthMode.Update) {
                    // In update mode, just signal success after TOTP is set up
                    // The verify is optional for update - user can skip to confirm TOTP was added
                    val response = Descope.totp.verify(inputLoginId, code)
                    // Save to prefs after successful update
                    lastUsedPreferences.saveLastUsedTotpLoginId(inputLoginId)
                    _lastUsedLoginId.value = inputLoginId
                    _isAuthenticated.value = true
                } else {
                    val response = Descope.totp.verify(inputLoginId, code)
                    val session = DescopeSession(response)
                    Descope.sessionManager.manageSession(session)
                    _isAuthenticated.value = true
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Verification failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * In update mode, allow completing without verification (TOTP is already added)
     */
    fun completeUpdate() {
        if (_mode.value == AuthMode.Update) {
            viewModelScope.launch {
                // Refresh user to reflect the change
                Descope.sessionManager.session?.let { session ->
                    try {
                        val updatedUser = Descope.auth.me(session.refreshJwt)
                        Descope.sessionManager.updateUser(updatedUser)
                    } catch (_: Exception) {
                        // Ignore refresh errors, TOTP was still added
                    }
                }
                // Save to prefs before completing
                val loginId = _loginId.value.trim()
                if (loginId.isNotEmpty()) {
                    lastUsedPreferences.saveLastUsedTotpLoginId(loginId)
                    _lastUsedLoginId.value = loginId
                }
                _isAuthenticated.value = true
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
