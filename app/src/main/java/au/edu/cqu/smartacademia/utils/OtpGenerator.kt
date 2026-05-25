package au.edu.cqu.smartacademia.utils

object OtpGenerator {
    fun generateOtp(): String {
        return (100000..999999).random().toString()
    }

    fun getExpiryTime(): Long {
        return System.currentTimeMillis() + 10 * 60 * 1000
    }

    fun isExpired(expiryTime: Long): Boolean {
        return System.currentTimeMillis() > expiryTime
    }
}