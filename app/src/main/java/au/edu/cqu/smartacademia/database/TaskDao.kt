package au.edu.cqu.smartacademia.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks WHERE userEmail = :email ORDER BY priorityScore DESC")
    fun getTasksForUser(email: String): LiveData<List<Task>>

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getTaskCount(): Int

    @Query("SELECT * FROM tasks WHERE userEmail = :email AND completed = 1")
    fun getCompletedTasks(email: String): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE userEmail = :email AND completed = 0")
    fun getActiveTasks(email: String): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: String): Task?

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)
}