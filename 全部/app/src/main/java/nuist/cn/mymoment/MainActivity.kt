package nuist.cn.mymoment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import nuist.cn.mymoment.view.*
import nuist.cn.mymoment.viewmodel.AuthViewModel
import nuist.cn.mymoment.viewmodel.DiaryViewModel

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
                    isViewingAllEntriesMap -> {
                        AllEntriesMapScreen(
                            diaryViewModel = diaryViewModel,
                            onBack = { isViewingAllEntriesMap = false }
                        )
                    }
                    isPickingLocation -> {
                        LocationPickerScreen(
                            diaryViewModel = diaryViewModel,
                            onLocationSelected = { isPickingLocation = false }
                        )
                    }
                    isAddingDiary -> {
                        AddDiaryScreen(
                            diaryViewModel = diaryViewModel,
                            onNavigateToLocationPicker = { isPickingLocation = true },
                            onBackToHome = { isAddingDiary = false }
                        )
                    }
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
