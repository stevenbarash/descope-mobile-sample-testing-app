package com.descope.testapp.ui.screens.oauthweb

import android.net.Uri

/**
 * Singleton holder for managing OAuth callback routing.
 * This allows the MainActivity to pass the OAuth redirect URI to the active OAuthWebViewModel.
 * TODO: Consider implementing a "Descope.resume()" function like iOS to avoid this global holder - same as flows.
 */
object OAuthCallbackHolder {
    private var activeViewModel: OAuthWebViewModel? = null

    /**
     * Set the active OAuthWebViewModel that should receive callbacks.
     */
    fun setActiveViewModel(viewModel: OAuthWebViewModel) {
        activeViewModel = viewModel
    }

    /**
     * Clear the active view model reference.
     */
    fun clear() {
        activeViewModel = null
    }

    /**
     * Handle an OAuth callback URI.
     *
     * @param uri The URI from the OAuth redirect
     * @return true if the callback was handled, false if no active viewmodel was available
     */
    fun handleCallback(uri: Uri): Boolean {
        return activeViewModel?.let { viewModel ->
            viewModel.handleOAuthCallback(uri)
            true
        } ?: false
    }

    /**
     * Notify that the Custom Tab was closed without completing authentication.
     */
    fun notifyCustomTabClosed() {
        activeViewModel?.onCustomTabClosed()
    }
}
