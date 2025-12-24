package nuist.cn.mymoment.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    isLargeFont: Boolean,
    onLargeFontChange: (Boolean) -> Unit,
    selectedColor: Color,           // Current diary card background color
    onColorChange: (Color) -> Unit, // Callback for color selection
    onBack: () -> Unit
) {
    // Preset color palette for diary cards
    val presetColors = listOf(
        Color(0xFFF5F5F5), // Default Light Gray
        Color(0xFFFFF9C4), // Soft Yellow
        Color(0xFFE1F5FE), // Soft Blue
        Color(0xFFE8F5E9), // Soft Green
        Color(0xFFFCE4EC)  // Soft Pink
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Dark mode toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Night Mode", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = isDarkMode, onCheckedChange = onDarkModeChange)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Font size toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Large Font Size", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = isLargeFont, onCheckedChange = onLargeFontChange)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Diary card background color picker
            Text("Diary Card Background", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Color selection circles
                presetColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (selectedColor == color) 3.dp else 1.dp,
                                color = if (selectedColor == color) MaterialTheme.colorScheme.primary else Color.Gray,
                                shape = CircleShape
                            )
                            .clickable { onColorChange(color) }
                    )
                }
            }

            // Helper text
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Pick a theme color for your diary list cards.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}