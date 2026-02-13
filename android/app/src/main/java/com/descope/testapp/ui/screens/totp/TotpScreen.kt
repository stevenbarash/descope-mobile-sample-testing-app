package com.descope.testapp.ui.screens.totp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.descope.testapp.ui.components.CodeInput
import com.descope.testapp.ui.components.SnackbarType
import com.descope.testapp.ui.components.StyledSnackbar
import com.descope.testapp.ui.components.StyledSnackbarData
import com.descope.testapp.ui.screens.AuthMode
import com.descope.testapp.ui.theme.Dimensions
import com.descope.types.TotpResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TotpScreen(
    viewModel: TotpViewModel,
    onNavigateBack: () -> Unit,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val loginId by viewModel.loginId.collectAsState()
    val lastUsedLoginId by viewModel.lastUsedLoginId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()
    val totpResponse by viewModel.totpResponse.collectAsState()
    val qrCodeBitmap by viewModel.qrCodeBitmap.collectAsState()
    val totpCode by viewModel.totpCode.collectAsState()
    val mode by viewModel.mode.collectAsState()

    val isUpdateMode = mode == AuthMode.Update

    val snackbarData = remember { mutableStateOf<StyledSnackbarData?>(null) }

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            onAuthSuccess()
        }
    }

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
                            if (currentStep != TotpStep.INPUT) {
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
                    (slideInHorizontally { it } + fadeIn()) togetherWith
                            (slideOutHorizontally { -it } + fadeOut())
                },
                label = "step_transition",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { step ->
                when (step) {
                    TotpStep.INPUT -> TotpInputStep(
                        loginId = loginId,
                        lastUsedLoginId = lastUsedLoginId,
                        isLoading = isLoading,
                        isUpdateMode = isUpdateMode,
                        onLoginIdChange = { viewModel.onLoginIdChanged(it) },
                        onSignIn = { viewModel.prepareForVerify() },
                        onSignUp = { viewModel.signUp() }
                    )
                    TotpStep.SETUP -> TotpSetupStep(
                        totpResponse = totpResponse,
                        qrCodeBitmap = qrCodeBitmap,
                        isUpdateMode = isUpdateMode,
                        onScanComplete = { viewModel.goToVerifyFromSetup() }
                    )
                    TotpStep.VERIFY -> TotpVerifyStep(
                        totpCode = totpCode,
                        isLoading = isLoading,
                        isUpdateMode = isUpdateMode,
                        onCodeChange = { viewModel.setTotpCode(it) },
                        onVerify = { viewModel.verify() }
                    )
                }
            }

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
private fun TotpInputStep(
    loginId: String,
    lastUsedLoginId: String?,
    isLoading: Boolean,
    isUpdateMode: Boolean,
    onLoginIdChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onSignUp: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    // Disable hints and autofill in update mode
    val hasLastUsed = !isUpdateMode && !lastUsedLoginId.isNullOrEmpty()
    val placeholderText = if (hasLastUsed) {
        "$lastUsedLoginId (last used)"
    } else {
        "User Name or Email"
    }

    val canSubmit = loginId.isNotBlank() || hasLastUsed

    val title = if (isUpdateMode) "Set Up Authenticator" else "TOTP Authentication"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimensions.ScreenHorizontalPadding)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

        HeroIcon(
            icon = Icons.Filled.Timer,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            iconColor = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingXLarge))

        // Hide login ID input in update mode - use current user
        if (!isUpdateMode) {
            OutlinedTextField(
                value = loginId,
                onValueChange = onLoginIdChange,
                label = { Text("Login ID") },
                placeholder = {
                    Text(
                        text = placeholderText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

            // Sign In Button (Main)
            Button(
                onClick = onSignIn,
                enabled = !isLoading && canSubmit,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.ButtonHeight)
            ) {
                Icon(Icons.AutoMirrored.Filled.Login, null)
            Spacer(Modifier.width(8.dp))
            Text("Sign In")
        }

        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

        // Sign Up Button (Secondary)
        OutlinedButton(
            onClick = {
                keyboardController?.hide()
                onSignUp()
            },
            enabled = !isLoading && canSubmit,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            ),
             modifier = Modifier
                .fillMaxWidth()
                .height(Dimensions.ButtonHeight)
        ) {
            Icon(Icons.Filled.QrCode, null)
            Spacer(Modifier.width(8.dp))
            Text("Sign Up")
        }
        } else {
            // Update mode - single button to set up TOTP
            Button(
                onClick = onSignUp,
                enabled = !isLoading,
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
                    Icon(Icons.Filled.QrCode, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Set Up TOTP")
                }
            }
        }
    }
}

@Composable
private fun TotpSetupStep(
    totpResponse: TotpResponse?,
    qrCodeBitmap: android.graphics.Bitmap?,
    isUpdateMode: Boolean,
    onScanComplete: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimensions.ScreenHorizontalPadding)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

        HeroIcon(
            icon = Icons.Filled.QrCode,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

        Text(
            text = "Scan QR Code",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

        if (qrCodeBitmap != null) {
            Image(
                bitmap = qrCodeBitmap.asImageBitmap(),
                contentDescription = "QR Code",
                modifier = Modifier
                    .size(250.dp)
                    .padding(8.dp)
            )
        } else {
             Box(modifier = Modifier
                 .size(250.dp)
                 .padding(8.dp), contentAlignment = Alignment.Center) {
                 CircularProgressIndicator()
             }
        }

        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

        // Provisioning URL Link
        if (totpResponse != null) {
            TextButton(onClick = {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(totpResponse.provisioningUrl)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {
                    Toast.makeText(context, "No app found to handle this link", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Open in Authenticator App")
            }

            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

            // Key Copy
            OutlinedTextField(
                value = totpResponse.key,
                onValueChange = {},
                readOnly = true,
                label = { Text("Manual Key") },
                textStyle = MaterialTheme.typography.bodySmall,
                trailingIcon = {
                     IconButton(onClick = {
                         val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                         val clip = ClipData.newPlainText("TOTP Key", totpResponse.key)
                         clipboard.setPrimaryClip(clip)
                         Toast.makeText(context, "Key copied", Toast.LENGTH_SHORT).show()
                     }) {
                         Icon(Icons.Default.ContentCopy, "Copy")
                     }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.SpacingXLarge))

        if (isUpdateMode) {
            // In update mode, verification is required - make it the primary button
            Button(
                onClick = onScanComplete,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.ButtonHeight)
            ) {
                Text("Verify Code")
            }
        } else {
            Button(
                onClick = onScanComplete,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.ButtonHeight)
            ) {
                Text("Sign In")
            }
        }

        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
    }
}

@Composable
private fun TotpVerifyStep(
    totpCode: String,
    isLoading: Boolean,
    isUpdateMode: Boolean,
    onCodeChange: (String) -> Unit,
    onVerify: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    val title = if (isUpdateMode) "Verify Setup" else "Enter Code"
    val buttonText = if (isUpdateMode) "Confirm" else "Verify"

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    // Auto-verify when all digits are entered
    LaunchedEffect(totpCode) {
        if (totpCode.length == 6) {
            keyboardController?.hide()
            onVerify()
        }
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

        HeroIcon(
            icon = Icons.Filled.Lock,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            iconColor = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingXLarge))

        CodeInput(
            code = totpCode,
            onCodeChange = onCodeChange,
            enabled = !isLoading,
            modifier = Modifier.focusRequester(focusRequester)
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

        Button(
            onClick = onVerify,
            enabled = !isLoading && totpCode.length == 6,
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
                    color = MaterialTheme.colorScheme.onPrimary
                 )
            } else {
                Text(buttonText)
            }
        }
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
