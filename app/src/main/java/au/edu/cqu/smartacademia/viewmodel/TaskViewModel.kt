package au.edu.cqu.smartacademia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import au.edu.cqu.smartacademia.database.SmartAcademiaDatabase
import au.edu.cqu.smartacademia.database.Task
import au.edu.cqu.smartacademia.database.TaskRepository
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository

    init {
        val taskDao = SmartAcademiaDatabase.getDatabase(application).taskDao()
        repository = TaskRepository(taskDao)
    }

    fun getTasksForUser(email: String): LiveData<List<Task>> {
        return repository.getTasksForUser(email)
    }

    fun getActiveTasks(email: String): LiveData<List<Task>> {
        return repository.getActiveTasks(email)
    }

    fun getCompletedTasks(email: String): LiveData<List<Task>> {
        return repository.getCompletedTasks(email)
    }

    fun insertTask(task: Task) {
        viewModelScope.launch {
            repository.insertTask(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun loadSeedData(userEmail: String) {
        viewModelScope.launch {
            repository.loadSeedDataIfEmpty(userEmail)
        }
    }

    fun getTaskById(taskId: String, result: (Task?) -> Unit) {
        viewModelScope.launch {
            result(repository.getTaskById(taskId))
        }
    }

    fun deleteTaskById(taskId: String) {
        viewModelScope.launch {
            repository.deleteTaskById(taskId)
        }
    }
}