package au.edu.cqu.smartacademia.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val email: String,
    var fullName: String,
    var password: String,
    var institution: String,
    var timezone: String,
    var isVerified: Boolean = false,
    var otpCode: String? = null,
    var otpExpiry: Long = 0L
)