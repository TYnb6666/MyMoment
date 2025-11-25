package nuist.cn.mymoment.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import nuist.cn.mymoment.R
import nuist.cn.mymoment.ui.main.MainActivity
import nuist.cn.mymoment.util.AppPreferences

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var preferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = AppPreferences(this)
        if (preferences.isLoggedIn()) {
            navigateToMain()
            return
        }
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            val username = usernameInput.text?.toString().orEmpty()
            val password = passwordInput.text?.toString().orEmpty()
            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(this, R.string.login_required, Toast.LENGTH_SHORT).show()
            } else {
                preferences.setLoggedIn(true)
                Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show()
                navigateToMain()
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

