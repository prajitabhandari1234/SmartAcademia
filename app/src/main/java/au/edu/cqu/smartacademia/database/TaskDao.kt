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

    /**
     * Inserts a task into the database.
     *
     * If a task with the same ID already exists,
     * it will be replaced.
     *
     * @param task Task to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    /**
     * Inserts multiple tasks into the database.
     *
     * Used when importing tasks from a remote server.
     *
     * @param tasks List of tasks to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>)

    /**
     * Updates an existing task.
     *
     * @param task Updated task object.
     */
    @Update
    suspend fun updateTask(task: Task)

    /**
     * Deletes a task from the database.
     *
     * @param task Task to delete.
     */
    @Delete
    suspend fun deleteTask(task: Task)

    /**
     * Returns all tasks belonging to a user.
     *
     * Results are ordered by priority score.
     *
     * @param email User email.
     * @return LiveData list of tasks.
     */
    @Query(
        "SELECT * FROM tasks " +
                "WHERE userEmail = :email " +
                "ORDER BY priorityScore DESC"
    )
    fun getTasksForUser(email: String): LiveData<List<Task>>

    /**
     * Returns the total number of tasks stored.
     *
     * @return Number of tasks.
     */
    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getTaskCount(): Int

    /**
     * Returns all completed tasks for a user.
     *
     * @param email User email.
     * @return LiveData list of completed tasks.
     */
    @Query(
        "SELECT * FROM tasks " +
                "WHERE userEmail = :email " +
                "AND completed = 1"
    )
    fun getCompletedTasks(email: String): LiveData<List<Task>>

    /**
     * Returns all active tasks for a user.
     *
     * @param email User email.
     * @return LiveData list of active tasks.
     */
    @Query(
        "SELECT * FROM tasks " +
                "WHERE userEmail = :email " +
                "AND completed = 0"
    )
    fun getActiveTasks(email: String): LiveData<List<Task>>

    /**
     * Finds a task by its ID.
     *
     * @param taskId Task identifier.
     * @return Matching task or null.
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: String): Task?

    /**
     * Deletes a task using its ID.
     *
     * @param taskId Task identifier.
     */
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    /**
     * Finds a task by title and user email.
     *
     * @param title Task title.
     * @param email User email.
     * @return Matching task or null.
     */
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