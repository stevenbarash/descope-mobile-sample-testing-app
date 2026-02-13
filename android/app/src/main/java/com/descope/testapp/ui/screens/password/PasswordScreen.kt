package com.descope.testapp.ui.screens.password

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.descope.testapp.ui.components.SnackbarType
import com.descope.testapp.ui.components.StyledSnackbar
import com.descope.testapp.ui.components.StyledSnackbarData
import com.descope.testapp.ui.screens.AuthMode
import com.descope.testapp.ui.theme.DescopeTheme
import com.descope.testapp.ui.theme.Dimensions
import com.descope.types.PasswordPolicy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordScreen(
    viewModel: PasswordViewModel,
    onNavigateBack: () -> Unit,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentAction by viewModel.currentAction.collectAsState()
    val emailValue by viewModel.emailValue.collectAsState()
    val passwordValue by viewModel.passwordValue.collectAsState()
    val oldPasswordValue by viewModel.oldPasswordValue.collectAsState()
    val newPasswordValue by viewModel.newPasswordValue.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val passwordPolicy by viewModel.passwordPolicy.collectAsState()
    val isPolicyLoading by viewModel.isPolicyLoading.collectAsState()
    val lastUsedEmail by viewModel.lastUsedEmail.collectAsState()
    val mode by viewModel.mode.collectAsState()

    val isUpdateMode = mode == AuthMode.Update

    val snackbarData = remember { mutableStateOf<StyledSnackbarData?>(null) }

    // Handle authentication success
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            onAuthSuccess()
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimensions.ScreenHorizontalPadding)
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

                // Hero Icon
                HeroIcon(
                    icon = Icons.Filled.Lock,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconColor = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

                // Title
                Text(
                    text = if (isUpdateMode) "Update Password" else "Password",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

                // Password Policy Card
                PasswordPolicyCard(
                    policy = passwordPolicy,
                    isLoading = isPolicyLoading,
                    currentPassword = when (currentAction) {
                        PasswordAction.SIGN_IN -> passwordValue
                        PasswordAction.SIGN_UP -> passwordValue
                        PasswordAction.REPLACE -> newPasswordValue
                    }
                )

                Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

                // Action selector (hide in update mode)
                if (!isUpdateMode) {
                    ActionSelector(
                        selectedAction = currentAction,
                        onActionSelected = { viewModel.setCurrentAction(it) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
                }

                // Form fields based on current action
                // Form fields based on current action
                PasswordForm(
                    currentAction = currentAction,
                    emailValue = emailValue,
                    passwordValue = passwordValue,
                    oldPasswordValue = oldPasswordValue,
                    newPasswordValue = newPasswordValue,
                    lastUsedEmail = lastUsedEmail,
                    isLoading = isLoading,
                    isUpdateMode = isUpdateMode,
                    onEmailChange = { viewModel.setEmailValue(it) },
                    onPasswordChange = { viewModel.setPasswordValue(it) },
                    onOldPasswordChange = { viewModel.setOldPasswordValue(it) },
                    onNewPasswordChange = { viewModel.setNewPasswordValue(it) },
                    onSubmit = { viewModel.executeAction() }
                )

                Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
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

@Composable
private fun PasswordPolicyCard(
    policy: PasswordPolicy?,
    isLoading: Boolean,
    currentPassword: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Password Requirements",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Loading policy...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (policy != null) {
                PolicyRequirement(
                    text = "At least ${policy.minLength} characters",
                    isMet = currentPassword.length >= policy.minLength,
                    isActive = currentPassword.isNotEmpty()
                )
                if (policy.lowercase) {
                    PolicyRequirement(
                        text = "One lowercase letter",
                        isMet = currentPassword.any { it.isLowerCase() },
                        isActive = currentPassword.isNotEmpty()
                    )
                }
                if (policy.uppercase) {
                    PolicyRequirement(
                        text = "One uppercase letter",
                        isMet = currentPassword.any { it.isUpperCase() },
                        isActive = currentPassword.isNotEmpty()
                    )
                }
                if (policy.number) {
                    PolicyRequirement(
                        text = "One number",
                        isMet = currentPassword.any { it.isDigit() },
                        isActive = currentPassword.isNotEmpty()
                    )
                }
                if (policy.nonAlphanumeric) {
                    PolicyRequirement(
                        text = "One special character",
                        isMet = !currentPassword.all { it.isLetterOrDigit() } && currentPassword.isNotEmpty(),
                        isActive = currentPassword.isNotEmpty()
                    )
                }
            } else {
                Text(
                    text = "Password policy not available",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PolicyRequirement(
    text: String,
    isMet: Boolean,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val iconColor by animateColorAsState(
        targetValue = when {
            !isActive -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            isMet -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.error
        },
        animationSpec = tween(200),
        label = "iconColor"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            !isActive -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            isMet -> MaterialTheme.colorScheme.onSurface
            else -> MaterialTheme.colorScheme.error
        },
        animationSpec = tween(200),
        label = "textColor"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = if (isMet && isActive) Icons.Filled.Check else Icons.Filled.Close,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = textColor
        )
    }
}

@Composable
private fun ActionSelector(
    selectedAction: PasswordAction,
    onActionSelected: (PasswordAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PasswordAction.entries.forEach { action ->
                ActionOption(
                    action = action,
                    isSelected = selectedAction == action,
                    onClick = { onActionSelected(action) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ActionOption(
    action: PasswordAction,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            Color.Transparent,
        animationSpec = tween(200),
        label = "backgroundColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.onPrimary
        else
            MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "contentColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.95f,
        animationSpec = tween(200),
        label = "scale"
    )

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = action.displayName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp)
        )
    }
}

@Composable
private fun PasswordForm(
    currentAction: PasswordAction,
    emailValue: String,
    passwordValue: String,
    oldPasswordValue: String,
    newPasswordValue: String,
    lastUsedEmail: String?,
    isLoading: Boolean,
    isUpdateMode: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onOldPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }

    // Disable hints and autofill in update mode
    val hasLastUsed = !isUpdateMode && !lastUsedEmail.isNullOrEmpty()
    val emailPlaceholder = if (hasLastUsed) "$lastUsedEmail (last used)" else "you@example.com"

    // Determine if form can be submitted
    val canSubmit = if (isUpdateMode) {
        newPasswordValue.isNotBlank()
    } else {
        when (currentAction) {
            PasswordAction.SIGN_IN -> (emailValue.isNotBlank() || hasLastUsed) && passwordValue.isNotBlank()
            PasswordAction.SIGN_UP -> (emailValue.isNotBlank() || hasLastUsed) && passwordValue.isNotBlank()
            PasswordAction.REPLACE -> (emailValue.isNotBlank() || hasLastUsed) && oldPasswordValue.isNotBlank() && newPasswordValue.isNotBlank()
        }
    }

    // Button text based on mode and action
    val buttonText = if (isUpdateMode) {
        "Update Password"
    } else {
        currentAction.displayName
    }

    Column {
        // Email field (hide in update mode - user is already authenticated)
        if (!isUpdateMode) {
            OutlinedTextField(
                value = emailValue,
                onValueChange = onEmailChange,
                label = { Text("Email Address") },
                placeholder = {
                    Text(
                        text = emailPlaceholder,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
        }

        // In update mode, show only new password field
        if (isUpdateMode) {
            OutlinedTextField(
                value = newPasswordValue,
                onValueChange = onNewPasswordChange,
                label = { Text("New Password") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                        Icon(
                            imageVector = if (newPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (newPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            when (currentAction) {
                PasswordAction.SIGN_IN, PasswordAction.SIGN_UP -> {
                    // Single password field
                    OutlinedTextField(
                        value = passwordValue,
                        onValueChange = onPasswordChange,
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            PasswordAction.REPLACE -> {
                // Old password field
                OutlinedTextField(
                    value = oldPasswordValue,
                    onValueChange = onOldPasswordChange,
                    label = { Text("Current Password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) {
                            Icon(
                                imageVector = if (oldPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (oldPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    visualTransformation = if (oldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

                // New password field
                OutlinedTextField(
                    value = newPasswordValue,
                    onValueChange = onNewPasswordChange,
                    label = { Text("New Password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(
                                imageVector = if (newPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (newPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        } // End of else block

        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

        // Submit button
        Button(
            onClick = onSubmit,
            enabled = !isLoading && canSubmit,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
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
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Hero icon displayed at the top
 */
@Composable
private fun HeroIcon(
    icon: ImageVector,
    containerColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(88.dp)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = containerColor.copy(alpha = 0.3f),
                spotColor = containerColor.copy(alpha = 0.3f)
            )
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        containerColor,
                        containerColor.copy(alpha = 0.8f)
                    )
                )
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(40.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PasswordPolicyCardPreview() {
    DescopeTheme {
        PasswordPolicyCard(
            policy = PasswordPolicy(
                minLength = 8,
                lowercase = true,
                uppercase = true,
                number = true,
                nonAlphanumeric = true
            ),
            isLoading = false,
            currentPassword = "Test1"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ActionSelectorPreview() {
    DescopeTheme {
        ActionSelector(
            selectedAction = PasswordAction.SIGN_IN,
            onActionSelected = {}
        )
    }
}
