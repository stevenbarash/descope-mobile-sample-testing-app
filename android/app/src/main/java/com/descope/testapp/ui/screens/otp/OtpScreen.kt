package com.descope.testapp.ui.screens.otp

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Email
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.descope.testapp.ui.components.CodeInput
import com.descope.testapp.ui.components.SnackbarType
import com.descope.testapp.ui.components.StyledSnackbar
import com.descope.testapp.ui.components.StyledSnackbarData
import com.descope.testapp.ui.screens.AuthMode
import com.descope.testapp.ui.theme.DescopeTheme
import com.descope.testapp.ui.theme.Dimensions

private const val OTP_LENGTH = 6

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    viewModel: OtpViewModel,
    onNavigateBack: () -> Unit,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val deliveryMethod by viewModel.deliveryMethod.collectAsState()
    val inputValue by viewModel.inputValue.collectAsState()
    val otpCode by viewModel.otpCode.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val lastUsedDeliveryMethod by viewModel.lastUsedDeliveryMethod.collectAsState()
    val lastUsedValue by viewModel.lastUsedValue.collectAsState()
    val mode by viewModel.mode.collectAsState()

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
                    IconButton(
                        onClick = {
                            if (currentStep == OtpStep.VERIFY) {
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
                    if (targetState == OtpStep.VERIFY) {
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
                    OtpStep.INPUT -> OtpInputStep(
                        deliveryMethod = deliveryMethod,
                        inputValue = inputValue,
                        isLoading = isLoading,
                        lastUsedDeliveryMethod = lastUsedDeliveryMethod,
                        lastUsedValue = lastUsedValue,
                        isUpdateMode = mode == AuthMode.Update,
                        onDeliveryMethodChange = { viewModel.setDeliveryMethod(it) },
                        onInputChange = { viewModel.setInputValue(it) },
                        onSendOtp = { viewModel.sendOtp() }
                    )
                    OtpStep.VERIFY -> OtpVerifyStep(
                        destination = inputValue,
                        otpCode = otpCode,
                        isLoading = isLoading,
                        isUpdateMode = mode == AuthMode.Update,
                        onOtpCodeChange = { viewModel.setOtpCode(it) },
                        onVerify = { viewModel.verifyOtp() },
                        onResend = { viewModel.resendOtp() }
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
private fun OtpInputStep(
    deliveryMethod: OtpDeliveryMethod,
    inputValue: String,
    isLoading: Boolean,
    lastUsedDeliveryMethod: OtpDeliveryMethod?,
    lastUsedValue: String?,
    isUpdateMode: Boolean,
    onDeliveryMethodChange: (OtpDeliveryMethod) -> Unit,
    onInputChange: (String) -> Unit,
    onSendOtp: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    // Check if there's a matching last used value for the current delivery method
    // Disable hints and autofill in update mode
    val hasMatchingLastUsed = !isUpdateMode && lastUsedDeliveryMethod == deliveryMethod && !lastUsedValue.isNullOrEmpty()
    val placeholderText = if (hasMatchingLastUsed) {
        "$lastUsedValue (last used)"
    } else {
        when (deliveryMethod) {
            OtpDeliveryMethod.EMAIL -> "you@example.com"
            OtpDeliveryMethod.PHONE -> "+1 (555) 123-4567"
        }
    }

    // Can submit if there's input OR there's a matching last used value (not in update mode)
    val canSubmit = inputValue.isNotBlank() || hasMatchingLastUsed

    // Title based on mode
    val title = if (isUpdateMode) {
        when (deliveryMethod) {
            OtpDeliveryMethod.EMAIL -> "Add Email"
            OtpDeliveryMethod.PHONE -> "Add Phone"
        }
    } else {
        "Sign In with OTP"
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
                OtpDeliveryMethod.EMAIL -> Icons.Outlined.Email
                OtpDeliveryMethod.PHONE -> Icons.Outlined.Phone
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
                        OtpDeliveryMethod.EMAIL -> "Email Address"
                        OtpDeliveryMethod.PHONE -> "Phone Number"
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
                        OtpDeliveryMethod.EMAIL -> Icons.Outlined.Email
                        OtpDeliveryMethod.PHONE -> Icons.Outlined.Phone
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = when (deliveryMethod) {
                    OtpDeliveryMethod.EMAIL -> KeyboardType.Email
                    OtpDeliveryMethod.PHONE -> KeyboardType.Phone
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

        // Send OTP button with icon
        Button(
            onClick = {
                keyboardController?.hide()
                onSendOtp()
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
                    text = "Send Code",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
    }
}

@Composable
private fun OtpVerifyStep(
    destination: String,
    otpCode: String,
    isLoading: Boolean,
    isUpdateMode: Boolean,
    onOtpCodeChange: (String) -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // Auto-focus the OTP input when this step is shown
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    // Auto-verify when all digits are entered
    LaunchedEffect(otpCode) {
        if (otpCode.length == OTP_LENGTH) {
            keyboardController?.hide()
            onVerify()
        }
    }

    val title = if (isUpdateMode) "Verify Update" else "Verification Code"
    val buttonText = if (isUpdateMode) "Confirm" else "Verify"

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
            icon = Icons.Filled.Lock,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            iconColor = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

        // Title
        Text(
            text = title,
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

        // OTP Code Input
        CodeInput(
            code = otpCode,
            onCodeChange = onOtpCodeChange,
            enabled = !isLoading,
            modifier = Modifier.focusRequester(focusRequester)
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

        // Verify button
        Button(
            onClick = onVerify,
            enabled = !isLoading && otpCode.length == OTP_LENGTH,
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
                    imageVector = Icons.Filled.Check,
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

        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

        // Resend button
        TextButton(
            onClick = onResend,
            enabled = !isLoading
        ) {
            Text(
                text = "Didn't receive the code? Resend",
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
    selectedMethod: OtpDeliveryMethod,
    onMethodSelected: (OtpDeliveryMethod) -> Unit,
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
            OtpDeliveryMethod.entries.forEach { method ->
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
    method: OtpDeliveryMethod,
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
                    OtpDeliveryMethod.EMAIL -> if (isSelected) Icons.Filled.Email else Icons.Outlined.Email
                    OtpDeliveryMethod.PHONE -> if (isSelected) Icons.Filled.Phone else Icons.Outlined.Phone
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

/**
 * Preview functions
 */

@Preview(showBackground = true)
@Composable
private fun OtpInputStepPreview() {
    DescopeTheme {
        OtpInputStep(
            deliveryMethod = OtpDeliveryMethod.EMAIL,
            inputValue = "",
            isLoading = false,
            lastUsedDeliveryMethod = null,
            lastUsedValue = null,
            isUpdateMode = false,
            onDeliveryMethodChange = {},
            onInputChange = {},
            onSendOtp = {}
        )
    }
}

@Preview(showBackground = true, name = "With Last Used Email")
@Composable
private fun OtpInputStepWithLastUsedPreview() {
    DescopeTheme {
        OtpInputStep(
            deliveryMethod = OtpDeliveryMethod.EMAIL,
            inputValue = "",
            isLoading = false,
            lastUsedDeliveryMethod = OtpDeliveryMethod.EMAIL,
            lastUsedValue = "user@example.com",
            isUpdateMode = false,
            onDeliveryMethodChange = {},
            onInputChange = {},
            onSendOtp = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OtpVerifyStepPreview() {
    DescopeTheme {
        OtpVerifyStep(
            destination = "test@example.com",
            otpCode = "123",
            isLoading = false,
            isUpdateMode = false,
            onOtpCodeChange = {},
            onVerify = {},
            onResend = {}
        )
    }
}
