package com.descope.testapp.ui.screens.magiclink

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.descope.Descope
import com.descope.session.DescopeSession
import com.descope.testapp.BuildConfig
import com.descope.testapp.data.LastUsedPreferences
import com.descope.testapp.ui.screens.AuthMode
import com.descope.types.DeliveryMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Delivery method for Magic Link - either email or phone (SMS)
 */
enum class MagicLinkDeliveryMethod(val displayName: String) {
    EMAIL("Email"),
    PHONE("Phone")
}

/**
 * Represents the current step in the Magic Link flow
 */
enum class MagicLinkStep {
    INPUT,      // User enters email/phone
    WAITING     // Waiting for user to click magic link
}

/**
 * ViewModel for the Magic Link authentication screen.
 * Handles sending magic links and verifying the callback.
 */
class MagicLinkViewModel(application: Application) : AndroidViewModel(application) {

    private val lastUsedPreferences = LastUsedPreferences(application)

    private val _mode = MutableStateFlow(AuthMode.SignIn)
    val mode: StateFlow<AuthMode> = _mode.asStateFlow()

    private val _deliveryMethod = MutableStateFlow(MagicLinkDeliveryMethod.EMAIL)
    val deliveryMethod: StateFlow<MagicLinkDeliveryMethod> = _deliveryMethod.asStateFlow()

    private val _inputValue = MutableStateFlow("")
    val inputValue: StateFlow<String> = _inputValue.asStateFlow()

    private val _currentStep = MutableStateFlow(MagicLinkStep.INPUT)
    val currentStep: StateFlow<MagicLinkStep> = _currentStep.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    // Last used data for hint display
    private val _lastUsedDeliveryMethod = MutableStateFlow<MagicLinkDeliveryMethod?>(null)
    val lastUsedDeliveryMethod: StateFlow<MagicLinkDeliveryMethod?> = _lastUsedDeliveryMethod.asStateFlow()

    private val _lastUsedValue = MutableStateFlow<String?>(null)
    val lastUsedValue: StateFlow<String?> = _lastUsedValue.asStateFlow()

    init {
        // Load last used data
        loadLastUsedData()
    }

    private fun loadLastUsedData() {
        val lastMethod = lastUsedPreferences.getLastUsedMagicLinkDeliveryMethod()
        val lastValue = lastUsedPreferences.getLastUsedMagicLinkValue()

        _lastUsedDeliveryMethod.value = lastMethod
        _lastUsedValue.value = lastValue

        // Set the delivery method to the last used one if available
        lastMethod?.let {
            _deliveryMethod.value = it
        }
    }

    fun setDeliveryMethod(method: MagicLinkDeliveryMethod) {
        _deliveryMethod.value = method
        // Clear input when switching methods
        _inputValue.value = ""
        _error.value = null
    }

    fun setInputValue(value: String) {
        _inputValue.value = value
        _error.value = null
    }

    fun clearError() {
        _error.value = null
    }

    fun setMode(mode: AuthMode) {
        _mode.value = mode
        if (mode == AuthMode.Update) {
            // In update mode, set delivery method based on what user doesn't have
            Descope.sessionManager.session?.user?.let { user ->
                if (user.email == null || !user.isVerifiedEmail) {
                    _deliveryMethod.value = MagicLinkDeliveryMethod.EMAIL
                } else if (user.phone == null || !user.isVerifiedPhone) {
                    _deliveryMethod.value = MagicLinkDeliveryMethod.PHONE
                }
            }
        }
    }

    /**
     * Normalize phone number by adding "+" prefix if not present.
     */
    private fun normalizePhoneNumber(phone: String): String {
        return if (phone.isNotEmpty() && !phone.startsWith("+")) {
            "+$phone"
        } else {
            phone
        }
    }

    /**
     * Get the effective input value - either the user input or the last used value if applicable.
     * For phone numbers, ensures "+" prefix is present.
     */
    private fun getEffectiveInput(): String? {
        val input = _inputValue.value.trim()
        if (input.isNotEmpty()) {
            return if (_deliveryMethod.value == MagicLinkDeliveryMethod.PHONE) {
                normalizePhoneNumber(input)
            } else {
                input
            }
        }

        // Use last used value if delivery method matches
        val lastMethod = _lastUsedDeliveryMethod.value
        val lastValue = _lastUsedValue.value
        if (lastMethod == _deliveryMethod.value && !lastValue.isNullOrEmpty()) {
            return lastValue
        }

        return null
    }

    /**
     * Send Magic Link to the provided email or phone number.
     * If input is empty, uses the last used value for the current delivery method.
     * In update mode, calls the update API instead of signUpOrIn.
     */
    fun sendMagicLink() {
        val effectiveInput = getEffectiveInput()
        if (effectiveInput.isNullOrEmpty()) {
            _error.value = when (_deliveryMethod.value) {
                MagicLinkDeliveryMethod.EMAIL -> "Please enter your email address"
                MagicLinkDeliveryMethod.PHONE -> "Please enter your phone number"
            }
            return
        }

        // If using last used value, set it as the input value for display
        if (_inputValue.value.isEmpty()) {
            _inputValue.value = effectiveInput
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Build the redirect URL using the deep link host
                val redirectUrl = "https://${BuildConfig.DEEP_LINK_HOST}/magiclink"

                if (_mode.value == AuthMode.Update) {
                    // Update mode - add email/phone to existing user
                    val session = Descope.sessionManager.session
                        ?: throw Exception("No active session")
                    val loginId = session.user.loginIds.firstOrNull()
                        ?: throw Exception("No login ID available")

                    when (_deliveryMethod.value) {
                        MagicLinkDeliveryMethod.EMAIL -> {
                            Descope.magicLink.updateEmail(
                                email = effectiveInput,
                                loginId = loginId,
                                uri = redirectUrl,
                                refreshJwt = session.refreshJwt
                            )
                        }
                        MagicLinkDeliveryMethod.PHONE -> {
                            Descope.magicLink.updatePhone(
                                phone = effectiveInput,
                                method = DeliveryMethod.Sms,
                                loginId = loginId,
                                uri = redirectUrl,
                                refreshJwt = session.refreshJwt
                            )
                        }
                    }
                } else {
                    // Sign-in mode
                    when (_deliveryMethod.value) {
                        MagicLinkDeliveryMethod.EMAIL -> {
                            Descope.magicLink.signUpOrIn(
                                method = DeliveryMethod.Email,
                                loginId = effectiveInput,
                                uri = redirectUrl
                            )
                        }
                        MagicLinkDeliveryMethod.PHONE -> {
                            Descope.magicLink.signUpOrIn(
                                method = DeliveryMethod.Sms,
                                loginId = effectiveInput,
                                uri = redirectUrl
                            )
                        }
                    }
                }

                // Save the last used data
                lastUsedPreferences.saveLastUsedMagicLink(_deliveryMethod.value, effectiveInput)
                _lastUsedDeliveryMethod.value = _deliveryMethod.value
                _lastUsedValue.value = effectiveInput

                // Move to waiting step
                _currentStep.value = MagicLinkStep.WAITING
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to send magic link"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Handle the Magic Link callback from the deep link.
     * Verifies the token and completes authentication.
     * In update mode, refreshes user data instead of creating a new session.
     *
     * @param uri The URI from the magic link containing the token parameter
     */
    fun handleMagicLinkCallback(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Extract the token from the URI
                val token = uri.getQueryParameter("t")

                // Check for errors in the callback
                val errorParam = uri.getQueryParameter("err") ?: uri.getQueryParameter("error")
                if (errorParam != null) {
                    throw Exception("Magic link error: $errorParam")
                }

                if (token.isNullOrEmpty()) {
                    throw Exception("No token received in magic link")
                }

                // Verify the magic link token
                val authResponse = Descope.magicLink.verify(token)

                if (_mode.value == AuthMode.Update) {
                    // In update mode, refresh user data to reflect the update
                    Descope.sessionManager.session?.let { session ->
                        val updatedUser = Descope.auth.me(session.refreshJwt)
                        Descope.sessionManager.updateUser(updatedUser)
                        // Save to prefs after successful update
                        val input = _inputValue.value.trim()
                        lastUsedPreferences.saveLastUsedMagicLink(_deliveryMethod.value, input)
                        _lastUsedDeliveryMethod.value = _deliveryMethod.value
                        _lastUsedValue.value = input
                    }
                } else {
                    // Create and manage the session
                    val session = DescopeSession(authResponse)
                    Descope.sessionManager.manageSession(session)
                }

                _isAuthenticated.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to verify magic link"
                // Go back to input step on error
                _currentStep.value = MagicLinkStep.INPUT
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Go back to the input step
     */
    fun goBackToInput() {
        _currentStep.value = MagicLinkStep.INPUT
        _error.value = null
    }

    /**
     * Resend the magic link
     */
    fun resendMagicLink() {
        sendMagicLink()
    }
}
