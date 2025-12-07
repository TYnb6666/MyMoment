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
            // ----------------------------------------------------
            // 1. 登录/注册状态
            var showRegister by remember { mutableStateOf(false) }

            // 2. 登录后子导航状态
            var isAddingDiary by remember { mutableStateOf(false) }
            var isPickingLocation by remember { mutableStateOf(false) } // 新增：控制地图页面
            // ----------------------------------------------------

            val authState = authViewModel.uiState.value

            if (!authState.isLoggedIn) {
                // 【未登录流程】→ 登录 / 注册

                // 确保在登出状态下，子导航状态重置
                isAddingDiary = false
                isPickingLocation = false // 登出时重置

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
                // 【已登录流程】→ Home / 添加日记 / 选择位置
                when {
                    isPickingLocation -> {
                        // 📍 处于选择位置页面
                        LocationPickerScreen(
                            diaryViewModel = diaryViewModel,
                            onLocationSelected = { isPickingLocation = false } // 点击确认后，返回添加日记页
                        )
                    }
                    isAddingDiary -> {
                        // ⭐ 处于添加日记页面
                        AddDiaryScreen(
                            diaryViewModel = diaryViewModel,
                            onBackToHome = { isAddingDiary = false }, // 返回主页
                            onNavigateToLocationPicker = { isPickingLocation = true } // 跳转到地图页
                        )
                    }
                    else -> {
                        // ⭐ 处于主页（日记列表）
                        HomeScreen(
                            diaryViewModel = diaryViewModel,
                            authViewModel = authViewModel,
                            // 点击“添加”按钮时，将状态设为 true，从而触发Compose重组到 AddDiaryScreen
                            onAddDiary = { isAddingDiary = true },
                            onLogout = {
                                authViewModel.logout()
                            }
                        )
                    }
                }
            }
        }
    }
}
