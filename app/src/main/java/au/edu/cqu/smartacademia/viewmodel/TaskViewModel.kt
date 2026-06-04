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
 * Acts as the connection between the UI layer and [TaskRepository].
 *
 * Supports Assignment 3 requirements:
 * - ViewModel architecture.
 * - LiveData observation.
 * - Room database access.
 * - RecyclerView task updates.
 * - HTTP task fetching.
 * - Task creation, editing, deletion and completion.
 */
class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository

    init {
        val taskDao = SmartAcademiaDatabase
            .getDatabase(application)
            .taskDao()

        repository = TaskRepository(taskDao)
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
     * Returns only active incomplete tasks for a user.
     *
     * @param email Logged-in user email.
     * @return LiveData list of active tasks.
     */
    fun getActiveTasks(email: String): LiveData<List<Task>> {
        return repository.getActiveTasks(email)
    }

    /**
     * Returns completed tasks for a user.
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
     * Used for editing tasks and marking tasks as completed.
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
     * Used when opening the edit task screen.
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
     * Fetches remote task data from the API
     * and saves new tasks into the Room database.
     *
     * @param userEmail Logged-in user email.
     */
    fun fetchTasksFromApi(userEmail: String) {
        viewModelScope.launch {
            repository.fetchTasksFromApi(userEmail)
        }
    }
}