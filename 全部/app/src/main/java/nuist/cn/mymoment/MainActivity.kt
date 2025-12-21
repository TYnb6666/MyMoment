package nuist.cn.mymoment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import nuist.cn.mymoment.view.*
import nuist.cn.mymoment.viewmodel.AuthViewModel
import nuist.cn.mymoment.viewmodel.DiaryViewModel
import nuist.cn.mymoment.model.Diary

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val diaryViewModel: DiaryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var showRegister by remember { mutableStateOf(false) }
            var isAddingDiary by remember { mutableStateOf(false) }
            var isPickingLocation by remember { mutableStateOf(false) }
            var isViewingAllEntriesMap by remember { mutableStateOf(false) }
            var selectedDiary by remember { mutableStateOf<Diary?>(null) }

            // The key architectural fix is here.
            // The Activity, which owns the navigation state, listens for navigation events.
            val editState = diaryViewModel.editState.value
            LaunchedEffect(editState.saveComplete) {
                if (editState.saveComplete) {
                    isAddingDiary = false
                }
            }

            val authState = authViewModel.uiState.value

            if (!authState.isLoggedIn) {
                isAddingDiary = false
                isPickingLocation = false
                isViewingAllEntriesMap = false

                if (showRegister) {
                    RegisterScreen(
                        viewModel = authViewModel,
                        onBackToLogin = { showRegister = false },
                        onRegisterSuccess = { showRegister = false }
                    )
                } else {
                    LoginScreen(
                        viewModel = authViewModel,
                        onGoToRegister = { showRegister = true }
                    )
                }
            } else {
                when {

                    //  ① detail page：highest priority
                    selectedDiary != null -> {
                        DiaryDetailScreen(
                            diary = selectedDiary!!,
                            onBack = { selectedDiary = null },
                            onEdit = { diary ->
                                selectedDiary = null
                                diaryViewModel.startEdit(diary)
                                isAddingDiary = true
                            },
                            onDelete = { diary ->
                                diaryViewModel.deleteDiary(diary.id)
                                selectedDiary = null
                            }
                        )
                    }

                    // ② 地图页
                    isViewingAllEntriesMap -> {
                        AllEntriesMapScreen(
                            diaryViewModel = diaryViewModel,
                            onBack = { isViewingAllEntriesMap = false }
                        )
                    }

                    // ③ 选定位页
                    isPickingLocation -> {
                        LocationPickerScreen(
                            diaryViewModel = diaryViewModel,
                            onLocationSelected = { isPickingLocation = false }
                        )
                    }

                    // ④ 添加 / 编辑日记页
                    isAddingDiary -> {
                        AddDiaryScreen(
                            diaryViewModel = diaryViewModel,
                            onNavigateToLocationPicker = { isPickingLocation = true },
                            onBackToHome = { isAddingDiary = false }
                        )
                    }

                    // ⑤ 主页
                    else -> {
                        HomeScreen(
                            diaryViewModel = diaryViewModel,
                            authViewModel = authViewModel,
                            onAddDiary = {
                                diaryViewModel.prepareNewDiary()
                                isAddingDiary = true
                            },
                            onLogout = { authViewModel.logout() },
                            onNavigateToAllEntriesMap = { isViewingAllEntriesMap = true },
                            onOpenDetail = { selectedDiary = it },
                            onEditDiary = { diary ->
                                diaryViewModel.startEdit(diary)
                                isAddingDiary = true
                            },
                            onDeleteDiary = { diary ->
                                diaryViewModel.deleteDiary(diary.id)
                            }
                        )
                    }
                }
            }
        }
    }
}
