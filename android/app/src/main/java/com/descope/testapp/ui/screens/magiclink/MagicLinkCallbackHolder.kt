package com.descope.testapp.ui.screens.magiclink

import android.net.Uri

/**
 * Singleton holder for managing Magic Link callback routing.
 * This allows the MainActivity to pass the magic link redirect URI to the active MagicLinkViewModel.
 *
 * Unlike OAuth which uses Custom Tabs (staying in app context), Magic Link requires the user
 * to open their email app and click a link. This means the app may be in the background
 * when the link is clicked. We handle this by storing the pending URI and processing it
 * when the MagicLinkScreen becomes active.
 */
object MagicLinkCallbackHolder {
    private var activeViewModel: MagicLinkViewModel? = null
    private var pendingUri: Uri? = null

    /**
     * Set the active MagicLinkViewModel that should receive callbacks.
     * If there's a pending URI, it will be processed immediately.
     */
    fun setActiveViewModel(viewModel: MagicLinkViewModel) {
        activeViewModel = viewModel
        // Process any pending callback
        pendingUri?.let { uri ->
            viewModel.handleMagicLinkCallback(uri)
            pendingUri = null
        }
    }

    /**
     * Clear the active view model reference.
     */
    fun clear() {
        activeViewModel = null
    }

    /**
     * Handle a Magic Link callback URI.
     * If there's an active view model, the callback is processed immediately.
     * Otherwise, the URI is stored and will be processed when a view model becomes active.
     *
     * @param uri The URI from the magic link redirect
     * @return true if the callback was handled or stored, false otherwise
     */
    fun handleCallback(uri: Uri): Boolean {
        return activeViewModel?.let { viewModel ->
            viewModel.handleMagicLinkCallback(uri)
            true
        } ?: run {
            // Store the URI for later processing when the screen becomes active
            pendingUri = uri
            true
        }
    }

    /**
     * Check if there's a pending magic link callback.
     */
    fun hasPendingCallback(): Boolean = pendingUri != null

    /**
     * Clear any pending callback without processing it.
     */
    fun clearPendingCallback() {
        pendingUri = null
    }
}
