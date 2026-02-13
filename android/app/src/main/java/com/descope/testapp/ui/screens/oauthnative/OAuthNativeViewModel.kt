package com.descope.testapp.ui.screens.oauthnative

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.descope.Descope
import com.descope.session.DescopeSession
import com.descope.types.OAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the OAuth Native (Google Sign-In) authentication screen.
 * Handles native OAuth authentication using the Descope SDK.
 */
class OAuthNativeViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    fun clearError() {
        _error.value = null
    }

    /**
     * Start the native Google Sign-In flow using Credential Manager.
     *
     * @param context The Android context (Activity) needed for Credential Manager
     */
    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Use the Descope SDK's native OAuth method
                val authResponse = Descope.oauth.native(
                    context = context,
                    provider = OAuthProvider.Google
                )

                // Create and manage the session
                val session = DescopeSession(authResponse)
                Descope.sessionManager.manageSession(session)

                _isAuthenticated.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to sign in with Google"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
