package com.descope.testapp.ui.screens.flow

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import com.descope.Descope
import com.descope.android.DescopeFlow
import com.descope.android.DescopeFlowView
import com.descope.session.DescopeSession
import com.descope.testapp.BuildConfig
import com.descope.testapp.ui.components.SnackbarType
import com.descope.testapp.ui.components.StyledSnackbar
import com.descope.testapp.ui.components.StyledSnackbarData
import com.descope.testapp.ui.theme.Dimensions
import com.descope.types.AuthenticationResponse
import com.descope.types.DescopeException
import com.descope.types.OAuthProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val ANIMATION_DELAY_MS = 350L // Slightly longer than nav animation (300ms)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowScreen(
    flowId: String,
    onFlowSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarData = remember { mutableStateOf<StyledSnackbarData?>(null) }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var shouldShowWebView by remember { mutableStateOf(false) }

    // Delay WebView creation until after enter animation completes
    LaunchedEffect(Unit) {
        delay(ANIMATION_DELAY_MS)
        shouldShowWebView = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Flow WebView - created after animation delay
            if (shouldShowWebView) {
                DisposableEffect(Unit) {
                    onDispose {
                        FlowViewHolder.clear()
                    }
                }

                AndroidView(
                    factory = { context ->
                        DescopeFlowView(context).apply {
                            FlowViewHolder.setActiveFlowView(this)

                            listener = object : DescopeFlowView.Listener {
                                override fun onReady() {
                                    isLoading = false
                                }

                                override fun onSuccess(response: AuthenticationResponse) {
                                    val session = DescopeSession(response)
                                    Descope.sessionManager.manageSession(session)
                                    onFlowSuccess()
                                }

                                override fun onError(exception: DescopeException) {
                                    scope.launch {
                                        snackbarData.value = StyledSnackbarData("$exception", SnackbarType.ERROR)
                                    }
                                    onNavigateBack()
                                }
                            }

                            val deepAppLink = "https://${BuildConfig.DEEP_LINK_HOST}/flow"
                            val customSchemeLink = "descope://${BuildConfig.DEEP_LINK_HOST}/flow"
                            startFlow(DescopeFlow.hosted(flowId).apply {
                                oauthNativeProvider = OAuthProvider.Google
                                oauthRedirect = deepAppLink
                                oauthRedirectCustomScheme = customSchemeLink
                                magicLinkRedirect = deepAppLink
                                ssoRedirect = deepAppLink
                                ssoRedirectCustomScheme = customSchemeLink
                            })
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(if (isLoading) 0f else 1f)
                )
            }

            // Loading indicator
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimensions.IconSizeLarge),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Styled Snackbar at the bottom
            StyledSnackbar(
                snackbarData = snackbarData.value,
                onDismiss = { snackbarData.value = null },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = innerPadding.calculateBottomPadding())
            )
        }
    }
}
