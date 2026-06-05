package au.edu.cqu.smartacademia.utils

import au.edu.cqu.smartacademia.database.Task
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * Utility object responsible for deadline analysis,
 * task prioritisation, and rule-based smart scheduling.
 *
 * The scheduling logic prioritises urgent tasks first,
 * then uses academic impact to sort tasks within the same urgency group.
 */
object ScheduleGenerator {

    /**
     * Date format used by task deadlines.
     *
     * Example:
     * 2026-06-05 23:45
     */
    private val dateFormat =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    /**
     * Calculates the number of days remaining before a task deadline.
     *
     * @param deadline Task deadline in yyyy-MM-dd HH:mm format.
     * @return Number of days remaining, or 1 if parsing fails.
     */
    fun calculateDaysRemaining(deadline: String): Long {
        return try {
            val deadlineDate = dateFormat.parse(deadline) ?: return 1
            val difference = deadlineDate.time - Date().time

            TimeUnit.MILLISECONDS.toDays(difference)
        } catch (e: Exception) {
            1
        }
    }

    /**
     * Returns an urgency tier for a task.
     *
     * Lower tier numbers mean higher urgency.
     *
     * Tier 1: Overdue or due today.
     * Tier 2: Due tomorrow.
     * Tier 3: Due in 2-3 days.
     * Tier 4: Due this week.
     * Tier 5: Future task.
     *
     * @param task Academic task.
     * @return Urgency tier.
     */
    fun getUrgencyTier(task: Task): Int {
        val days = calculateDaysRemaining(task.deadline)

        return when {
            days < 0 -> 1
            days == 0L -> 2
            days == 1L -> 3
            days in 2..3 -> 4
            days in 4..7 -> 5
            else -> 6
        }
    }

    /**
     * Calculates academic impact score for a task.
     *
     * Formula:
     * (Weight x Estimated Hours) / Days Remaining
     *
     * This score is used after urgency tier sorting.
     *
     * @param task Academic task.
     * @return Calculated priority score.
     */
    fun calculatePriorityScore(task: Task): Int {
        val daysRemaining = calculateDaysRemaining(task.deadline)
        val safeDays = max(daysRemaining, 1)

        return ((task.weight * task.estimatedHours) / safeDays).toInt()
    }

    /**
     * Sorts tasks using the SmartAcademia smart scheduling rule.
     *
     * Sorting order:
     * 1. Urgency tier first.
     * 2. Priority score second.
     *
     * This ensures small tasks due today are not ignored
     * because of larger future assignments.
     *
     * @param tasks User task list.
     * @return Sorted task list.
     */
    fun sortBySmartPriority(tasks: List<Task>): List<Task> {
        return tasks
            .filter { !it.completed }
            .sortedWith(
                compareBy<Task> { getUrgencyTier(it) }
                    .thenByDescending { calculatePriorityScore(it) }
            )
    }

    /**
     * Generates today's recommended study plan.
     *
     * The method selects the top three tasks after applying
     * urgency-first smart scheduling.
     *
     * @param tasks User task list.
     * @return Formatted study plan text.
     */
    fun generateTodayPlan(tasks: List<Task>): String {
        val activeTasks = sortBySmartPriority(tasks).take(3)

        if (activeTasks.isEmpty()) {
            return "No study tasks available today"
        }

        return activeTasks.mapIndexed { index, task ->
            "${index + 1}. ${task.title} (${task.estimatedHours} hrs)"
        }.joinToString("\n")
    }

    /**
     * Counts overdue tasks.
     *
     * @param tasks User task list.
     * @return Number of overdue incomplete tasks.
     */
    fun countOverdue(tasks: List<Task>): Int {
        return tasks.count {
            calculateDaysRemaining(it.deadline) < 0 && !it.completed
        }
    }

    /**
     * Counts tasks due today.
     *
     * @param tasks User task list.
     * @return Number of incomplete tasks due today.
     */
    fun countDueToday(tasks: List<Task>): Int {
        return tasks.count {
            calculateDaysRemaining(it.deadline) == 0L && !it.completed
        }
    }

    /**
     * Counts tasks due within the next seven days.
     *
     * @param tasks User task list.
     * @return Number of incomplete tasks due this week.
     */
    fun countThisWeek(tasks: List<Task>): Int {
        return tasks.count {
            val days = calculateDaysRemaining(it.deadline)
            days in 0..7 && !it.completed
        }
    }
}