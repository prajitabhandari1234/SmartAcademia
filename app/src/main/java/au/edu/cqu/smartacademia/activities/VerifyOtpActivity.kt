package au.edu.cqu.smartacademia.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import au.edu.cqu.smartacademia.MainActivity
import au.edu.cqu.smartacademia.R
import au.edu.cqu.smartacademia.utils.OtpGenerator
import au.edu.cqu.smartacademia.viewmodel.UserViewModel

/**
 * Activity responsible for verifying user accounts using OTP.
 *
 * Supports OTP generation, OTP expiry validation,
 * OTP resend functionality, user account verification,
 * session creation and navigation to the dashboard.
 */
class VerifyOtpActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel
    private lateinit var email: String
    private lateinit var demoOtpTextView: TextView
    private lateinit var otpEditText: EditText

    /**
     * Creates and initialises the OTP verification screen.
     *
     * Sets up the ViewModel, retrieves the user email,
     * generates the first OTP and configures verify/resend actions.
     *
     * @param savedInstanceState Previously saved activity state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_verify_otp)

        userViewModel =
            ViewModelProvider(this)[UserViewModel::class.java]

        email =
            intent.getStringExtra("email") ?: ""

        demoOtpTextView =
            findViewById(R.id.demoOtpTextView)

        otpEditText =
            findViewById(R.id.otpEditText)

        val verifyOtpButton =
            findViewById<Button>(R.id.verifyOtpButton)

        val resendOtpButton =
            findViewById<Button>(R.id.resendOtpButton)

        generateAndSaveOtp()

        resendOtpButton.setOnClickListener {
            generateAndSaveOtp()
        }

        verifyOtpButton.setOnClickListener {
            verifyOtp()
        }
    }

    /**
     * Generates a new OTP and saves it with
     * an expiry timestamp in the Room database.
     *
     * The OTP is displayed for demonstration
     * and testing purposes.
     */
    private fun generateAndSaveOtp() {
        userViewModel.getUserByEmail(email) { user ->

            if (user == null) {
                Toast.makeText(
                    this,
                    getString(R.string.user_not_found_message),
                    Toast.LENGTH_SHORT
                ).show()

                return@getUserByEmail
            }

            val otp =
                OtpGenerator.generateOtp()

            user.otpCode =
                otp

            user.otpExpiry =
                OtpGenerator.getExpiryTime()

            userViewModel.updateUser(user)

            demoOtpTextView.text =
                getString(
                    R.string.demo_otp_label,
                    otp
                )

            Toast.makeText(
                this,
                getString(R.string.otp_sent_message),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Verifies the OTP entered by the user.
     *
     * Validation checks:
     * - User exists.
     * - OTP has not expired.
     * - OTP matches the saved code.
     *
     * On success, the account is marked as verified,
     * OTP data is cleared, session data is saved
     * and MainActivity is launched.
     */
    private fun verifyOtp() {
        val enteredOtp =
            otpEditText.text.toString().trim()

        userViewModel.getUserByEmail(email) { user ->

            if (user == null) {
                Toast.makeText(
                    this,
                    getString(R.string.user_not_found_message),
                    Toast.LENGTH_SHORT
                ).show()

                return@getUserByEmail
            }

            if (OtpGenerator.isExpired(user.otpExpiry)) {
                Toast.makeText(
                    this,
                    getString(R.string.otp_expired_message),
                    Toast.LENGTH_SHORT
                ).show()

                return@getUserByEmail
            }

            if (enteredOtp != user.otpCode) {
                Toast.makeText(
                    this,
                    getString(R.string.otp_invalid_message),
                    Toast.LENGTH_SHORT
                ).show()

                return@getUserByEmail
            }

            user.isVerified = true
            user.otpCode = null
            user.otpExpiry = 0L

            userViewModel.updateUser(user)

            getSharedPreferences(
                "smartacademia_session",
                MODE_PRIVATE
            )
                .edit()
                .putString(
                    "email",
                    user.email
                )
                .putString(
                    "full_name",
                    user.fullName
                )
                .apply()

            Toast.makeText(
                this,
                getString(R.string.otp_verified_message),
                Toast.LENGTH_SHORT
            ).show()

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