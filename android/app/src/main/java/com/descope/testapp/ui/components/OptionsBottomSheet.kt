package com.descope.testapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.descope.testapp.ui.theme.DescopeTheme

/**
 * Data class representing an option in the bottom sheet.
 *
 * @param id Unique identifier for the option
 * @param title Main title text displayed for the option
 * @param subtitle Optional subtitle text displayed below the title
 * @param icon Icon to display for the option
 * @param iconTint Color tint for the icon (defaults to primary color)
 */
data class BottomSheetOption(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val iconTint: Color? = null
)

/**
 * Configuration for the bottom sheet header.
 *
 * @param title Main title displayed in the header
 * @param subtitle Optional subtitle displayed below the title
 * @param icon Icon displayed in the header
 * @param iconContainerColor Background color for the icon container
 * @param iconTint Tint color for the header icon
 */
data class BottomSheetHeaderConfig(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val iconContainerColor: Color? = null,
    val iconTint: Color? = null
)

/**
 * A reusable modal bottom sheet component with a modern design.
 *
 * @param isVisible Whether the bottom sheet is visible
 * @param onDismiss Callback when the bottom sheet is dismissed
 * @param headerConfig Configuration for the header section
 * @param options List of options to display
 * @param onOptionSelected Callback when an option is selected
 * @param sheetState Optional sheet state for controlling the bottom sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    headerConfig: BottomSheetHeaderConfig,
    options: List<BottomSheetOption>,
    onOptionSelected: (BottomSheetOption) -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState? = null
) {
    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState ?: rememberModalBottomSheetState(),
            modifier = modifier
        ) {
            OptionsBottomSheetContent(
                headerConfig = headerConfig,
                options = options,
                onOptionSelected = onOptionSelected
            )
        }
    }
}

/**
 * The content of the options bottom sheet, can be used standalone without the modal wrapper.
 */
@Composable
fun OptionsBottomSheetContent(
    headerConfig: BottomSheetHeaderConfig,
    options: List<BottomSheetOption>,
    onOptionSelected: (BottomSheetOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp)
    ) {
        // Modern header with icon
        BottomSheetHeader(config = headerConfig)

        Spacer(modifier = Modifier.height(8.dp))

        // Option cards
        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(options, key = { it.id }) { option ->
                OptionCard(
                    option = option,
                    onClick = { onOptionSelected(option) }
                )
            }
        }
    }
}

@Composable
private fun BottomSheetHeader(
    config: BottomSheetHeaderConfig,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(config.iconContainerColor ?: MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = config.icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = config.iconTint ?: MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = config.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        config.subtitle?.let { subtitle ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun OptionCard(
    option: BottomSheetOption,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with background
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        (option.iconTint ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = option.iconTint ?: MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                option.subtitle?.let { subtitle ->
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Arrow indicator
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OptionsBottomSheetContentPreview() {
    DescopeTheme {
        OptionsBottomSheetContent(
            headerConfig = BottomSheetHeaderConfig(
                title = "Select Option",
                subtitle = "Choose from the available options",
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight
            ),
            options = listOf(
                BottomSheetOption(
                    id = "1",
                    title = "Option 1",
                    subtitle = "This is the first option",
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowRight
                ),
                BottomSheetOption(
                    id = "2",
                    title = "Option 2",
                    subtitle = "This is the second option",
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowRight
                )
            ),
            onOptionSelected = {}
        )
    }
}
