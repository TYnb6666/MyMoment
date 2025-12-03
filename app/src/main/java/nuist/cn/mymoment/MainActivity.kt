package nuist.cn.mymoment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import nuist.cn.mymoment.model.Diary
import nuist.cn.mymoment.view.*
import nuist.cn.mymoment.viewmodel.AuthViewModel
import nuist.cn.mymoment.viewmodel.DiaryViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val diaryViewModel: DiaryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // 设置状态：夜间模式、字体大小、卡片颜色
            val systemInDark = isSystemInDarkTheme()
            var isDarkMode by remember { mutableStateOf(systemInDark) }
            var isLargeFont by remember { mutableStateOf(false) }
            var diaryCardColor by remember { mutableStateOf(Color(0xFFF5F5F5)) }
            
            val colorScheme = if (isDarkMode) darkColorScheme() else lightColorScheme()

            // 定义基于 isLargeFont 的字体排版
            val typography = if (isLargeFont) {
                Typography(
                    headlineLarge = Typography().headlineLarge.copy(fontSize = 38.sp),
                    headlineMedium = Typography().headlineMedium.copy(fontSize = 34.sp),
                    titleLarge = Typography().titleLarge.copy(fontSize = 28.sp),
                    titleMedium = Typography().titleMedium.copy(fontSize = 22.sp),
                    bodyLarge = Typography().bodyLarge.copy(fontSize = 22.sp),
                    bodyMedium = Typography().bodyMedium.copy(fontSize = 20.sp),
                    bodySmall = Typography().bodySmall.copy(fontSize = 18.sp),
                    labelLarge = Typography().labelLarge.copy(fontSize = 20.sp)
                )
            } else {
                Typography()
            }

            MaterialTheme(colorScheme = colorScheme, typography = typography) {
                var showRegister by remember { mutableStateOf(false) }
                var isAddingDiary by remember { mutableStateOf(false) }
                var isPickingLocation by remember { mutableStateOf(false) }
                var isViewingAllEntriesMap by remember { mutableStateOf(false) }
                var isViewingSettings by remember { mutableStateOf(false) }
                var selectedDiary by remember { mutableStateOf<Diary?>(null) }

                val editState by diaryViewModel.editState
                LaunchedEffect(editState.saveComplete) {
                    if (editState.saveComplete) {
                        isAddingDiary = false
                        diaryViewModel.prepareNewDiary()
                    }
                }

                val authState = authViewModel.uiState.value

                if (!authState.isLoggedIn) {
                    isAddingDiary = false
                    isPickingLocation = false
                    isViewingAllEntriesMap = false
                    isViewingSettings = false
                    selectedDiary = null

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
                        isViewingSettings -> {
                            SettingsScreen(
                                isDarkMode = isDarkMode,
                                onDarkModeChange = { isDarkMode = it },
                                isLargeFont = isLargeFont,
                                onLargeFontChange = { isLargeFont = it },
                                selectedColor = diaryCardColor,
                                onColorChange = { diaryCardColor = it },
                                onBack = { isViewingSettings = false }
                            )
                        }
                        selectedDiary != null -> {
                            DiaryDetailScreen(
                                diary = selectedDiary!!,
                                onBack = { selectedDiary = null }
                            )
                        }
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
                                onBack = { isAddingDiary = false }
                            )
                        }
                        else -> {
                            HomeScreen(
                                diaryViewModel = diaryViewModel,
                                authViewModel = authViewModel,
                                // 核心修改：如果是夜间模式，强制卡片为黑色；否则使用用户选择的颜色
                                diaryCardColor = if (isDarkMode) Color.Black else diaryCardColor,
                                onAddDiary = {
                                    if (diaryViewModel.editState.value.editingId != null) {
                                        diaryViewModel.prepareNewDiary()
                                    }
                                    isAddingDiary = true
                                },
                                onLogout = { authViewModel.logout() },
                                onNavigateToAllEntriesMap = { isViewingAllEntriesMap = true },
                                onNavigateToSettings = { isViewingSettings = true },
                                onEditDiary = { diary ->
                                    diaryViewModel.startEdit(diary)
                                    isAddingDiary = true
                                },
                                onDeleteDiary = { diary ->
                                    diaryViewModel.deleteDiary(diary.id)
                                },
                                onDiaryClick = { diary ->
                                    selectedDiary = diary
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
