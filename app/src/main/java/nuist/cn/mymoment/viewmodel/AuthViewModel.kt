package nuist.cn.mymoment.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nuist.cn.mymoment.repository.AuthRepository

data class AuthUiState (
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

class AuthViewModel (
    private val repository: AuthRepository = AuthRepository()
)  : ViewModel() {
    var uiState = androidx.compose.runtime.mutableStateOf(AuthUiState()) // create a viewmodel state
        private set // make sure only the viewmodel can update the state, outside can only read

    init {  // check if the user is logged in
        viewModelScope.launch {
            if (repository.currentUser != null) {
                uiState.value = uiState.value.copy(isLoggedIn = true)
            }
        }
    }

    // update corresponding data according to the user input
    fun onEmailChange(email: String) {
        uiState.value = uiState.value.copy(email = email)
    }

    // update corresponding data according to the user input
    fun onPasswordChange(password: String) {
        uiState.value = uiState.value.copy(password = password)
    }

    fun login() {
        val state = uiState.value
//        if (state.email.isBlank() || state.password.isBlank()){
//            uiState.value = state.copy(error = "Email and password cannot be empty", isLoading = false)
//            return
//        }

        viewModelScope.launch {
            uiState.value = state.copy(isLoading = true, error = null)
            val result = repository.login(state.email!!, state.password!!)
            if (result.isSuccess) {
                uiState.value = state.copy(isLoggedIn = true, isLoading = false, error = null)
            } else {
                uiState.value = state.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }

        }
    }

    fun register(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            //  begin loading, setting loading status
            uiState.value = uiState.value.copy(isLoading = true, error = null)

            val result = repository.register(email, password)

            if (result.isSuccess) {
                uiState.value = uiState.value.copy(isLoading = false, error = null)
                onResult(true, null)
            } else {
                val msg = result.exceptionOrNull()?.message
                uiState.value = uiState.value.copy(isLoading = false, error = msg)
                onResult(false, msg)
            }
        }
    }

    fun logout() {
        repository.logout()
        uiState.value = AuthUiState() // reset state so UI shows login screen
    }
}

