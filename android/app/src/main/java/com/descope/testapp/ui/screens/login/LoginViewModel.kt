package com.descope.testapp.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.descope.Descope
import com.descope.testapp.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Available API authentication methods that can be selected from the bottom sheet.
 */
enum class AuthMethod(val displayName: String) {
    OTP("OTP (One-Time Password)"),
    MAGIC_LINK("Magic Link"),
    ENCHANTED_LINK("Enchanted Link"),
    OAUTH("OAuth / Social Login"),
    OAUTH_NATIVE("Native OAuth (Google)"),
    PASSWORD("Password"),
    PASSKEY("Passkey"),
    TOTP("TOTP (Authenticator App)"),
}

/**
 * ViewModel for the Login screen.
 * Manages authentication state and provides methods for various auth flows.
 */
class LoginViewModel : ViewModel() {

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _showApiOptions = MutableStateFlow(false)
    val showApiOptions: StateFlow<Boolean> = _showApiOptions.asStateFlow()

    private val _showFlowOptions = MutableStateFlow(false)
    val showFlowOptions: StateFlow<Boolean> = _showFlowOptions.asStateFlow()

    private val _navigateToOtp = MutableStateFlow(false)
    val navigateToOtp: StateFlow<Boolean> = _navigateToOtp.asStateFlow()

    private val _navigateToOAuthNative = MutableStateFlow(false)
    val navigateToOAuthNative: StateFlow<Boolean> = _navigateToOAuthNative.asStateFlow()

    private val _navigateToOAuthWeb = MutableStateFlow(false)
    val navigateToOAuthWeb: StateFlow<Boolean> = _navigateToOAuthWeb.asStateFlow()

    private val _navigateToMagicLink = MutableStateFlow(false)
    val navigateToMagicLink: StateFlow<Boolean> = _navigateToMagicLink.asStateFlow()

    private val _navigateToEnchantedLink = MutableStateFlow(false)
    val navigateToEnchantedLink: StateFlow<Boolean> = _navigateToEnchantedLink.asStateFlow()

    private val _navigateToPassword = MutableStateFlow(false)
    val navigateToPassword: StateFlow<Boolean> = _navigateToPassword.asStateFlow()

    private val _navigateToPasskey = MutableStateFlow(false)
    val navigateToPasskey: StateFlow<Boolean> = _navigateToPasskey.asStateFlow()

    private val _navigateToTotp = MutableStateFlow(false)
    val navigateToTotp: StateFlow<Boolean> = _navigateToTotp.asStateFlow()

    val availableFlows: List<String> = BuildConfig.DESCOPE_FLOW_IDS
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    init {
        // Check if user is already authenticated
        checkExistingSession()
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            val session = Descope.sessionManager.session
            _isAuthenticated.value = session != null
        }
    }

    fun showApiOptionsSheet() {
        _showApiOptions.value = true
    }

    fun hideApiOptionsSheet() {
        _showApiOptions.value = false
    }

    fun showFlowOptionsSheet() {
        _showFlowOptions.value = true
    }

    fun hideFlowOptionsSheet() {
        _showFlowOptions.value = false
    }

    fun onFlowSelected(flowId: String, onNavigateToFlow: (String) -> Unit) {
        hideFlowOptionsSheet()
        onNavigateToFlow(flowId)
    }

    fun onAuthMethodSelected(method: AuthMethod) {
        hideApiOptionsSheet()

        when (method) {
            AuthMethod.OTP -> startOtpAuth()
            AuthMethod.MAGIC_LINK -> startMagicLinkAuth()
            AuthMethod.ENCHANTED_LINK -> startEnchantedLinkAuth()
            AuthMethod.OAUTH -> startOAuthAuth()
            AuthMethod.OAUTH_NATIVE -> startOAuthNativeAuth()
            AuthMethod.PASSWORD -> startPasswordAuth()
            AuthMethod.PASSKEY -> startPasskeyAuth()
            AuthMethod.TOTP -> startTotpAuth()
        }
    }

    fun startFlowAuth() {
        showFlowOptionsSheet()
    }

    private fun startOtpAuth() {
        _navigateToOtp.value = true
    }

    fun onOtpNavigationHandled() {
        _navigateToOtp.value = false
    }

    private fun startOAuthNativeAuth() {
        _navigateToOAuthNative.value = true
    }

    fun onOAuthNativeNavigationHandled() {
        _navigateToOAuthNative.value = false
    }

    private fun startOAuthAuth() {
        _navigateToOAuthWeb.value = true
    }

    fun onOAuthWebNavigationHandled() {
        _navigateToOAuthWeb.value = false
    }

    private fun startMagicLinkAuth() {
        _navigateToMagicLink.value = true
    }

    fun onMagicLinkNavigationHandled() {
        _navigateToMagicLink.value = false
    }

    private fun startEnchantedLinkAuth() {
        _navigateToEnchantedLink.value = true
    }

    fun onEnchantedLinkNavigationHandled() {
        _navigateToEnchantedLink.value = false
    }

    private fun startPasswordAuth() {
        _navigateToPassword.value = true
    }

    fun onPasswordNavigationHandled() {
        _navigateToPassword.value = false
    }

    private fun startPasskeyAuth() {
        _navigateToPasskey.value = true
    }

    fun onPasskeyNavigationHandled() {
        _navigateToPasskey.value = false
    }

    private fun startTotpAuth() {
        _navigateToTotp.value = true
    }

    fun onTotpNavigationHandled() {
        _navigateToTotp.value = false
    }

    fun logout() {
        viewModelScope.launch {
            Descope.sessionManager.clearSession()
            _isAuthenticated.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun setAuthenticated(authenticated: Boolean) {
        _isAuthenticated.value = authenticated
    }
}
