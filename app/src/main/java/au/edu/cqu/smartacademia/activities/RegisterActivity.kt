package au.edu.cqu.smartacademia.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import au.edu.cqu.smartacademia.R
import au.edu.cqu.smartacademia.database.User
import au.edu.cqu.smartacademia.viewmodel.UserViewModel

/**
 * Activity responsible for registering new SmartAcademia users.
 *
 * Supports:
 * - New account creation.
 * - Duplicate email checking.
 * - Room database user storage.
 * - Navigation to OTP verification.
 * - Navigation back to login screen.
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel

    /**
     * Creates and initialises the registration screen.
     *
     * Sets up:
     * - ViewModel
     * - Registration form fields
     * - Create account action
     * - Back to login navigation
     *
     * @param savedInstanceState Previously saved activity state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        userViewModel =
            ViewModelProvider(this)[UserViewModel::class.java]

        val fullNameEditText =
            findViewById<EditText>(R.id.fullNameEditText)

        val emailEditText =
            findViewById<EditText>(R.id.emailEditText)

        val passwordEditText =
            findViewById<EditText>(R.id.passwordEditText)

        val institutionEditText =
            findViewById<EditText>(R.id.institutionEditText)

        val timezoneEditText =
            findViewById<EditText>(R.id.timezoneEditText)

        val createAccountButton =
            findViewById<Button>(R.id.createAccountButton)

        val backToLoginButton =
            findViewById<Button>(R.id.backToLoginButton)

        backToLoginButton.setOnClickListener {
            finish()
        }

        createAccountButton.setOnClickListener {

            val fullName =
                fullNameEditText.text.toString().trim()

            val email =
                emailEditText.text.toString().trim()

            val password =
                passwordEditText.text.toString().trim()

            val institution =
                institutionEditText.text.toString().trim()

            val timezone =
                timezoneEditText.text.toString().trim()

            if (
                fullName.isEmpty() ||
                email.isEmpty() ||
                password.isEmpty() ||
                institution.isEmpty()
            ) {
                Toast.makeText(
                    this,
                    getString(R.string.empty_fields_message),
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val user = User(
                email = email,
                fullName = fullName,
                password = password,
                institution = institution,
                timezone = timezone,
                isVerified = false
            )

            userViewModel.registerUser(user) { success ->

                if (success) {

                    Toast.makeText(
                        this,
                        getString(R.string.register_success_message),
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
                    finish()

                } else {

                    Toast.makeText(
                        this,
                        getString(R.string.email_exists_message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}