package com.descope.testapp.ui.screens.home

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.descope.Descope
import com.descope.session.DescopeSession
import com.descope.testapp.BuildConfig
import com.descope.types.DescopeUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Data class to hold session status information.
 */
data class SessionStatus(
    val expiresAt: Date?,
    val isExpired: Boolean,
    val timeRemainingSeconds: Long? = null
)

/**
 * Available authentication methods that support updating an existing user.
 * These methods can add or update authentication info for a logged-in user.
 */
enum class UpdateAuthMethod(val displayName: String) {
    OTP("OTP (One-Time Password)"),
    MAGIC_LINK("Magic Link"),
    ENCHANTED_LINK("Enchanted Link"),
    PASSWORD("Password"),
    PASSKEY("Passkey"),
    TOTP("TOTP (Authenticator App)"),
}

/**
 * ViewModel for the Home screen.
 * Manages user information display and session refresh/logout operations.
 */
class HomeViewModel : ViewModel(), DefaultLifecycleObserver {

    // region State

    private val _user = MutableStateFlow<DescopeUser?>(null)
    val user: StateFlow<DescopeUser?> = _user.asStateFlow()

    private val _sessionStatus = MutableStateFlow<SessionStatus?>(null)
    val sessionStatus: StateFlow<SessionStatus?> = _sessionStatus.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isRefreshingUser = MutableStateFlow(false)
    val isRefreshingUser: StateFlow<Boolean> = _isRefreshingUser.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _showAuthenticatedFlowOptions = MutableStateFlow(false)
    val showAuthenticatedFlowOptions: StateFlow<Boolean> = _showAuthenticatedFlowOptions.asStateFlow()

    private val _showUpdateAuthOptions = MutableStateFlow(false)
    val showUpdateAuthOptions: StateFlow<Boolean> = _showUpdateAuthOptions.asStateFlow()

    // Navigation triggers for update auth methods
    private val _navigateToUpdateOtp = MutableStateFlow(false)
    val navigateToUpdateOtp: StateFlow<Boolean> = _navigateToUpdateOtp.asStateFlow()

    private val _navigateToUpdateMagicLink = MutableStateFlow(false)
    val navigateToUpdateMagicLink: StateFlow<Boolean> = _navigateToUpdateMagicLink.asStateFlow()

    private val _navigateToUpdateEnchantedLink = MutableStateFlow(false)
    val navigateToUpdateEnchantedLink: StateFlow<Boolean> = _navigateToUpdateEnchantedLink.asStateFlow()

    private val _navigateToUpdatePassword = MutableStateFlow(false)
    val navigateToUpdatePassword: StateFlow<Boolean> = _navigateToUpdatePassword.asStateFlow()

    private val _navigateToUpdatePasskey = MutableStateFlow(false)
    val navigateToUpdatePasskey: StateFlow<Boolean> = _navigateToUpdatePasskey.asStateFlow()

    private val _navigateToUpdateTotp = MutableStateFlow(false)
    val navigateToUpdateTotp: StateFlow<Boolean> = _navigateToUpdateTotp.asStateFlow()

    val availableFlows: List<String> = BuildConfig.DESCOPE_AUTHENTICATED_FLOW_IDS
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    // endregion

    // region Private Properties

    private var sessionUpdateJob: Job? = null
    private var wasJobRunningBeforeBackground = false
    private var hasLoadedInitialData = false

    // endregion

    // region Lifecycle

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        loadUserData()
    }

    override fun onStart(owner: LifecycleOwner) {
        if (wasJobRunningBeforeBackground) {
            wasJobRunningBeforeBackground = false
            val session = Descope.sessionManager.session
            updateSessionStatus(session)
            if (session != null && !session.refreshToken.isExpired) {
                startSessionStatusUpdates()
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        wasJobRunningBeforeBackground = sessionUpdateJob?.isActive == true
        stopSessionStatusUpdates()
    }

    override fun onCleared() {
        super.onCleared()
        stopSessionStatusUpdates()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
    }

    // endregion

    // region Session Status Updates

    private fun startSessionStatusUpdates() {
        if (sessionUpdateJob?.isActive == true) return

        sessionUpdateJob = viewModelScope.launch {
            while (isActive) {
                delay(5000L)

                val session = Descope.sessionManager.session
                updateSessionStatus(session)

                if (session == null || session.refreshToken.isExpired) {
                    break
                }
            }
        }
    }

    private fun stopSessionStatusUpdates() {
        sessionUpdateJob?.cancel()
        sessionUpdateJob = null
    }

    private fun updateSessionStatus(session: DescopeSession?) {
        _sessionStatus.value = session?.let {
            val expiresAtMillis = it.refreshToken.expiresAt
            val nowMillis = System.currentTimeMillis()
            val remainingMillis = expiresAtMillis - nowMillis
            val remainingSeconds = maxOf(remainingMillis / 1000, 0L)

            SessionStatus(
                expiresAt = Date(expiresAtMillis),
                isExpired = it.refreshToken.isExpired,
                timeRemainingSeconds = remainingSeconds
            )
        }
    }

    // endregion

    // region Public Actions

    /**
     * Loads the current user data from the session manager.
     * Only runs once per ViewModel instance to prevent duplicate calls.
     */
    fun loadUserData() {
        if (hasLoadedInitialData) return
        hasLoadedInitialData = true

        viewModelScope.launch {
            val session = Descope.sessionManager.session
            _user.value = session?.user
            updateSessionStatus(session)

            if (session != null && !session.refreshToken.isExpired) {
                startSessionStatusUpdates()
            }
        }
    }

    /**
     * Refreshes the session tokens using the current refresh token.
     */
    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null

            try {
                val refreshJwt = Descope.sessionManager.session?.refreshJwt
                    ?: throw Exception("No refresh token available")
                val refreshResponse = Descope.auth.refreshSession(refreshJwt)
                Descope.sessionManager.updateTokens(refreshResponse)
                updateSessionStatus(Descope.sessionManager.session)
                startSessionStatusUpdates()
                _successMessage.value = "Session refreshed"
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to refresh session"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    /**
     * Refreshes the user information from the server.
     */
    fun refreshUser() {
        viewModelScope.launch {
            _isRefreshingUser.value = true
            _error.value = null

            try {
                val refreshJwt = Descope.sessionManager.session?.refreshJwt
                    ?: throw Exception("No refresh token available")
                val userResponse = Descope.auth.me(refreshJwt)
                Descope.sessionManager.updateUser(userResponse)
                _user.value = userResponse
                _successMessage.value = "User refreshed"
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to refresh user"
            } finally {
                _isRefreshingUser.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    fun showAuthenticatedFlowOptionsSheet() {
        _showAuthenticatedFlowOptions.value = true
    }

    fun hideAuthenticatedFlowOptionsSheet() {
        _showAuthenticatedFlowOptions.value = false
    }

    fun onAuthenticatedFlowSelected(flowId: String, onNavigateToFlow: (String) -> Unit) {
        hideAuthenticatedFlowOptionsSheet()
        onNavigateToFlow(flowId)
    }

    // Update Auth Options Sheet

    fun showUpdateAuthOptionsSheet() {
        _showUpdateAuthOptions.value = true
    }

    fun hideUpdateAuthOptionsSheet() {
        _showUpdateAuthOptions.value = false
    }

    fun onUpdateAuthMethodSelected(method: UpdateAuthMethod) {
        hideUpdateAuthOptionsSheet()
        when (method) {
            UpdateAuthMethod.OTP -> _navigateToUpdateOtp.value = true
            UpdateAuthMethod.MAGIC_LINK -> _navigateToUpdateMagicLink.value = true
            UpdateAuthMethod.ENCHANTED_LINK -> _navigateToUpdateEnchantedLink.value = true
            UpdateAuthMethod.PASSWORD -> _navigateToUpdatePassword.value = true
            UpdateAuthMethod.PASSKEY -> _navigateToUpdatePasskey.value = true
            UpdateAuthMethod.TOTP -> _navigateToUpdateTotp.value = true
        }
    }

    fun onUpdateOtpNavigationHandled() {
        _navigateToUpdateOtp.value = false
    }

    fun onUpdateMagicLinkNavigationHandled() {
        _navigateToUpdateMagicLink.value = false
    }

    fun onUpdateEnchantedLinkNavigationHandled() {
        _navigateToUpdateEnchantedLink.value = false
    }

    fun onUpdatePasswordNavigationHandled() {
        _navigateToUpdatePassword.value = false
    }

    fun onUpdatePasskeyNavigationHandled() {
        _navigateToUpdatePasskey.value = false
    }

    fun onUpdateTotpNavigationHandled() {
        _navigateToUpdateTotp.value = false
    }

    /**
     * Called when an update operation completes successfully.
     * Shows a success message and refreshes user data.
     */
    fun onUpdateSuccess(message: String) {
        _successMessage.value = message
        // Refresh user data to reflect the update
        hasLoadedInitialData = false
        loadUserData()
    }

    // endregion
}
