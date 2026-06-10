package au.edu.cqu.smartacademia.database

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * Data Access Object for managing Task entities.
 *
 * Provides database operations for creating, reading,
 * updating and deleting academic tasks stored in Room.
 */
@Dao
interface TaskDao {

    /**
     * Inserts a task into the database.
     *
     * @param task Task to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    /**
     * Inserts multiple tasks into the database.
     *
     * @param tasks List of tasks to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>)

    /**
     * Updates an existing task.
     *
     * @param task Updated task.
     */
    @Update
    suspend fun updateTask(task: Task)

    /**
     * Deletes a task.
     *
     * @param task Task to delete.
     */
    @Delete
    suspend fun deleteTask(task: Task)

    /**
     * Returns all tasks for the logged-in user.
     *
     * @param email Logged-in user email.
     * @return LiveData list of user tasks.
     */
    @Query(
        "SELECT * FROM tasks " +
                "WHERE userEmail = :email " +
                "ORDER BY priorityScore DESC"
    )
    fun getTasksForUser(email: String): LiveData<List<Task>>

    /**
     * Returns all tasks linked to a selected unit.
     *
     * @param email Logged-in user email.
     * @param unitId Selected unit ID.
     * @return LiveData list of tasks inside the unit.
     */
    @Query(
        "SELECT * FROM tasks " +
                "WHERE userEmail = :email " +
                "AND unitId = :unitId " +
                "ORDER BY priorityScore DESC"
    )
    fun getTasksForUnit(
        email: String,
        unitId: String
    ): LiveData<List<Task>>

    /**
     * Deletes all tasks linked to a selected unit.
     *
     * Used when deleting a unit so its assignments
     * are removed at the same time.
     *
     * @param unitId Selected unit ID.
     */
    @Query("DELETE FROM tasks WHERE unitId = :unitId")
    suspend fun deleteTasksForUnit(unitId: String)

    /**
     * Returns total number of tasks for one logged-in user.
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

    /**
     * Returns completed tasks for a user.
     *
     * @param email Logged-in user email.
     * @return LiveData list of completed tasks.
     */
    @Query(
        "SELECT * FROM tasks " +
                "WHERE userEmail = :email " +
                "AND completed = 1"
    )
    fun getCompletedTasks(email: String): LiveData<List<Task>>

    /**
     * Returns active incomplete tasks for a user.
     *
     * @param email Logged-in user email.
     * @return LiveData list of active tasks.
     */
    @Query(
        "SELECT * FROM tasks " +
                "WHERE userEmail = :email " +
                "AND completed = 0"
    )
    fun getActiveTasks(email: String): LiveData<List<Task>>

    /**
     * Returns a task using its ID.
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
     * Returns a task by title for duplicate checking.
     *
     * @param title Task title.
     * @param email Logged-in user email.
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