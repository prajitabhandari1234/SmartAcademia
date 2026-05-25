package au.edu.cqu.smartacademia.utils

import au.edu.cqu.smartacademia.database.Task
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.max

object ScheduleGenerator {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    fun calculateDaysRemaining(deadline: String): Long {
        return try {
            val deadlineDate = dateFormat.parse(deadline) ?: return 1
            val diff = deadlineDate.time - Date().time
            TimeUnit.MILLISECONDS.toDays(diff)
        } catch (e: Exception) {
            1
        }
    }

    fun calculatePriorityScore(task: Task): Int {
        val daysRemaining = calculateDaysRemaining(task.deadline)
        val safeDays = max(daysRemaining, 1)
        return ((task.weight * task.estimatedHours) / safeDays).toInt()
    }

    fun generateTodayPlan(tasks: List<Task>): String {
        val activeTasks = tasks.filter { !it.completed }
            .sortedByDescending { calculatePriorityScore(it) }
            .take(3)

        if (activeTasks.isEmpty()) {
            return "No study tasks available today"
        }

        return activeTasks.mapIndexed { index, task ->
            "${index + 1}. ${task.title} (${task.estimatedHours} hrs)"
        }.joinToString("\n")
    }

    fun countOverdue(tasks: List<Task>): Int {
        return tasks.count { calculateDaysRemaining(it.deadline) < 0 && !it.completed }
    }

    fun countDueToday(tasks: List<Task>): Int {
        return tasks.count { calculateDaysRemaining(it.deadline) == 0L && !it.completed }
    }

    fun countThisWeek(tasks: List<Task>): Int {
        return tasks.count {
            val days = calculateDaysRemaining(it.deadline)
            days in 0..7 && !it.completed
        }
    }
}