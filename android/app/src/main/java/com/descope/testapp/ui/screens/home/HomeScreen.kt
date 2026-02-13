package com.descope.testapp.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer as RowSpacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
import com.descope.types.DescopeUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onLogout: () -> Unit,
    onNavigateToFlow: (String) -> Unit,
    onNavigateToUpdateOtp: () -> Unit,
    onNavigateToUpdateMagicLink: () -> Unit,
    onNavigateToUpdateEnchantedLink: () -> Unit,
    onNavigateToUpdatePassword: () -> Unit,
    onNavigateToUpdatePasskey: () -> Unit,
    onNavigateToUpdateTotp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val user by viewModel.user.collectAsState()
    val sessionStatus by viewModel.sessionStatus.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isRefreshingUser by viewModel.isRefreshingUser.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val showAuthenticatedFlowOptions by viewModel.showAuthenticatedFlowOptions.collectAsState()
    val showUpdateAuthOptions by viewModel.showUpdateAuthOptions.collectAsState()

    // Navigation observers for update auth methods
    val navigateToUpdateOtp by viewModel.navigateToUpdateOtp.collectAsState()
    val navigateToUpdateMagicLink by viewModel.navigateToUpdateMagicLink.collectAsState()
    val navigateToUpdateEnchantedLink by viewModel.navigateToUpdateEnchantedLink.collectAsState()
    val navigateToUpdatePassword by viewModel.navigateToUpdatePassword.collectAsState()
    val navigateToUpdatePasskey by viewModel.navigateToUpdatePasskey.collectAsState()
    val navigateToUpdateTotp by viewModel.navigateToUpdateTotp.collectAsState()

    val snackbarData = remember { mutableStateOf<StyledSnackbarData?>(null) }

    // Handle navigation to update screens
    LaunchedEffect(navigateToUpdateOtp) {
        if (navigateToUpdateOtp) {
            onNavigateToUpdateOtp()
            viewModel.onUpdateOtpNavigationHandled()
        }
    }

    LaunchedEffect(navigateToUpdateMagicLink) {
        if (navigateToUpdateMagicLink) {
            onNavigateToUpdateMagicLink()
            viewModel.onUpdateMagicLinkNavigationHandled()
        }
    }

    LaunchedEffect(navigateToUpdateEnchantedLink) {
        if (navigateToUpdateEnchantedLink) {
            onNavigateToUpdateEnchantedLink()
            viewModel.onUpdateEnchantedLinkNavigationHandled()
        }
    }

    LaunchedEffect(navigateToUpdatePassword) {
        if (navigateToUpdatePassword) {
            onNavigateToUpdatePassword()
            viewModel.onUpdatePasswordNavigationHandled()
        }
    }

    LaunchedEffect(navigateToUpdatePasskey) {
        if (navigateToUpdatePasskey) {
            onNavigateToUpdatePasskey()
            viewModel.onUpdatePasskeyNavigationHandled()
        }
    }

    LaunchedEffect(navigateToUpdateTotp) {
        if (navigateToUpdateTotp) {
            onNavigateToUpdateTotp()
            viewModel.onUpdateTotpNavigationHandled()
        }
    }

    // Show error in styled snackbar
    LaunchedEffect(error) {
        error?.let {
            snackbarData.value = StyledSnackbarData(it, SnackbarType.ERROR)
            viewModel.clearError()
        }
    }

    // Show success message in styled snackbar
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarData.value = StyledSnackbarData(it, SnackbarType.SUCCESS)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        var showRefreshMenu by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top bar with action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logout button - left aligned with primary outline
                    OutlinedButton(
                        onClick = onLogout,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        RowSpacer(modifier = Modifier.width(6.dp))
                        Text("Log out")
                    }

                    // Right side - FAB-like refresh menu with vertical dropdown
                    Box {
                        // Toggle button with refresh icon
                        FilledIconButton(
                            onClick = { showRefreshMenu = !showRefreshMenu },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = if (showRefreshMenu) Icons.Filled.Close else Icons.Filled.Refresh,
                                contentDescription = if (showRefreshMenu) "Close" else "Refresh options",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Dropdown with styled button-like options
                        DropdownMenu(
                            expanded = showRefreshMenu,
                            onDismissRequest = { showRefreshMenu = false }
                        ) {
                            // Refresh Session option
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (isRefreshing) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        } else {
                                            Icon(
                                                Icons.Filled.Refresh,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Text(
                                            "Refresh Session",
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.refresh()
                                    showRefreshMenu = false
                                },
                                enabled = !isRefreshing
                            )

                            // Refresh User option
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (isRefreshingUser) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        } else {
                                            Icon(
                                                Icons.Filled.PersonSearch,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Text(
                                            "Refresh User",
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.refreshUser()
                                    showRefreshMenu = false
                                },
                                enabled = !isRefreshingUser
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // User info card
                UserInfoCard(user = user, sessionStatus = sessionStatus)

                Spacer(modifier = Modifier.height(32.dp))

                // Run Authenticated Flow button - primary style
                Button(
                    onClick = { viewModel.showAuthenticatedFlowOptionsSheet() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimensions.ButtonHeight)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        RowSpacer(modifier = Modifier.width(8.dp))
                        Text("Run Authenticated Flow")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Update User via API button - secondary style
                OutlinedButton(
                    onClick = { viewModel.showUpdateAuthOptionsSheet() },
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
                        Text("Update User via API")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
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

        // Authenticated Flow Options Bottom Sheet
        OptionsBottomSheet(
            isVisible = showAuthenticatedFlowOptions,
            onDismiss = { viewModel.hideAuthenticatedFlowOptionsSheet() },
            headerConfig = BottomSheetHeaderConfig(
                title = "Authenticated Flow",
                subtitle = "Run a flow with your current session",
                icon = Icons.Filled.PlayArrow,
                iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                iconTint = MaterialTheme.colorScheme.onTertiaryContainer
            ),
            options = viewModel.availableFlows.map { flowId ->
                BottomSheetOption(
                    id = flowId,
                    title = flowId,
                    icon = Icons.AutoMirrored.Filled.Login,
                    iconTint = MaterialTheme.colorScheme.tertiary
                )
            },
            onOptionSelected = { option ->
                viewModel.onAuthenticatedFlowSelected(option.id, onNavigateToFlow)
            }
        )

        // Update Auth Options Bottom Sheet
        OptionsBottomSheet(
            isVisible = showUpdateAuthOptions,
            onDismiss = { viewModel.hideUpdateAuthOptionsSheet() },
            headerConfig = BottomSheetHeaderConfig(
                title = "Update User",
                subtitle = "Add or update authentication methods",
                icon = Icons.Filled.Key,
                iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                iconTint = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            options = UpdateAuthMethod.entries.map { it.toBottomSheetOption() },
            onOptionSelected = { option ->
                UpdateAuthMethod.entries.find { it.name == option.id }?.let { method ->
                    viewModel.onUpdateAuthMethodSelected(method)
                }
            }
        )
    }
}

/**
 * Extension function to convert UpdateAuthMethod to BottomSheetOption
 */
@Composable
private fun UpdateAuthMethod.toBottomSheetOption(): BottomSheetOption = BottomSheetOption(
    id = this.name,
    title = this.displayName,
    subtitle = this.description(),
    icon = this.icon()
)

@Composable
private fun UpdateAuthMethod.icon(): ImageVector = when (this) {
    UpdateAuthMethod.OTP -> Icons.Filled.Password
    UpdateAuthMethod.MAGIC_LINK -> Icons.Filled.MailOutline
    UpdateAuthMethod.ENCHANTED_LINK -> Icons.Filled.Link
    UpdateAuthMethod.PASSWORD -> Icons.Filled.Lock
    UpdateAuthMethod.PASSKEY -> Icons.Filled.Fingerprint
    UpdateAuthMethod.TOTP -> Icons.Filled.Timer
}

@Composable
private fun UpdateAuthMethod.description(): String = when (this) {
    UpdateAuthMethod.OTP -> "Add email or phone via OTP"
    UpdateAuthMethod.MAGIC_LINK -> "Add email or phone via magic link"
    UpdateAuthMethod.ENCHANTED_LINK -> "Add email via enchanted link"
    UpdateAuthMethod.PASSWORD -> "Update your password"
    UpdateAuthMethod.PASSKEY -> "Add a new passkey"
    UpdateAuthMethod.TOTP -> "Set up authenticator app"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserInfoCard(
    user: DescopeUser?,
    sessionStatus: SessionStatus?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.CardPaddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Compact header with icon and session status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                // Session Status badge
                sessionStatus?.let { status ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (status.isExpired) "Expired" else "Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (status.isExpired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        status.timeRemainingSeconds?.let { seconds ->
                            if (!status.isExpired) {
                                Text(
                                    text = "• ${formatTimeRemainingCompact(seconds)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (seconds < 60) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (user != null) {
                // User details in a compact grid
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Name and Email on one line if possible
                    user.name?.let { name ->
                        UserInfoRow(label = "Name", value = name)
                    }

                    user.email?.let { email ->
                        UserInfoRow(
                            label = "Email",
                            value = email,
                            verified = user.isVerifiedEmail
                        )
                    }

                    user.phone?.let { phone ->
                        UserInfoRow(
                            label = "Phone",
                            value = phone,
                            verified = user.isVerifiedPhone
                        )
                    }

                    // User ID (truncated)
                    UserInfoRow(
                        label = "ID",
                        value = if (user.userId.length > 20) "${user.userId.take(20)}..." else user.userId
                    )

                    // Authentication methods section
                    val authMethods = buildList {
                        if (user.authentication.passkey) add("Passkey")
                        if (user.authentication.password) add("Password")
                        if (user.authentication.totp) add("TOTP")
                        if (user.authentication.sso) add("SSO")
                        user.authentication.oauth.forEach { provider ->
                            add("OAuth: $provider")
                        }
                    }

                    if (authMethods.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Authentication Methods",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            authMethods.forEach { method ->
                                AssistChip(
                                    onClick = { },
                                    label = {
                                        Text(
                                            text = method,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    modifier = Modifier.height(24.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                    ),
                                    border = null
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "No user information available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun UserInfoRow(
    label: String,
    value: String,
    verified: Boolean? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall
            )
            verified?.let {
                if (it) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Formats the remaining seconds into a compact string.
 */
private fun formatTimeRemainingCompact(seconds: Long): String {
    if (seconds <= 0) return "expired"

    val days = seconds / 86400
    val hours = (seconds % 86400) / 3600
    val minutes = (seconds % 3600) / 60

    return when {
        days > 0 -> "${days}d ${hours}h"
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "${seconds}s"
    }
}


@Preview(showBackground = true)
@Composable
private fun UserInfoCardPreview() {
    DescopeTheme {
        UserInfoCard(user = null, sessionStatus = null)
    }
}
