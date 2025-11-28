package nuist.cn.mymoment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import nuist.cn.mymoment.view.LoginScreen
import nuist.cn.mymoment.view.RegisterScreen
import nuist.cn.mymoment.view.AddDiaryScreen
import nuist.cn.mymoment.viewmodel.AuthViewModel
import nuist.cn.mymoment.viewmodel.DiaryViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val diaryViewModel: DiaryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            var showRegister by remember { mutableStateOf(false) }

            // 从 AuthViewModel 拿登录状态
            val authState = authViewModel.uiState.value

            if (!authState.isLoggedIn) {
                // 【还没登录】→ 显示 登录/注册
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
                // 【已经登录】→ 直接显示 添加日记界面（用于调试）
                AddDiaryScreen(
                    diaryViewModel = diaryViewModel,
                    onBackToHome = {
                        // 调试阶段你可以先什么都不做，或者之后换成回到主页
                    }
                )
            }
        }
    }
}