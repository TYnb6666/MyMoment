package nuist.cn.mymoment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import nuist.cn.mymoment.view.LoginScreen
import nuist.cn.mymoment.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val state = authViewModel.uiState.value
            if (state.isLoggedIn) {
                // TODO: 这里换成你的 DiaryScreen
                // DiaryScreen(...)
                LoginScreen(authViewModel) // 先临时放登录界面防报错
            } else {
                LoginScreen(authViewModel)
            }
        }
    }
}