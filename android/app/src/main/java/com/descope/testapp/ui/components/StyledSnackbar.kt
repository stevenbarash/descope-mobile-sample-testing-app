package com.descope.testapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.descope.testapp.ui.theme.DescopePrimary
import kotlinx.coroutines.delay

/**
 * Types of styled snackbar messages
 */
enum class SnackbarType {
    SUCCESS,
    ERROR,
    INFO
}

/**
 * Data class representing a styled snackbar message
 */
data class StyledSnackbarData(
    val message: String,
    val type: SnackbarType = SnackbarType.INFO,
    val durationMs: Long = 4000L
)

/**
 * A styled snackbar component that matches the app's design system.
 * This provides a more polished look compared to the default Material snackbar.
 *
 * @param snackbarData The snackbar data to display, or null if no snackbar should be shown
 * @param onDismiss Callback when the snackbar is dismissed
 * @param modifier Optional modifier for the container
 */
@Composable
fun StyledSnackbar(
    snackbarData: StyledSnackbarData?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(snackbarData) {
        if (snackbarData != null) {
            isVisible = true
            delay(snackbarData.durationMs)
            isVisible = false
            delay(300) // Wait for exit animation
            onDismiss()
        } else {
            isVisible = false
        }
    }

    AnimatedVisibility(
        visible = isVisible && snackbarData != null,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        snackbarData?.let { data ->
            StyledSnackbarContent(
                data = data,
                onDismiss = {
                    isVisible = false
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun StyledSnackbarContent(
    data: StyledSnackbarData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, iconTint, icon) = when (data.type) {
        SnackbarType.SUCCESS -> Triple(
            Color(0xFF1B5E20).copy(alpha = 0.95f),
            Color(0xFF81C784),
            Icons.Filled.CheckCircle
        )
        SnackbarType.ERROR -> Triple(
            Color(0xFFB71C1C).copy(alpha = 0.95f),
            Color(0xFFEF9A9A),
            Icons.Filled.Error
        )
        SnackbarType.INFO -> Triple(
            DescopePrimary.copy(alpha = 0.95f),
            Color.White,
            Icons.Filled.Info
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = data.message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Dismiss",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Helper composable to manage styled snackbar state
 */
@Composable
fun rememberStyledSnackbarState(): StyledSnackbarState {
    return remember { StyledSnackbarState() }
}

/**
 * State holder for styled snackbar
 */
class StyledSnackbarState {
    var currentSnackbar by mutableStateOf<StyledSnackbarData?>(null)
        private set

    fun showSuccess(message: String, durationMs: Long = 4000L) {
        currentSnackbar = StyledSnackbarData(message, SnackbarType.SUCCESS, durationMs)
    }

    fun showError(message: String, durationMs: Long = 4000L) {
        currentSnackbar = StyledSnackbarData(message, SnackbarType.ERROR, durationMs)
    }

    fun showInfo(message: String, durationMs: Long = 4000L) {
        currentSnackbar = StyledSnackbarData(message, SnackbarType.INFO, durationMs)
    }

    fun dismiss() {
        currentSnackbar = null
    }
}
