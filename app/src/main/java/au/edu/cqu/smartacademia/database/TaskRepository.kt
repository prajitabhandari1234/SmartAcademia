package au.edu.cqu.smartacademia.database

import androidx.lifecycle.LiveData
import au.edu.cqu.smartacademia.network.RetrofitInstance

class TaskRepository(private val taskDao: TaskDao) {

    fun getTasksForUser(email: String): LiveData<List<Task>> {
        return taskDao.getTasksForUser(email)
    }

    fun getActiveTasks(email: String): LiveData<List<Task>> {
        return taskDao.getActiveTasks(email)
    }

    fun getCompletedTasks(email: String): LiveData<List<Task>> {
        return taskDao.getCompletedTasks(email)
    }

    suspend fun insertTask(task: Task) {
        task.priorityScore = au.edu.cqu.smartacademia.utils.ScheduleGenerator.calculatePriorityScore(task)
        taskDao.insertTask(task)
    }

    suspend fun insertTasks(tasks: List<Task>) {
        tasks.forEach {
            it.priorityScore = au.edu.cqu.smartacademia.utils.ScheduleGenerator.calculatePriorityScore(it)
        }
        taskDao.insertTasks(tasks)
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun getTaskById(taskId: String): Task? {
        return taskDao.getTaskById(taskId)
    }

    suspend fun deleteTaskById(taskId: String) {
        taskDao.deleteTaskById(taskId)
    }

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
                    lat = -33.8731,
                    lon = 151.2065
                )
            )

            taskDao.insertTasks(seedTasks)
        }
    }
    suspend fun fetchTasksFromApi(userEmail: String) {
        try {
            val response = RetrofitInstance.api.getTasks()

            if (response.isSuccessful) {
                val apiTasks = response.body() ?: emptyList()

                apiTasks.forEach { apiTask ->

                    val existingTask =
                        taskDao.getTaskByTitle(apiTask.title, userEmail)

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
                            au.edu.cqu.smartacademia.utils.ScheduleGenerator.calculatePriorityScore(task)

                        taskDao.insertTask(task)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}