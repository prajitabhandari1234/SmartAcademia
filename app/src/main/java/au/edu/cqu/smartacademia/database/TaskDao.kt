package au.edu.cqu.smartacademia.database

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * Data Access Object (DAO) for managing [Task] entities.
 *
 * Provides database operations for creating, reading,
 * updating and deleting academic tasks stored in Room.
 */
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

    @Query(
        "SELECT * FROM tasks " +
                "WHERE userEmail = :email " +
                "ORDER BY priorityScore DESC"
    )
    fun getTasksForUser(email: String): LiveData<List<Task>>

    /**
     * Returns total number of tasks for one logged-in user.
     *
     * Used so each new user receives seed data
     * when their own task list is empty.
     *
     * @param email Logged-in user email.
     * @return Number of tasks for that user.
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE userEmail = :email")
    suspend fun getTaskCountForUser(email: String): Int

    /**
     * Returns the total number of tasks stored in the app.
     *
     * @return Total number of tasks.
     */
    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getTaskCount(): Int

    @Query(
        "SELECT * FROM tasks " +
                "WHERE userEmail = :email " +
                "AND completed = 1"
    )
    fun getCompletedTasks(email: String): LiveData<List<Task>>

    @Query(
        "SELECT * FROM tasks " +
                "WHERE userEmail = :email " +
                "AND completed = 0"
    )
    fun getActiveTasks(email: String): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: String): Task?

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    @Query(
        "SELECT * FROM tasks " +
                "WHERE title = :title " +
                "AND userEmail = :email " +
                "LIMIT 1"
    )
    suspend fun getTaskByTitle(
        title: String,
        email: String
    ): Task?
}