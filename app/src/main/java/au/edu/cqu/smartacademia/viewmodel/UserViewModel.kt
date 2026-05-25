package au.edu.cqu.smartacademia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import au.edu.cqu.smartacademia.database.SmartAcademiaDatabase
import au.edu.cqu.smartacademia.database.User
import au.edu.cqu.smartacademia.database.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserRepository

    init {
        val userDao = SmartAcademiaDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
    }

    fun registerUser(user: User, result: (Boolean) -> Unit) {
        viewModelScope.launch {
            result(repository.registerUser(user))
        }
    }

    fun login(email: String, password: String, result: (User?) -> Unit) {
        viewModelScope.launch {
            result(repository.login(email, password))
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            repository.updateUser(user)
        }
    }

    fun getUserByEmail(email: String, result: (User?) -> Unit) {
        viewModelScope.launch {
            result(repository.getUserByEmail(email))
        }
    }
}