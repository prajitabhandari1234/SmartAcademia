package au.edu.cqu.smartacademia.database

import androidx.lifecycle.LiveData
import au.edu.cqu.smartacademia.network.RetrofitInstance
import au.edu.cqu.smartacademia.utils.ScheduleGenerator

/**
 * Repository responsible for managing task data.
 *
 * Acts as a bridge between the ViewModel and the Room database.
 */
class TaskRepository(private val taskDao: TaskDao) {

    /**
     * Returns all tasks belonging to a user.
     *
     * @param email User email.
     * @return LiveData list of tasks.
     */
    fun getTasksForUser(email: String): LiveData<List<Task>> {
        return taskDao.getTasksForUser(email)
    }

    /**
     * Returns only tasks linked to any unit.
     *
     * Used by the Home dashboard so old unlinked demo tasks
     * do not appear in the study plan.
     *
     * @param email User email.
     * @return LiveData list of unit-linked tasks.
     */
    fun getTasksLinkedToUnits(email: String): LiveData<List<Task>> {
        return taskDao.getTasksLinkedToUnits(email)
    }

    /**
     * Returns tasks belonging to a selected unit.
     *
     * Supports both new unitId-linked assignments and older
     * course-code based assignments.
     *
     * @param email User email.
     * @param unitId Selected unit ID.
     * @param unitCode Selected unit code.
     * @return LiveData list of tasks inside the unit.
     */
    fun getTasksForUnit(
        email: String,
        unitId: String,
        unitCode: String
    ): LiveData<List<Task>> {
        return taskDao.getTasksForUnit(
            email,
            unitId,
            unitCode
        )
    }

    /**
     * Returns active incomplete tasks.
     *
     * @param email User email.
     * @return LiveData list of active tasks.
     */
    fun getActiveTasks(email: String): LiveData<List<Task>> {
        return taskDao.getActiveTasks(email)
    }

    /**
     * Returns completed tasks.
     *
     * @param email User email.
     * @return LiveData list of completed tasks.
     */
    fun getCompletedTasks(email: String): LiveData<List<Task>> {
        return taskDao.getCompletedTasks(email)
    }

    /**
     * Inserts a task after calculating its priority score.
     *
     * @param task Task to insert.
     */
    suspend fun insertTask(task: Task) {
        task.priorityScore =
            ScheduleGenerator.calculatePriorityScore(task)

        taskDao.insertTask(task)
    }

    /**
     * Inserts multiple tasks after calculating priority scores.
     *
     * @param tasks List of tasks.
     */
    suspend fun insertTasks(tasks: List<Task>) {
        tasks.forEach {
            it.priorityScore =
                ScheduleGenerator.calculatePriorityScore(it)
        }

        taskDao.insertTasks(tasks)
    }

    /**
     * Updates an existing task.
     *
     * @param task Updated task.
     */
    suspend fun updateTask(task: Task) {
        task.priorityScore =
            ScheduleGenerator.calculatePriorityScore(task)

        taskDao.updateTask(task)
    }

    /**
     * Retrieves a task using its ID.
     *
     * @param taskId Task identifier.
     * @return Matching task or null.
     */
    suspend fun getTaskById(taskId: String): Task? {
        return taskDao.getTaskById(taskId)
    }

    /**
     * Deletes a task using its ID.
     *
     * @param taskId Task identifier.
     */
    suspend fun deleteTaskById(taskId: String) {
        taskDao.deleteTaskById(taskId)
    }

    /**
     * Deletes all tasks linked to a selected unit.
     *
     * @param unitId Selected unit ID.
     */
    suspend fun deleteTasksForUnit(unitId: String) {
        taskDao.deleteTasksForUnit(unitId)
    }

    /**
     * Loads seed data only when needed.
     *
     * For portfolio use, this is intentionally kept empty
     * so demo tasks do not appear for real users.
     *
     * @param userEmail Logged-in user email.
     */
    suspend fun loadSeedDataIfEmpty(userEmail: String) {
        // Seed data disabled for portfolio-ready behaviour.
    }

    /**
     * Fetches tasks from a remote JSON API.
     *
     * New tasks are inserted into the Room database.
     * Existing tasks are ignored to prevent duplicates.
     *
     * @param userEmail Logged-in user email.
     */
    suspend fun fetchTasksFromApi(userEmail: String) {
        try {
            val response =
                RetrofitInstance.api.getTasks()

            if (response.isSuccessful) {
                val apiTasks =
                    response.body() ?: emptyList()

                apiTasks.forEach { apiTask ->

                    val existingTask =
                        taskDao.getTaskByTitle(
                            apiTask.title,
                            userEmail
                        )

                    if (existingTask == null) {
                        val task = Task(
                            userEmail = userEmail,
                            title = apiTask.title,
                            course = apiTask.course,
                            deadline = apiTask.deadline,
                            weight = apiTask.weight,
                            estimatedHours = apiTask.estimatedHours,
                            notes = apiTask.notes,
                            lat = apiTask.lat,
                            lon = apiTask.lon
                        )

                        task.priorityScore =
                            ScheduleGenerator.calculatePriorityScore(task)

                        taskDao.insertTask(task)
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}