package au.edu.cqu.smartacademia.database

/**
 * Repository responsible for managing user data.
 *
 * Acts as a bridge between the ViewModel and Room database.
 *
 * Responsibilities:
 * - User registration.
 * - User authentication.
 * - User retrieval.
 * - OTP verification updates.
 * - User account management.
 */
class UserRepository(private val userDao: UserDao) {

    /**
     * Registers a new user account.
     *
     * Registration succeeds only if the email address
     * does not already exist in the database.
     *
     * @param user User account to register.
     * @return true if registration succeeds, otherwise false.
     */
    suspend fun registerUser(user: User): Boolean {

        val existingUser = userDao.getUserByEmail(user.email)

        return if (existingUser == null) {
            userDao.insertUser(user)
            true
        } else {
            false
        }
    }

    /**
     * Authenticates a user using email and password.
     *
     * @param email User email.
     * @param password User password.
     * @return Matching user if credentials are valid,
     * otherwise null.
     */
    suspend fun login(
        email: String,
        password: String
    ): User? {
        return userDao.login(email, password)
    }

    /**
     * Retrieves a user account by email address.
     *
     * Used for:
     * - Registration validation.
     * - OTP verification.
     * - Session management.
     *
     * @param email User email.
     * @return Matching user or null.
     */
    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

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
    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }
}