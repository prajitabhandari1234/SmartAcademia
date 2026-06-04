package au.edu.cqu.smartacademia.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import au.edu.cqu.smartacademia.MainActivity
import au.edu.cqu.smartacademia.R
import au.edu.cqu.smartacademia.viewmodel.UserViewModel

/**
 * Activity responsible for user authentication.
 *
 * Features:
 * - User login.
 * - Session management.
 * - Navigation to registration screen.
 * - Navigation to OTP verification screen.
 * - Loading the SmartAcademia dashboard after successful login.
 *
 * Supports Assignment 3 authentication and user management features.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel

    /**
     * Creates and initialises the login screen.
     *
     * Sets up:
     * - ViewModel
     * - Login functionality
     * - Registration navigation
     * - Session storage
     *
     * @param savedInstanceState Previously saved activity state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        userViewModel =
            ViewModelProvider(this)[UserViewModel::class.java]

        val emailEditText =
            findViewById<EditText>(R.id.emailEditText)

        val passwordEditText =
            findViewById<EditText>(R.id.passwordEditText)

        val loginButton =
            findViewById<Button>(R.id.loginButton)

        val registerButton =
            findViewById<Button>(R.id.registerButton)

        registerButton.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    RegisterActivity::class.java
                )
            )
        }

        loginButton.setOnClickListener {

            val email =
                emailEditText.text.toString().trim()

            val password =
                passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {

                Toast.makeText(
                    this,
                    getString(R.string.empty_fields_message),
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            userViewModel.login(
                email,
                password
            ) { user ->

                when {

                    user == null -> {

                        Toast.makeText(
                            this,
                            getString(R.string.invalid_login_message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    !user.isVerified -> {

                        Toast.makeText(
                            this,
                            getString(R.string.verify_first_message),
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent =
                            Intent(
                                this,
                                VerifyOtpActivity::class.java
                            )

                        intent.putExtra(
                            "email",
                            email
                        )

                        startActivity(intent)
                    }

                    else -> {

                        val sharedPreferences =
                            getSharedPreferences(
                                "smartacademia_session",
                                MODE_PRIVATE
                            )

                        sharedPreferences.edit()
                            .putString(
                                "email",
                                user.email
                            )
                            .putString(
                                "full_name",
                                user.fullName
                            )
                            .apply()

                        startActivity(
                            Intent(
                                this,
                                MainActivity::class.java
                            )
                        )

                        finish()
                    }
                }
            }
        }
    }
}