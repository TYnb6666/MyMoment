package nuist.cn.mymoment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import nuist.cn.mymoment.view.LoginScreen
import nuist.cn.mymoment.view.RegisterScreen
import nuist.cn.mymoment.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var showRegister by remember { mutableStateOf(false) }

            if (showRegister) {
                RegisterScreen(
                    viewModel = authViewModel,
                    onBackToLogin = { showRegister = false },
                    onRegisterSuccess = { showRegister = false } // 成功后回到登录
                )
            } else {
                LoginScreen(
                    viewModel = authViewModel,
                    onGoToRegister = { showRegister = true }
                )
            }
        }
    }
}