package com.descope.testapp.ui.screens.otp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.descope.Descope
import com.descope.session.DescopeSession
import com.descope.testapp.data.LastUsedPreferences
import com.descope.testapp.ui.screens.AuthMode
import com.descope.types.DeliveryMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Delivery method for OTP - either email or phone (SMS)
 */
enum class OtpDeliveryMethod(val displayName: String) {
    EMAIL("Email"),
    PHONE("Phone")
}

/**
 * Represents the current step in the OTP flow
 */
enum class OtpStep {
    INPUT,      // User enters email/phone
    VERIFY      // User enters OTP code
}

/**
 * ViewModel for the OTP authentication screen.
 * Handles sending OTP and verifying the code.
 */
class OtpViewModel(application: Application) : AndroidViewModel(application) {

    private val lastUsedPreferences = LastUsedPreferences(application)

    private val _mode = MutableStateFlow(AuthMode.SignIn)
    val mode: StateFlow<AuthMode> = _mode.asStateFlow()

    private val _deliveryMethod = MutableStateFlow(OtpDeliveryMethod.EMAIL)
    val deliveryMethod: StateFlow<OtpDeliveryMethod> = _deliveryMethod.asStateFlow()

    private val _inputValue = MutableStateFlow("")
    val inputValue: StateFlow<String> = _inputValue.asStateFlow()

    private val _otpCode = MutableStateFlow("")
    val otpCode: StateFlow<String> = _otpCode.asStateFlow()

    private val _currentStep = MutableStateFlow(OtpStep.INPUT)
    val currentStep: StateFlow<OtpStep> = _currentStep.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    // Last used data for hint display
    private val _lastUsedDeliveryMethod = MutableStateFlow<OtpDeliveryMethod?>(null)
    val lastUsedDeliveryMethod: StateFlow<OtpDeliveryMethod?> = _lastUsedDeliveryMethod.asStateFlow()

    private val _lastUsedValue = MutableStateFlow<String?>(null)
    val lastUsedValue: StateFlow<String?> = _lastUsedValue.asStateFlow()

    init {
        // Load last used data
        loadLastUsedData()
    }

    private fun loadLastUsedData() {
        val lastMethod = lastUsedPreferences.getLastUsedOtpDeliveryMethod()
        val lastValue = lastUsedPreferences.getLastUsedOtpValue()

        _lastUsedDeliveryMethod.value = lastMethod
        _lastUsedValue.value = lastValue

        // Set the delivery method to the last used one if available
        lastMethod?.let {
            _deliveryMethod.value = it
        }
    }

    fun setDeliveryMethod(method: OtpDeliveryMethod) {
        _deliveryMethod.value = method
        // Clear input when switching methods
        _inputValue.value = ""
        _error.value = null
    }

    fun setInputValue(value: String) {
        _inputValue.value = value
        _error.value = null
    }

    fun setOtpCode(code: String) {
        _otpCode.value = code
        _error.value = null
    }

    fun clearError() {
        _error.value = null
    }

    /**
     * Set the authentication mode (SignIn or Update).
     * Should be called before the screen is displayed.
     * In update mode, prefills with current user data.
     */
    fun setMode(mode: AuthMode) {
        _mode.value = mode
        if (mode == AuthMode.Update) {
            // Prefill from signed-in user
            Descope.sessionManager.session?.user?.let { user ->
                // For update mode, we want to add a NEW email/phone, so don't prefill input
                // But set the delivery method based on what the user doesn't have yet
                if (user.email == null || !user.isVerifiedEmail) {
                    _deliveryMethod.value = OtpDeliveryMethod.EMAIL
                } else if (user.phone == null || !user.isVerifiedPhone) {
                    _deliveryMethod.value = OtpDeliveryMethod.PHONE
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
            return if (_deliveryMethod.value == OtpDeliveryMethod.PHONE) {
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
     * Send OTP to the provided email or phone number.
     * If input is empty, uses the last used value for the current delivery method.
     * In update mode, calls the update API instead of signUpOrIn.
     */
    fun sendOtp() {
        val effectiveInput = getEffectiveInput()
        if (effectiveInput.isNullOrEmpty()) {
            _error.value = when (_deliveryMethod.value) {
                OtpDeliveryMethod.EMAIL -> "Please enter your email address"
                OtpDeliveryMethod.PHONE -> "Please enter your phone number"
            }
            return
        }

        // If using last used value, set it as the input value for display in verification step
        if (_inputValue.value.isEmpty()) {
            _inputValue.value = effectiveInput
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                if (_mode.value == AuthMode.Update) {
                    // Update mode - add email/phone to existing user
                    val session = Descope.sessionManager.session
                        ?: throw Exception("No active session")
                    val loginId = session.user.loginIds.firstOrNull()
                        ?: throw Exception("No login ID available")

                    when (_deliveryMethod.value) {
                        OtpDeliveryMethod.EMAIL -> {
                            Descope.otp.updateEmail(effectiveInput, loginId, session.refreshJwt)
                        }
                        OtpDeliveryMethod.PHONE -> {
                            Descope.otp.updatePhone(effectiveInput, DeliveryMethod.Sms, loginId, session.refreshJwt)
                        }
                    }
                } else {
                    // Sign-in mode
                    when (_deliveryMethod.value) {
                        OtpDeliveryMethod.EMAIL -> {
                            Descope.otp.signUpOrIn(DeliveryMethod.Email, effectiveInput)
                        }
                        OtpDeliveryMethod.PHONE -> {
                            Descope.otp.signUpOrIn(DeliveryMethod.Sms, effectiveInput)
                        }
                    }
                }
                // Save the last used data
                lastUsedPreferences.saveLastUsedOtp(_deliveryMethod.value, effectiveInput)
                _lastUsedDeliveryMethod.value = _deliveryMethod.value
                _lastUsedValue.value = effectiveInput

                // Move to verification step
                _currentStep.value = OtpStep.VERIFY
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to send OTP"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Verify the OTP code entered by the user.
     * In update mode, verification completes the update operation using the session's login ID.
     * In sign-in mode, creates a new session using the input value.
     */
    fun verifyOtp() {
        val code = _otpCode.value.trim()
        if (code.length != 6) {
            _error.value = "Please enter the complete 6-digit code"
            return
        }

        val newValue = _inputValue.value.trim()

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                if (_mode.value == AuthMode.Update) {
                    // In update mode, verify using the session's login ID
                    val session = Descope.sessionManager.session
                        ?: throw Exception("No active session")
                    val loginId = session.user.loginIds.firstOrNull()
                        ?: throw Exception("No login ID available")

                    when (_deliveryMethod.value) {
                        OtpDeliveryMethod.EMAIL -> {
                            Descope.otp.verify(DeliveryMethod.Email, loginId, code)
                        }
                        OtpDeliveryMethod.PHONE -> {
                            Descope.otp.verify(DeliveryMethod.Sms, loginId, code)
                        }
                    }

                    // Refresh the user data to reflect the update
                    val updatedUser = Descope.auth.me(session.refreshJwt)
                    Descope.sessionManager.updateUser(updatedUser)
                    // Save to shared prefs to track the new value
                    lastUsedPreferences.saveLastUsedOtp(_deliveryMethod.value, newValue)
                    _lastUsedDeliveryMethod.value = _deliveryMethod.value
                    _lastUsedValue.value = newValue
                    _isAuthenticated.value = true
                } else {
                    // In sign-in mode, verify using the input value
                    val response = when (_deliveryMethod.value) {
                        OtpDeliveryMethod.EMAIL -> {
                            Descope.otp.verify(DeliveryMethod.Email, newValue, code)
                        }
                        OtpDeliveryMethod.PHONE -> {
                            Descope.otp.verify(DeliveryMethod.Sms, newValue, code)
                        }
                    }
                    // Store session from authentication response
                    val descopeSession = DescopeSession(response)
                    Descope.sessionManager.manageSession(descopeSession)
                    _isAuthenticated.value = true
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Invalid code. Please try again."
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Go back to the input step
     */
    fun goBackToInput() {
        _currentStep.value = OtpStep.INPUT
        _otpCode.value = ""
        _error.value = null
    }

    /**
     * Resend the OTP code
     */
    fun resendOtp() {
        sendOtp()
    }
}
