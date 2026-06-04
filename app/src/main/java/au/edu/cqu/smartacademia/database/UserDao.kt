package au.edu.cqu.smartacademia.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * Data Access Object (DAO) for managing User entities.
 *
 * Provides database operations related to:
 * - User registration.
 * - User login.
 * - OTP verification.
 * - Account updates.
 *
 * All user information is stored in the Room database.
 */
@Dao
interface UserDao {

    /**
     * Inserts a new user into the database.
     *
     * Registration will fail if a user with the same
     * email address already exists.
     *
     * @param user User account to insert.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User)

    /**
     * Updates an existing user record.
     *
     * Used for:
     * - OTP generation.
     * - OTP verification.
     * - Profile updates.
     *
     * @param user Updated user information.
     */
    @Update
    suspend fun updateUser(user: User)

    /**
     * Retrieves a user using their email address.
     *
     * Used during:
     * - Registration validation.
     * - OTP verification.
     * - Session management.
     *
     * @param email User email.
     * @return Matching user or null.
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    /**
     * Authenticates a user using email and password.
     *
     * Used during login.
     *
     * @param email User email.
     * @param password User password.
     * @return Matching user if credentials are valid, otherwise null.
     */
    @Query(
        "SELECT * FROM users " +
                "WHERE email = :email " +
                "AND password = :password " +
                "LIMIT 1"
    )
    suspend fun login(
        email: String,
        password: String
    ): User?
}