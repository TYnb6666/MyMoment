package nuist.cn.mymoment.ui.login

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import nuist.cn.mymoment.data.preferences.AppPreferences

data class LoginState(
    val isLoggedIn: Boolean = false,
    val error: LoginValidationError? = null
)

enum class LoginValidationError {
    EMPTY_FIELDS,
    INVALID_CREDENTIALS
}

class LoginViewModel(
    private val preferences: AppPreferences
) : ViewModel() {

    private val _loginState = MutableLiveData(LoginState())
    val loginState: LiveData<LoginState> = _loginState

    fun hasLoggedInSession(): Boolean = preferences.isLoggedIn()

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _loginState.value = LoginState(error = LoginValidationError.EMPTY_FIELDS)
            return
        }
        // Demo-only authentication: accept any non-empty credentials.
        preferences.setLoggedIn(true)
        _loginState.value = LoginState(isLoggedIn = true)
    }
}

class LoginViewModelFactory(
    private val appContext: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(AppPreferences(appContext)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

