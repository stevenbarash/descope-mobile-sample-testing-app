package com.descope.testapp.ui.screens.magiclink

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material.icons.outlined.Phone
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.descope.testapp.ui.components.SnackbarType
import com.descope.testapp.ui.components.StyledSnackbar
import com.descope.testapp.ui.components.StyledSnackbarData
import com.descope.testapp.ui.screens.AuthMode
import com.descope.testapp.ui.theme.DescopeTheme
import com.descope.testapp.ui.theme.Dimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MagicLinkScreen(
    viewModel: MagicLinkViewModel,
    onNavigateBack: () -> Unit,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val deliveryMethod by viewModel.deliveryMethod.collectAsState()
    val inputValue by viewModel.inputValue.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val lastUsedDeliveryMethod by viewModel.lastUsedDeliveryMethod.collectAsState()
    val lastUsedValue by viewModel.lastUsedValue.collectAsState()
    val mode by viewModel.mode.collectAsState()

    val isUpdateMode = mode == AuthMode.Update

    val snackbarData = remember { mutableStateOf<StyledSnackbarData?>(null) }

    // Register viewmodel with callback holder for deep link handling
    DisposableEffect(viewModel) {
        MagicLinkCallbackHolder.setActiveViewModel(viewModel)
        onDispose {
            MagicLinkCallbackHolder.clear()
        }
    }

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
                    IconButton(
                        onClick = {
                            if (currentStep == MagicLinkStep.WAITING) {
                                viewModel.goBackToInput()
                            } else {
                                onNavigateBack()
                            }
                        }
                    ) {
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
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState == MagicLinkStep.WAITING) {
                        (slideInHorizontally { it } + fadeIn()) togetherWith
                                (slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()) togetherWith
                                (slideOutHorizontally { it } + fadeOut())
                    }
                },
                label = "step_transition",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { step ->
                when (step) {
                    MagicLinkStep.INPUT -> MagicLinkInputStep(
                        deliveryMethod = deliveryMethod,
                        inputValue = inputValue,
                        isLoading = isLoading,
                        lastUsedDeliveryMethod = lastUsedDeliveryMethod,
                        lastUsedValue = lastUsedValue,
                        isUpdateMode = isUpdateMode,
                        onDeliveryMethodChange = { viewModel.setDeliveryMethod(it) },
                        onInputChange = { viewModel.setInputValue(it) },
                        onSendMagicLink = { viewModel.sendMagicLink() }
                    )
                    MagicLinkStep.WAITING -> MagicLinkWaitingStep(
                        destination = inputValue,
                        isLoading = isLoading,
                        isUpdateMode = isUpdateMode,
                        onResend = { viewModel.resendMagicLink() }
                    )
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
    }
}

@Composable
private fun MagicLinkInputStep(
    deliveryMethod: MagicLinkDeliveryMethod,
    inputValue: String,
    isLoading: Boolean,
    lastUsedDeliveryMethod: MagicLinkDeliveryMethod?,
    lastUsedValue: String?,
    isUpdateMode: Boolean,
    onDeliveryMethodChange: (MagicLinkDeliveryMethod) -> Unit,
    onInputChange: (String) -> Unit,
    onSendMagicLink: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    // Check if there's a matching last used value for the current delivery method
    // Disable hints and autofill in update mode
    val hasMatchingLastUsed = !isUpdateMode && lastUsedDeliveryMethod == deliveryMethod && !lastUsedValue.isNullOrEmpty()
    val placeholderText = if (hasMatchingLastUsed) {
        "$lastUsedValue (last used)"
    } else {
        when (deliveryMethod) {
            MagicLinkDeliveryMethod.EMAIL -> "you@example.com"
            MagicLinkDeliveryMethod.PHONE -> "+1 (555) 123-4567"
        }
    }

    // Can submit if there's input OR there's a matching last used value (not in update mode)
    val canSubmit = inputValue.isNotBlank() || hasMatchingLastUsed

    // Title based on mode
    val title = if (isUpdateMode) {
        when (deliveryMethod) {
            MagicLinkDeliveryMethod.EMAIL -> "Add Email"
            MagicLinkDeliveryMethod.PHONE -> "Add Phone"
        }
    } else {
        "Sign In with Magic Link"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimensions.ScreenHorizontalPadding)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

        // Hero Icon
        HeroIcon(
            icon = when (deliveryMethod) {
                MagicLinkDeliveryMethod.EMAIL -> Icons.Filled.MailOutline
                MagicLinkDeliveryMethod.PHONE -> Icons.Outlined.Phone
            },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            iconColor = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingXLarge))

        // Modern delivery method selector
        DeliveryMethodSelector(
            selectedMethod = deliveryMethod,
            onMethodSelected = onDeliveryMethodChange,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

        // Input field with modern styling
        OutlinedTextField(
            value = inputValue,
            onValueChange = onInputChange,
            label = {
                Text(
                    when (deliveryMethod) {
                        MagicLinkDeliveryMethod.EMAIL -> "Email Address"
                        MagicLinkDeliveryMethod.PHONE -> "Phone Number"
                    }
                )
            },
            placeholder = {
                Text(
                    text = placeholderText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = when (deliveryMethod) {
                        MagicLinkDeliveryMethod.EMAIL -> Icons.Outlined.Email
                        MagicLinkDeliveryMethod.PHONE -> Icons.Outlined.Phone
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = when (deliveryMethod) {
                    MagicLinkDeliveryMethod.EMAIL -> KeyboardType.Email
                    MagicLinkDeliveryMethod.PHONE -> KeyboardType.Phone
                },
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

        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

        // Send Magic Link button with icon
        Button(
            onClick = {
                keyboardController?.hide()
                onSendMagicLink()
            },
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
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Send Magic Link",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
    }
}

@Composable
private fun MagicLinkWaitingStep(
    destination: String,
    isLoading: Boolean,
    isUpdateMode: Boolean,
    onResend: () -> Unit
) {
    // Animated pulsing effect for the icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimensions.ScreenHorizontalPadding)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

        // Animated Hero Icon
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(88.dp)
                .scale(scale)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    ambientColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                    spotColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                        )
                    )
                )
        ) {
            Icon(
                imageVector = Icons.Outlined.MarkEmailRead,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

        // Title
        Text(
            text = "Check Your Inbox",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

        // Destination in a pill
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = destination,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.SpacingXLarge))

        // Instructions card
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Click the link in your email to sign in",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "The link will expire in 10 minutes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimensions.SpacingXLarge))

        // Loading indicator while waiting
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
            Text(
                text = "Verifying...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Resend button
        TextButton(
            onClick = onResend,
            enabled = !isLoading
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Didn't receive the email? Resend",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isLoading)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                else
                    MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
    }
}

/**
 * Hero icon displayed at the top of each step
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

/**
 * Modern delivery method selector with animated selection
 */
@Composable
private fun DeliveryMethodSelector(
    selectedMethod: MagicLinkDeliveryMethod,
    onMethodSelected: (MagicLinkDeliveryMethod) -> Unit,
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
            MagicLinkDeliveryMethod.entries.forEach { method ->
                DeliveryMethodOption(
                    method = method,
                    isSelected = selectedMethod == method,
                    onClick = { onMethodSelected(method) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DeliveryMethodOption(
    method: MagicLinkDeliveryMethod,
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
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (method) {
                    MagicLinkDeliveryMethod.EMAIL -> if (isSelected) Icons.Filled.Email else Icons.Outlined.Email
                    MagicLinkDeliveryMethod.PHONE -> if (isSelected) Icons.Filled.Phone else Icons.Outlined.Phone
                },
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = method.displayName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = contentColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MagicLinkInputStepPreview() {
    DescopeTheme {
        MagicLinkInputStep(
            deliveryMethod = MagicLinkDeliveryMethod.EMAIL,
            inputValue = "",
            isLoading = false,
            lastUsedDeliveryMethod = null,
            lastUsedValue = null,
            isUpdateMode = false,
            onDeliveryMethodChange = {},
            onInputChange = {},
            onSendMagicLink = {}
        )
    }
}

@Preview(showBackground = true, name = "With Last Used Email")
@Composable
private fun MagicLinkInputStepWithLastUsedPreview() {
    DescopeTheme {
        MagicLinkInputStep(
            deliveryMethod = MagicLinkDeliveryMethod.EMAIL,
            inputValue = "",
            isLoading = false,
            lastUsedDeliveryMethod = MagicLinkDeliveryMethod.EMAIL,
            lastUsedValue = "user@example.com",
            isUpdateMode = false,
            onDeliveryMethodChange = {},
            onInputChange = {},
            onSendMagicLink = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MagicLinkWaitingStepPreview() {
    DescopeTheme {
        MagicLinkWaitingStep(
            destination = "user@example.com",
            isLoading = false,
            isUpdateMode = false,
            onResend = {}
        )
    }
}
