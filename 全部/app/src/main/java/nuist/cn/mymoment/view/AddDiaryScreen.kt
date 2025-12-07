package nuist.cn.mymoment.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nuist.cn.mymoment.viewmodel.DiaryViewModel

@Composable
fun AddDiaryScreen(
    diaryViewModel: DiaryViewModel,
    onBackToHome: () -> Unit,   // 保存成功后返回主页
    onNavigateToLocationPicker: () -> Unit // 新增一个导航到选择位置页面的回调
) {
    val state by diaryViewModel.editState

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "New Diary",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.title,
                onValueChange = { diaryViewModel.onTitleChange(it) },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.content,
                onValueChange = { diaryViewModel.onContentChange(it) },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                maxLines = 10,
            )

            Spacer(Modifier.height(16.dp))

            // 新增的选择位置按钮和显示位置的文本
            Button(
                onClick = { onNavigateToLocationPicker() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select Location")
            }

            Spacer(Modifier.height(8.dp))

            state.location?.let { location ->
                Text(
                    text = "Selected Location: %.4f, %.4f".format(location.latitude, location.longitude),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(16.dp))


            Button(
                onClick = {
                    diaryViewModel.saveDiary {
                        // 保存成功回调：先重置，再回主页
                        diaryViewModel.resetAfterSaved()
                        onBackToHome()
                    }
                },
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isSaving) "Saving..." else "Save Diary")
            }

            state.error?.let {
                Spacer(Modifier.height(8.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}