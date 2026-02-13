package com.descope.testapp.ui.screens.enchantedlink

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.descope.Descope
import com.descope.session.DescopeSession
import com.descope.testapp.BuildConfig
import com.descope.testapp.data.LastUsedPreferences
import com.descope.testapp.ui.screens.AuthMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Represents the current step in the Enchanted Link flow
 */
enum class EnchantedLinkStep {
    INPUT,      // User enters email
    WAITING     // Waiting for user to click enchanted link (polling)
}

/**
 * ViewModel for the Enchanted Link authentication screen.
 * Handles sending enchanted links and polling for session verification.
 *
 * Unlike Magic Link which redirects back to the app, Enchanted Link uses a
 * web page for verification and the app polls for the session.
 */
class EnchantedLinkViewModel(application: Application) : AndroidViewModel(application) {

    private val lastUsedPreferences = LastUsedPreferences(application)

    private val _mode = MutableStateFlow(AuthMode.SignIn)
    val mode: StateFlow<AuthMode> = _mode.asStateFlow()

    private val _emailValue = MutableStateFlow("")
    val emailValue: StateFlow<String> = _emailValue.asStateFlow()

    private val _currentStep = MutableStateFlow(EnchantedLinkStep.INPUT)
    val currentStep: StateFlow<EnchantedLinkStep> = _currentStep.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    // The link ID to show the user (which link to click in email)
    private val _linkId = MutableStateFlow<String?>(null)
    val linkId: StateFlow<String?> = _linkId.asStateFlow()

    // Last used data for hint display
    private val _lastUsedEmail = MutableStateFlow<String?>(null)
    val lastUsedEmail: StateFlow<String?> = _lastUsedEmail.asStateFlow()

    // Polling job reference for cancellation
    private var pollingJob: Job? = null

    init {
        loadLastUsedData()
    }

    private fun loadLastUsedData() {
        val lastEmail = lastUsedPreferences.getLastUsedEnchantedLinkEmail()
        _lastUsedEmail.value = lastEmail
    }

    fun setEmailValue(value: String) {
        _emailValue.value = value
        _error.value = null
    }

    fun clearError() {
        _error.value = null
    }

    fun setMode(mode: AuthMode) {
        _mode.value = mode
        // EnchantedLink is email-only, no need to change delivery method
        // In update mode, we want user to enter a NEW email, so don't prefill
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
     * Send Enchanted Link to the provided email address.
     * If input is empty, uses the last used email.
     * In update mode, calls the updateEmail API instead of signUpOrIn.
     */
    fun sendEnchantedLink() {
        val effectiveEmail = getEffectiveEmail()
        if (effectiveEmail.isNullOrEmpty()) {
            _error.value = "Please enter your email address"
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
                // Build the verify URL for enchanted link
                val verifyUrl = "https://${BuildConfig.DEEP_LINK_HOST}/verify/${BuildConfig.DESCOPE_PROJECT_ID}"

                val response = if (_mode.value == AuthMode.Update) {
                    // Update mode - add email to existing user
                    val session = Descope.sessionManager.session
                        ?: throw Exception("No active session")
                    val loginId = session.user.loginIds.firstOrNull()
                        ?: throw Exception("No login ID available")

                    Descope.enchantedLink.updateEmail(
                        email = effectiveEmail,
                        loginId = loginId,
                        uri = verifyUrl,
                        refreshJwt = session.refreshJwt
                    )
                } else {
                    // Sign-in mode
                    Descope.enchantedLink.signUpOrIn(
                        loginId = effectiveEmail,
                        uri = verifyUrl
                    )
                }

                // Save the last used email
                lastUsedPreferences.saveLastUsedEnchantedLinkEmail(effectiveEmail)
                _lastUsedEmail.value = effectiveEmail

                // Store the link ID to show the user
                _linkId.value = response.linkId

                // Move to waiting step
                _currentStep.value = EnchantedLinkStep.WAITING
                _isLoading.value = false

                // Start polling for session
                startPolling(response.pendingRef)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to send enchanted link"
                _isLoading.value = false
            }
        }
    }

    /**
     * Start polling for session verification.
     * In update mode, refreshes user data instead of creating a new session.
     */
    private fun startPolling(pendingRef: String) {
        // Cancel any existing polling job
        pollingJob?.cancel()

        pollingJob = viewModelScope.launch {
            _isLoading.value = true

            try {
                // Poll for session - this will block until verified or timeout
                val authResponse = Descope.enchantedLink.pollForSession(pendingRef)

                if (_mode.value == AuthMode.Update) {
                    // In update mode, refresh user data to reflect the update
                    Descope.sessionManager.session?.let { session ->
                        val updatedUser = Descope.auth.me(session.refreshJwt)
                        Descope.sessionManager.updateUser(updatedUser)
                        // Save to prefs after successful update
                        val email = _emailValue.value.trim()
                        lastUsedPreferences.saveLastUsedEnchantedLinkEmail(email)
                        _lastUsedEmail.value = email
                    }
                } else {
                    // Create and manage the session
                    val session = DescopeSession(authResponse)
                    Descope.sessionManager.manageSession(session)
                }

                _isAuthenticated.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to verify enchanted link"
                // Go back to input step on error
                _currentStep.value = EnchantedLinkStep.INPUT
                _linkId.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Go back to the input step and cancel polling.
     */
    fun goBackToInput() {
        pollingJob?.cancel()
        pollingJob = null
        _currentStep.value = EnchantedLinkStep.INPUT
        _linkId.value = null
        _error.value = null
        _isLoading.value = false
    }

    /**
     * Resend the enchanted link.
     */
    fun resendEnchantedLink() {
        pollingJob?.cancel()
        pollingJob = null
        sendEnchantedLink()
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
