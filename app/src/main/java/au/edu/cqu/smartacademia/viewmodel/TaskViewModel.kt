package au.edu.cqu.smartacademia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import au.edu.cqu.smartacademia.database.SmartAcademiaDatabase
import au.edu.cqu.smartacademia.database.Task
import au.edu.cqu.smartacademia.database.TaskRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for managing academic task data.
 *
 * Acts as the connection between the UI layer and TaskRepository.
 */
class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository

    init {
        val taskDao =
            SmartAcademiaDatabase
                .getDatabase(application)
                .taskDao()

        repository =
            TaskRepository(taskDao)
    }

    /**
     * Returns all tasks for a specific user.
     *
     * @param email Logged-in user email.
     * @return LiveData list of tasks.
     */
    fun getTasksForUser(email: String): LiveData<List<Task>> {
        return repository.getTasksForUser(email)
    }

    /**
     * Returns tasks linked to a selected unit.
     *
     * @param email Logged-in user email.
     * @param unitId Selected unit ID.
     * @return LiveData list of unit tasks.
     */
    fun getTasksForUnit(
        email: String,
        unitId: String,
        unitCode: String
    ): LiveData<List<Task>> {
        return repository.getTasksForUnit(
            email,
            unitId,
            unitCode
        )
    }

    /**
     * Returns only active incomplete tasks.
     *
     * @param email Logged-in user email.
     * @return LiveData list of active tasks.
     */
    fun getActiveTasks(email: String): LiveData<List<Task>> {
        return repository.getActiveTasks(email)
    }

    /**
     * Returns completed tasks.
     *
     * @param email Logged-in user email.
     * @return LiveData list of completed tasks.
     */
    fun getCompletedTasks(email: String): LiveData<List<Task>> {
        return repository.getCompletedTasks(email)
    }

    /**
     * Inserts a new task into the database.
     *
     * @param task Task to insert.
     */
    fun insertTask(task: Task) {
        viewModelScope.launch {
            repository.insertTask(task)
        }
    }

    /**
     * Updates an existing task.
     *
     * @param task Updated task.
     */
    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    /**
     * Loads initial seed data when the database is empty.
     *
     * @param userEmail Logged-in user email.
     */
    fun loadSeedData(userEmail: String) {
        viewModelScope.launch {
            repository.loadSeedDataIfEmpty(userEmail)
        }
    }

    /**
     * Retrieves a task by ID.
     *
     * @param taskId Task identifier.
     * @param result Callback returning the matching task or null.
     */
    fun getTaskById(
        taskId: String,
        result: (Task?) -> Unit
    ) {
        viewModelScope.launch {
            result(repository.getTaskById(taskId))
        }
    }

    /**
     * Deletes a task using its unique ID.
     *
     * @param taskId Task identifier.
     */
    fun deleteTaskById(taskId: String) {
        viewModelScope.launch {
            repository.deleteTaskById(taskId)
        }
    }

    /**
     * Deletes all tasks linked to a selected unit.
     *
     * @param unitId Selected unit ID.
     */
    fun deleteTasksForUnit(unitId: String) {
        viewModelScope.launch {
            repository.deleteTasksForUnit(unitId)
        }
    }

    /**
     * Fetches remote task data from the API.
     *
     * @param userEmail Logged-in user email.
     */
    fun fetchTasksFromApi(userEmail: String) {
        viewModelScope.launch {
            repository.fetchTasksFromApi(userEmail)
        }
    }
}