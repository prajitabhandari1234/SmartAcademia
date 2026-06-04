package au.edu.cqu.smartacademia.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * User entity used by the SmartAcademia application.
 * Stores user account information in the Room database.
 * Assignment 3 Requirements Supported:
 * - User registration and login.
 * - OTP email verification.
 * - Session management.
 * - Personalised dashboard greeting.
 * - User-specific task management.
 * Each user is uniquely identified by their email address.
 */
@Entity(tableName = "users")
data class User(

    /**
     * Unique email address used as the primary key.
     * Each email can only be registered once.
     */
    @PrimaryKey
    val email: String,

    /**
     * User's full name.
     * Displayed on the dashboard greeting.
     */
    var fullName: String,

    /**
     * User password used for authentication.
     * Note:
     * In a production application this should be encrypted.
     */
    var password: String,

    /**
     * User's institution or university.
     */
    var institution: String,

    /**
     * User's preferred timezone.
     * Used for scheduling and reminder features.
     */
    var timezone: String,

    /**
     * Indicates whether the account has been verified.
     * Users must verify their account before login.
     */
    var isVerified: Boolean = false,

    /**
     * One-Time Password (OTP) generated during verification.
     */
    var otpCode: String? = null,

    /**
     * Expiry timestamp for the OTP.
     * Used to ensure verification codes expire after a defined period.
     */
    var otpExpiry: Long = 0L
)