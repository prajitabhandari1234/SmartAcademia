package au.edu.cqu.smartacademia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import au.edu.cqu.smartacademia.database.SmartAcademiaDatabase
import au.edu.cqu.smartacademia.database.User
import au.edu.cqu.smartacademia.database.UserRepository
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for user account management.
 *
 * Acts as the connection between the UI layer and
 * the UserRepository.
 *
 * Supports:
 * - User registration.
 * - User login.
 * - Email verification.
 * - OTP verification workflow.
 * - User profile updates.
 *
 * Uses Android Architecture Components:
 * - ViewModel
 * - Room Database
 * - Repository Pattern
 * - Coroutines
 */
class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserRepository

    init {

        val userDao = SmartAcademiaDatabase
            .getDatabase(application)
            .userDao()

        repository = UserRepository(userDao)
    }

    /**
     * Registers a new user account.
     *
     * Checks whether the email address already exists
     * before inserting the user into the database.
     *
     * @param user User object to register.
     * @param result Callback returning true if registration succeeds.
     */
    fun registerUser(
        user: User,
        result: (Boolean) -> Unit
    ) {

        viewModelScope.launch {
            result(repository.registerUser(user))
        }
    }

    /**
     * Attempts user authentication.
     *
     * Validates the supplied email and password
     * against stored database records.
     *
     * @param email User email address.
     * @param password User password.
     * @param result Callback returning the matching user or null.
     */
    fun login(
        email: String,
        password: String,
        result: (User?) -> Unit
    ) {

        viewModelScope.launch {
            result(repository.login(email, password))
        }
    }

    /**
     * Updates an existing user record.
     *
     * Used for:
     * - Email verification.
     * - OTP updates.
     * - User profile updates.
     *
     * @param user Updated user object.
     */
    fun updateUser(user: User) {

        viewModelScope.launch {
            repository.updateUser(user)
        }
    }

    /**
     * Retrieves a user using their email address.
     *
     * Used during:
     * - Login validation.
     * - OTP verification.
     * - Account management.
     *
     * @param email User email address.
     * @param result Callback returning the matching user or null.
     */
    fun getUserByEmail(
        email: String,
        result: (User?) -> Unit
    ) {

        viewModelScope.launch {
            result(repository.getUserByEmail(email))
        }
    }
}