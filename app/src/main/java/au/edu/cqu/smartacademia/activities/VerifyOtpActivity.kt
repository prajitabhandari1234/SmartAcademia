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

class VerifyOtpActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel
    private lateinit var email: String
    private lateinit var demoOtpTextView: TextView
    private lateinit var otpEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_verify_otp)

        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        email = intent.getStringExtra("email") ?: ""

        demoOtpTextView = findViewById(R.id.demoOtpTextView)
        otpEditText = findViewById(R.id.otpEditText)

        val verifyOtpButton = findViewById<Button>(R.id.verifyOtpButton)
        val resendOtpButton = findViewById<Button>(R.id.resendOtpButton)

        generateAndSaveOtp()

        resendOtpButton.setOnClickListener {
            generateAndSaveOtp()
        }

        verifyOtpButton.setOnClickListener {
            verifyOtp()
        }
    }

    private fun generateAndSaveOtp() {
        userViewModel.getUserByEmail(email) { user ->
            if (user == null) {
                Toast.makeText(this, getString(R.string.user_not_found_message), Toast.LENGTH_SHORT).show()
                return@getUserByEmail
            }

            val otp = OtpGenerator.generateOtp()
            user.otpCode = otp
            user.otpExpiry = OtpGenerator.getExpiryTime()

            userViewModel.updateUser(user)

            demoOtpTextView.text = "Demo OTP: $otp"
            Toast.makeText(this, getString(R.string.otp_sent_message), Toast.LENGTH_SHORT).show()
        }
    }

    private fun verifyOtp() {
        val enteredOtp = otpEditText.text.toString().trim()

        userViewModel.getUserByEmail(email) { user ->
            if (user == null) {
                Toast.makeText(this, getString(R.string.user_not_found_message), Toast.LENGTH_SHORT).show()
                return@getUserByEmail
            }

            if (OtpGenerator.isExpired(user.otpExpiry)) {
                Toast.makeText(this, getString(R.string.otp_expired_message), Toast.LENGTH_SHORT).show()
                return@getUserByEmail
            }

            if (enteredOtp != user.otpCode) {
                Toast.makeText(this, getString(R.string.otp_invalid_message), Toast.LENGTH_SHORT).show()
                return@getUserByEmail
            }

            user.isVerified = true
            user.otpCode = null
            user.otpExpiry = 0L
            userViewModel.updateUser(user)

            getSharedPreferences("smartacademia_session", MODE_PRIVATE)
                .edit()
                .putString("email", email)
                .apply()

            Toast.makeText(this, getString(R.string.otp_verified_message), Toast.LENGTH_SHORT).show()

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}