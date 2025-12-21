package nuist.cn.mymoment.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nuist.cn.mymoment.model.Diary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryDetailScreen(
    diary: Diary,
    onBack: () -> Unit,
    onEdit: (Diary) -> Unit,
    onDelete: (Diary) -> Unit
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val timeText = if (diary.timestamp > 0) sdf.format(Date(diary.timestamp)) else ""

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Diary") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(diary) }) {
                        Icon(Icons.Default.Edit, contentDescription = "edit")
                    }
                    IconButton(onClick = { onDelete(diary) }) {
                        Icon(Icons.Default.Delete, contentDescription = "delete")
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
            Text(
                text = diary.title.ifBlank { "Untitled" },
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(8.dp))

            if (timeText.isNotBlank()) {
                Text(text = timeText, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(12.dp))
            }

            diary.location?.let { gp ->
                Text(
                    text = "Location: %.5f, %.5f".format(gp.latitude, gp.longitude),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(12.dp))
            }

            Divider()

            Spacer(Modifier.height(12.dp))

            Text(
                text = diary.content.ifBlank { "(No content)" },
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}