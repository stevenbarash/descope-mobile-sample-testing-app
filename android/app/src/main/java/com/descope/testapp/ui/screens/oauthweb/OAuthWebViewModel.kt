package com.descope.testapp.ui.screens.oauthweb

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.descope.Descope
import com.descope.android.launchCustomTab
import com.descope.session.DescopeSession
import com.descope.testapp.BuildConfig
import com.descope.types.OAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Available OAuth providers for web-based OAuth authentication.
 */
enum class OAuthProviderOption(val displayName: String, val provider: OAuthProvider) {
    GOOGLE("Google", OAuthProvider.Google),
    APPLE("Apple", OAuthProvider.Apple),
    FACEBOOK("Facebook", OAuthProvider.Facebook),
    GITHUB("GitHub", OAuthProvider.Github),
    MICROSOFT("Microsoft", OAuthProvider.Microsoft),
    GITLAB("GitLab", OAuthProvider.Gitlab),
    SLACK("Slack", OAuthProvider.Slack),
    DISCORD("Discord", OAuthProvider.Discord),
}

/**
 * ViewModel for the OAuth Web authentication screen.
 * Handles web-based OAuth authentication using Custom Tabs and the Descope SDK.
 */
class OAuthWebViewModel : ViewModel() {

    private val _selectedProvider = MutableStateFlow(OAuthProviderOption.GOOGLE)
    val selectedProvider: StateFlow<OAuthProviderOption> = _selectedProvider.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    fun setSelectedProvider(provider: OAuthProviderOption) {
        _selectedProvider.value = provider
    }

    fun clearError() {
        _error.value = null
    }

    /**
     * Start the OAuth web flow using Custom Tabs.
     *
     * @param context The Android context needed to launch Custom Tabs
     */
    fun startOAuthFlow(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Build the OAuth redirect URL using the deep link host
                val redirectUrl = "https://${BuildConfig.DEEP_LINK_HOST}/oauth"

                // Get the authorization URL from Descope
                val authUrl = Descope.oauth.signUpOrIn(
                    provider = _selectedProvider.value.provider,
                    redirectUrl = redirectUrl
                )

                // Launch Custom Tab with the authorization URL
                launchCustomTab(context, authUrl)

                // Note: isLoading stays true until we receive the callback
                // The flow continues when handleOAuthCallback is called
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to start OAuth flow"
                _isLoading.value = false
            }
        }
    }

    /**
     * Handle the OAuth callback from the deep link.
     * Exchanges the authorization code for tokens.
     *
     * @param uri The URI from the OAuth redirect containing the code parameter
     */
    fun handleOAuthCallback(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Extract the code from the URI
                val code = uri.getQueryParameter("code")

                // Check for errors in the callback
                val errorParam = uri.getQueryParameter("err") ?: uri.getQueryParameter("error")
                if (errorParam != null) {
                    throw Exception("OAuth error: $errorParam")
                }

                if (code.isNullOrEmpty()) {
                    throw Exception("No authorization code received")
                }

                // Exchange the code for tokens
                val authResponse = Descope.oauth.exchange(code)

                // Create and manage the session
                val session = DescopeSession(authResponse)
                Descope.sessionManager.manageSession(session)

                _isAuthenticated.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to complete OAuth authentication"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Called when the user cancels or returns from the Custom Tab without completing auth.
     */
    fun onCustomTabClosed() {
        _isLoading.value = false
    }
}
