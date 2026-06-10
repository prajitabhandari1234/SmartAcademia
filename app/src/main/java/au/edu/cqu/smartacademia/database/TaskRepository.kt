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
     * Returns tasks belonging to a selected unit.
     *
     * @param email User email.
     * @param unitId Selected unit ID.
     * @return LiveData list of tasks inside the unit.
     */
    fun getTasksForUnit(
        email: String,
        unitId: String
    ): LiveData<List<Task>> {
        return taskDao.getTasksForUnit(email, unitId)
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
     * Loads sample seed data when the database is empty.
     *
     * @param userEmail Logged-in user email.
     */
    suspend fun loadSeedDataIfEmpty(userEmail: String) {
        if (taskDao.getTaskCount() == 0) {

            val seedTasks = listOf(
                Task(
                    userEmail = userEmail,
                    title = "Assignment 2 Portfolio",
                    course = "COIT13234",
                    deadline = "2026-05-15 23:45",
                    weight = 30,
                    estimatedHours = 5,
                    notes = "Complete mobile app design portfolio",
                    priorityScore = 90,
                    locationName = "CQU Sydney",
                    lat = -33.8688,
                    lon = 151.2093
                ),

                Task(
                    userEmail = userEmail,
                    title = "Assignment 3 Final Project",
                    course = "COIT13234",
                    deadline = "2026-06-05 23:45",
                    weight = 40,
                    estimatedHours = 10,
                    notes = "Build SmartAcademia prototype",
                    priorityScore = 95,
                    locationName = "CQU Sydney",
                    lat = -33.8858,
                    lon = 151.2073
                ),

                Task(
                    userEmail = userEmail,
                    title = "Weekly Quiz",
                    course = "COIT13229",
                    deadline = "2026-05-28 18:00",
                    weight = 10,
                    estimatedHours = 2,
                    notes = "Revise distributed systems concepts",
                    priorityScore = 70,
                    locationName = "Town Hall Sydney",
                    lat = -33.8731,
                    lon = 151.2065
                )
            )

            taskDao.insertTasks(seedTasks)
        }
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