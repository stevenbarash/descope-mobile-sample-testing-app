package com.descope.testapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private const val DEFAULT_CODE_LENGTH = 6

/**
 * Custom code input with individual digit boxes.
 * Supports:
 * - Auto-focus shifting between digits
 * - Paste from clipboard
 * - SMS autofill suggestions
 */
@Composable
fun CodeInput(
    code: String,
    onCodeChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    length: Int = DEFAULT_CODE_LENGTH
) {
    // Use a single hidden TextField for better autofill support
    var textFieldValue by remember(code) {
        mutableStateOf(
            TextFieldValue(
                text = code,
                selection = TextRange(code.length)
            )
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        // Hidden TextField for keyboard input and autofill
        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                // Handle paste - extract only digits
                val newText = newValue.text.filter { it.isDigit() }.take(length)

                // Check if this looks like a paste operation (more than 1 new character)
                if (newValue.text.length - code.length > 1) {
                    // Try to extract digits from pasted content
                    val pastedDigits = newValue.text.filter { it.isDigit() }.take(length)
                    if (pastedDigits.isNotEmpty()) {
                        textFieldValue = TextFieldValue(
                            text = pastedDigits,
                            selection = TextRange(pastedDigits.length)
                        )
                        onCodeChange(pastedDigits)
                        return@BasicTextField
                    }
                }

                textFieldValue = TextFieldValue(
                    text = newText,
                    selection = TextRange(newText.length)
                )
                onCodeChange(newText)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            enabled = enabled,
            singleLine = true,
            cursorBrush = SolidColor(Color.Transparent),
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp) // Make it nearly invisible but still focusable
                .padding(0.dp),
            decorationBox = { innerTextField ->
                // Don't show the actual text field, just use it for input
                Box(modifier = Modifier.size(0.dp)) {
                    innerTextField()
                }
            }
        )

        // Visual code boxes
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(length) { index ->
                DigitBox(
                    digit = code.getOrNull(index)?.toString() ?: "",
                    isFocused = code.length == index && enabled,
                    hasError = false
                )
            }
        }
    }
}

/**
 * Individual digit box for code input with animations
 */
@Composable
private fun DigitBox(
    digit: String,
    isFocused: Boolean,
    hasError: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            hasError -> MaterialTheme.colorScheme.errorContainer
            digit.isNotEmpty() -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            isFocused -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        animationSpec = tween(150),
        label = "backgroundColor"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            hasError -> MaterialTheme.colorScheme.error
            digit.isNotEmpty() -> MaterialTheme.colorScheme.primary
            isFocused -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outlineVariant
        },
        animationSpec = tween(150),
        label = "borderColor"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isFocused || digit.isNotEmpty() || hasError) 2.dp else 1.dp,
        animationSpec = tween(150),
        label = "borderWidth"
    )

    val scale by animateFloatAsState(
        targetValue = if (digit.isNotEmpty()) 1f else 0.95f,
        animationSpec = tween(100),
        label = "scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(52.dp)
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        AnimatedVisibility(
            visible = digit.isNotEmpty(),
            enter = scaleIn(animationSpec = tween(100)) + fadeIn(animationSpec = tween(100)),
            exit = scaleOut(animationSpec = tween(100)) + fadeOut(animationSpec = tween(100))
        ) {
            Text(
                text = digit,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}
