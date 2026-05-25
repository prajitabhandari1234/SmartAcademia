package au.edu.cqu.smartacademia.database

class UserRepository(private val userDao: UserDao) {

    suspend fun registerUser(user: User): Boolean {
        val existingUser = userDao.getUserByEmail(user.email)
        return if (existingUser == null) {
            userDao.insertUser(user)
            true
        } else {
            false
        }
    }

    suspend fun login(email: String, password: String): User? {
        return userDao.login(email, password)
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }
}