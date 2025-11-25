package nuist.cn.mymoment.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import nuist.cn.mymoment.R
import nuist.cn.mymoment.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = LoginViewModelFactory(applicationContext)
        viewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]
        if (viewModel.hasLoggedInSession()) {
            navigateToMain()
            return
        }
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)

        observeViewModel()

        loginButton.setOnClickListener {
            val username = usernameInput.text?.toString().orEmpty()
            val password = passwordInput.text?.toString().orEmpty()
            viewModel.login(username, password)
        }
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(this) { state ->
            when {
                state.isLoggedIn -> {
                    Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show()
                    navigateToMain()
                }
                state.error == LoginValidationError.EMPTY_FIELDS -> {
                    Toast.makeText(this, R.string.login_required, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
