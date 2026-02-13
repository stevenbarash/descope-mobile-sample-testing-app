package com.descope.testapp.ui.screens.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer as RowSpacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.descope.testapp.ui.components.BottomSheetHeaderConfig
import com.descope.testapp.ui.components.BottomSheetOption
import com.descope.testapp.ui.components.OptionsBottomSheet
import com.descope.testapp.ui.components.SnackbarType
import com.descope.testapp.ui.components.StyledSnackbar
import com.descope.testapp.ui.components.StyledSnackbarData
import com.descope.testapp.ui.theme.DescopeTheme
import com.descope.testapp.ui.theme.Dimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToFlow: (String) -> Unit,
    onNavigateToOtp: () -> Unit,
    onNavigateToOAuthNative: () -> Unit,
    onNavigateToOAuthWeb: () -> Unit,
    onNavigateToMagicLink: () -> Unit,
    onNavigateToEnchantedLink: () -> Unit,
    onNavigateToPassword: () -> Unit,
    onNavigateToPasskey: () -> Unit,
    onNavigateToTotp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val showApiOptions by viewModel.showApiOptions.collectAsState()
    val showFlowOptions by viewModel.showFlowOptions.collectAsState()
    val navigateToOtp by viewModel.navigateToOtp.collectAsState()
    val navigateToOAuthNative by viewModel.navigateToOAuthNative.collectAsState()
    val navigateToOAuthWeb by viewModel.navigateToOAuthWeb.collectAsState()
    val navigateToMagicLink by viewModel.navigateToMagicLink.collectAsState()
    val navigateToEnchantedLink by viewModel.navigateToEnchantedLink.collectAsState()
    val navigateToPassword by viewModel.navigateToPassword.collectAsState()
    val navigateToPasskey by viewModel.navigateToPasskey.collectAsState()
    val navigateToTotp by viewModel.navigateToTotp.collectAsState()

    val snackbarData = remember { mutableStateOf<StyledSnackbarData?>(null) }

    // Handle navigation to OTP
    LaunchedEffect(navigateToOtp) {
        if (navigateToOtp) {
            onNavigateToOtp()
            viewModel.onOtpNavigationHandled()
        }
    }

    // Handle navigation to OAuth Native
    LaunchedEffect(navigateToOAuthNative) {
        if (navigateToOAuthNative) {
            onNavigateToOAuthNative()
            viewModel.onOAuthNativeNavigationHandled()
        }
    }

    // Handle navigation to OAuth Web
    LaunchedEffect(navigateToOAuthWeb) {
        if (navigateToOAuthWeb) {
            onNavigateToOAuthWeb()
            viewModel.onOAuthWebNavigationHandled()
        }
    }

    // Handle navigation to Magic Link
    LaunchedEffect(navigateToMagicLink) {
        if (navigateToMagicLink) {
            onNavigateToMagicLink()
            viewModel.onMagicLinkNavigationHandled()
        }
    }

    // Handle navigation to Enchanted Link
    LaunchedEffect(navigateToEnchantedLink) {
        if (navigateToEnchantedLink) {
            onNavigateToEnchantedLink()
            viewModel.onEnchantedLinkNavigationHandled()
        }
    }

    // Handle navigation to Password
    LaunchedEffect(navigateToPassword) {
        if (navigateToPassword) {
            onNavigateToPassword()
            viewModel.onPasswordNavigationHandled()
        }
    }

    // Handle navigation to Passkey
    LaunchedEffect(navigateToPasskey) {
        if (navigateToPasskey) {
            onNavigateToPasskey()
            viewModel.onPasskeyNavigationHandled()
        }
    }

    // Handle navigation to TOTP
    LaunchedEffect(navigateToTotp) {
        if (navigateToTotp) {
            onNavigateToTotp()
            viewModel.onTotpNavigationHandled()
        }
    }

    // Show error in styled snackbar
    LaunchedEffect(error) {
        error?.let {
            snackbarData.value = StyledSnackbarData(it, SnackbarType.ERROR)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Title
                Text(
                    text = "Descope Sample App",
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Android SDK Demo & Testing",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Sign in with Flow button
                Button(
                    onClick = { viewModel.startFlowAuth() },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimensions.ButtonHeight)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(Dimensions.LoadingIndicatorSize),
                            strokeWidth = Dimensions.LoadingIndicatorStrokeWidth,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Login,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            RowSpacer(modifier = Modifier.width(8.dp))
                            Text("Sign in with Flow")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // API Authentication button (opens bottom sheet)
                OutlinedButton(
                    onClick = { viewModel.showApiOptionsSheet() },
                    enabled = !isLoading,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimensions.ButtonHeight)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Key,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        RowSpacer(modifier = Modifier.width(8.dp))
                        Text("API Authentication")
                    }
                }
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

        // API Options Bottom Sheet
        OptionsBottomSheet(
            isVisible = showApiOptions,
            onDismiss = { viewModel.hideApiOptionsSheet() },
            headerConfig = BottomSheetHeaderConfig(
                title = "API Authentication",
                subtitle = "Choose your preferred sign-in method",
                icon = Icons.Filled.Key,
                iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                iconTint = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            options = AuthMethod.entries.map { it.toBottomSheetOption() },
            onOptionSelected = { option ->
                AuthMethod.entries.find { it.name == option.id }?.let { method ->
                    viewModel.onAuthMethodSelected(method)
                }
            }
        )

        // Flow Options Bottom Sheet
        OptionsBottomSheet(
            isVisible = showFlowOptions,
            onDismiss = { viewModel.hideFlowOptionsSheet() },
            headerConfig = BottomSheetHeaderConfig(
                title = "Select Flow",
                subtitle = "Choose a flow to start authentication",
                icon = Icons.Filled.AccountTree,
                iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                iconTint = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            options = viewModel.availableFlows.map { flowId ->
                BottomSheetOption(
                    id = flowId,
                    title = flowId,
                    icon = Icons.AutoMirrored.Filled.Login,
                    iconTint = MaterialTheme.colorScheme.primary
                )
            },
            onOptionSelected = { option ->
                viewModel.onFlowSelected(option.id, onNavigateToFlow)
            }
        )
    }
}

/**
 * Extension function to convert AuthMethod to BottomSheetOption
 */
@Composable
private fun AuthMethod.toBottomSheetOption(): BottomSheetOption = BottomSheetOption(
    id = this.name,
    title = this.displayName,
    subtitle = this.description(),
    icon = this.icon()
)

@Composable
private fun AuthMethod.icon(): ImageVector = when (this) {
    AuthMethod.OTP -> Icons.Filled.Password
    AuthMethod.MAGIC_LINK -> Icons.Filled.MailOutline
    AuthMethod.ENCHANTED_LINK -> Icons.Filled.Link
    AuthMethod.OAUTH -> Icons.Filled.People
    AuthMethod.OAUTH_NATIVE -> Icons.Filled.People
    AuthMethod.PASSWORD -> Icons.Filled.Lock
    AuthMethod.PASSKEY -> Icons.Filled.Fingerprint
    AuthMethod.TOTP -> Icons.Filled.Timer
}

@Composable
private fun AuthMethod.description(): String = when (this) {
    AuthMethod.OTP -> "Receive a code via email or SMS"
    AuthMethod.MAGIC_LINK -> "Get a sign-in link in your email"
    AuthMethod.ENCHANTED_LINK -> "Approve sign-in from another device"
    AuthMethod.OAUTH -> "Sign in with Google, Apple, etc."
    AuthMethod.OAUTH_NATIVE -> "Native Google Sign-In via Credential Manager"
    AuthMethod.PASSWORD -> "Use your email and password"
    AuthMethod.PASSKEY -> "Biometric or device authentication"
    AuthMethod.TOTP -> "Use your authenticator app"
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    DescopeTheme {
        // Preview placeholder
    }
}
