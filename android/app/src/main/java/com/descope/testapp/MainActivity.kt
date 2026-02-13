package com.descope.testapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.descope.testapp.ui.App
import com.descope.testapp.ui.screens.flow.FlowViewHolder
import com.descope.testapp.ui.screens.magiclink.MagicLinkCallbackHolder
import com.descope.testapp.ui.screens.oauthweb.OAuthCallbackHolder
import com.descope.testapp.ui.theme.DescopeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DescopeTheme {
                App(
                    onHandleDeepLink = { intent -> handleDeepLink(intent) }
                )
            }
        }

        // Handle initial deep link if app was launched from one
        intent?.let { handleDeepLink(it) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent) {
        val uri = intent.data ?: return
        val path = uri.path ?: return

        when {
            path == "/flow" -> {
                // Flow authentication callback
                if (FlowViewHolder.resumeFromDeepLink(uri)) {
                    Log.d("MainActivity", "Flow deep link handled: $uri")
                } else {
                    Log.w("MainActivity", "No active flow view to handle deep link: $uri")
                }
            }
            path == "/magiclink" -> {
                // Magic Link callback
                if (MagicLinkCallbackHolder.handleCallback(uri)) {
                    Log.d("MainActivity", "Magic link deep link handled: $uri")
                } else {
                    Log.w("MainActivity", "No active magic link view to handle deep link: $uri")
                }
            }
            path == "/oauth" -> {
                // OAuth callback
                if (OAuthCallbackHolder.handleCallback(uri)) {
                    Log.d("MainActivity", "OAuth deep link handled: $uri")
                } else {
                    Log.w("MainActivity", "No active OAuth view to handle deep link: $uri")
                }
            }
        }
    }
}
