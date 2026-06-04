package au.edu.cqu.smartacademia.utils

/**
 * Utility object responsible for generating
 * and validating One-Time Passwords (OTP).
 *
 * Supports user account verification by:
 * - Generating secure 6-digit OTP codes.
 * - Setting OTP expiry times.
 * - Validating OTP expiration status.
 */
object OtpGenerator {

    /**
     * Generates a random 6-digit OTP code.
     *
     * @return Random OTP string between 100000 and 999999.
     */
    fun generateOtp(): String {
        return (100000..999999).random().toString()
    }

    /**
     * Calculates the OTP expiry timestamp.
     *
     * OTPs remain valid for 10 minutes from
     * the time they are generated.
     *
     * @return Expiry time in milliseconds.
     */
    fun getExpiryTime(): Long {
        return System.currentTimeMillis() + 10 * 60 * 1000
    }

    /**
     * Checks whether an OTP has expired.
     *
     * @param expiryTime Stored OTP expiry timestamp.
     * @return True if expired, otherwise false.
     */
    fun isExpired(expiryTime: Long): Boolean {
        return System.currentTimeMillis() > expiryTime
    }
}