package au.edu.cqu.smartacademia.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import au.edu.cqu.smartacademia.R
import au.edu.cqu.smartacademia.activities.LoginActivity
import au.edu.cqu.smartacademia.database.CourseUnit
import au.edu.cqu.smartacademia.database.Task
import au.edu.cqu.smartacademia.utils.ReminderScheduler
import au.edu.cqu.smartacademia.utils.ScheduleGenerator
import au.edu.cqu.smartacademia.viewmodel.TaskViewModel
import au.edu.cqu.smartacademia.viewmodel.UnitViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Dashboard fragment displayed as the home screen of SmartAcademia.
 *
 * Shows a personalised greeting, current date, task summary,
 * academic progress, study plan, upcoming deadlines,
 * reminder control, profile shortcut and logout option.
 */
class DashboardFragment : Fragment() {

    private lateinit var taskViewModel: TaskViewModel
    private lateinit var unitViewModel: UnitViewModel

    private var userEmail: String = ""

    private lateinit var greetingTextView: TextView
    private lateinit var dateTimeTextView: TextView
    private lateinit var overdueCardTextView: TextView
    private lateinit var dueTodayCardTextView: TextView
    private lateinit var thisWeekCardTextView: TextView
    private lateinit var completedCardTextView: TextView
    private lateinit var unitSummaryTextView: TextView
    private lateinit var studyPlanTextView: TextView
    private lateinit var upcomingDeadlinesTextView: TextView

    private var latestTasks: List<Task> = emptyList()
    private var latestUnits: List<CourseUnit> = emptyList()

    /**
     * Creates and initialises the dashboard screen.
     *
     * @param inflater Layout inflater.
     * @param container Parent view container.
     * @param savedInstanceState Previous fragment state.
     * @return Dashboard view.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view =
            inflater.inflate(R.layout.fragment_dashboard, container, false)

        greetingTextView =
            view.findViewById(R.id.greetingTextView)

        dateTimeTextView =
            view.findViewById(R.id.dateTimeTextView)

        overdueCardTextView =
            view.findViewById(R.id.overdueCardTextView)

        dueTodayCardTextView =
            view.findViewById(R.id.dueTodayCardTextView)

        thisWeekCardTextView =
            view.findViewById(R.id.thisWeekCardTextView)

        completedCardTextView =
            view.findViewById(R.id.completedCardTextView)

        unitSummaryTextView =
            view.findViewById(R.id.unitSummaryTextView)

        studyPlanTextView =
            view.findViewById(R.id.studyPlanTextView)

        upcomingDeadlinesTextView =
            view.findViewById(R.id.upcomingDeadlinesTextView)

        val profileButton =
            view.findViewById<Button>(R.id.profileButton)

        val logoutButton =
            view.findViewById<Button>(R.id.logoutButton)

        val sharedPreferences =
            requireActivity().getSharedPreferences(
                "smartacademia_session",
                Context.MODE_PRIVATE
            )

        userEmail =
            sharedPreferences.getString("email", "") ?: ""

        updateGreetingAndDate()

        taskViewModel =
            ViewModelProvider(this)[TaskViewModel::class.java]

        unitViewModel =
            ViewModelProvider(this)[UnitViewModel::class.java]

        observeTasks()
        observeUnits()

        profileButton.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Profile screen coming next",
                Toast.LENGTH_SHORT
            ).show()
        }

        logoutButton.setOnClickListener {
            confirmLogout()
        }

        return view
    }

    /**
     * Observes unit-linked tasks for the logged-in user.
     */
    private fun observeTasks() {
        taskViewModel.getTasksLinkedToUnits(userEmail)
            .observe(viewLifecycleOwner) { tasks ->

                latestTasks =
                    tasks

                updateDashboard()
                updateAcademicProgress()
                enableAutomaticReminders()
            }
    }

    /**
     * Observes all units for the logged-in user.
     */
    private fun observeUnits() {
        unitViewModel.getUnitsForUser(userEmail)
            .observe(viewLifecycleOwner) { units ->

                latestUnits =
                    units

                updateDashboard()
                updateAcademicProgress()
            }
    }

    /**
     * Updates the greeting and current date/time.
     */
    private fun updateGreetingAndDate() {
        val calendar =
            Calendar.getInstance()

        val hour =
            calendar.get(Calendar.HOUR_OF_DAY)

        val greeting =
            when {
                hour < 12 -> getString(R.string.good_morning)
                hour < 17 -> getString(R.string.good_afternoon)
                else -> getString(R.string.good_evening)
            }

        val sharedPreferences =
            requireActivity().getSharedPreferences(
                "smartacademia_session",
                Context.MODE_PRIVATE
            )

        val fullName =
            sharedPreferences.getString(
                "full_name",
                getString(R.string.default_student)
            ) ?: getString(R.string.default_student)

        val firstName =
            fullName.trim()
                .split(" ")
                .firstOrNull()
                ?: getString(R.string.default_student)

        greetingTextView.text =
            getString(
                R.string.greeting_format,
                greeting,
                firstName
            )

        val dateFormat =
            SimpleDateFormat(
                "EEEE, d MMMM yyyy\nhh:mm a",
                Locale.getDefault()
            )

        dateTimeTextView.text =
            dateFormat.format(calendar.time)
    }

    /**
     * Returns only tasks belonging to currently added units.
     *
     * This prevents old demo tasks or unlinked tasks from appearing
     * on the Home dashboard.
     *
     * @return List of tasks linked to existing units.
     */
    private fun getDashboardTasks(): List<Task> {
        if (latestUnits.isEmpty()) {
            return emptyList()
        }

        return latestTasks.filter { task ->
            latestUnits.any { unit ->
                task.unitId == unit.id ||
                        task.course.equals(
                            unit.unitCode,
                            ignoreCase = true
                        )
            }
        }
    }

    /**
     * Updates task summary cards, today's study plan and upcoming deadlines.
     */
    private fun updateDashboard() {
        val dashboardTasks =
            getDashboardTasks()

        val overdue =
            ScheduleGenerator.countOverdue(dashboardTasks)

        val dueToday =
            ScheduleGenerator.countDueToday(dashboardTasks)

        val thisWeek =
            ScheduleGenerator.countThisWeek(dashboardTasks)

        val completed =
            dashboardTasks.count { it.completed }

        overdueCardTextView.text =
            "$overdue\n${getString(R.string.overdue_card)}"

        dueTodayCardTextView.text =
            "$dueToday\n${getString(R.string.due_today_card)}"

        thisWeekCardTextView.text =
            "$thisWeek\n${getString(R.string.this_week_card)}"

        completedCardTextView.text =
            "$completed\n${getString(R.string.completed_card)}"

        val activeTasks =
            ScheduleGenerator.sortBySmartPriority(dashboardTasks)
                .take(3)

        studyPlanTextView.text =
            if (activeTasks.isEmpty()) {
                getString(R.string.no_active_tasks)
            } else {
                activeTasks.mapIndexed { index, task ->
                    "${index + 1}. ${task.title} (${task.estimatedHours} hrs)"
                }.joinToString("\n")
            }

        val upcomingTasks =
            dashboardTasks.filter {
                !it.completed &&
                        ScheduleGenerator.calculateDaysRemaining(
                            it.deadline
                        ) >= 0
            }
                .sortedBy {
                    ScheduleGenerator.calculateDaysRemaining(
                        it.deadline
                    )
                }
                .take(3)

        upcomingDeadlinesTextView.text =
            if (upcomingTasks.isEmpty()) {
                getString(R.string.no_upcoming_deadlines)
            } else {
                upcomingTasks.joinToString("\n\n") { task ->

                    val days =
                        ScheduleGenerator.calculateDaysRemaining(
                            task.deadline
                        )

                    val dueText =
                        when (days) {
                            0L -> getString(R.string.due_today)
                            1L -> getString(R.string.due_tomorrow)
                            else -> getString(
                                R.string.due_in_days,
                                days.toInt()
                            )
                        }

                    "• ${task.title}\n  ${task.course} - $dueText"
                }
            }
    }

    /**
     * Updates academic progress using current unit and assignment data.
     */
    private fun updateAcademicProgress() {
        if (latestUnits.isEmpty()) {
            unitSummaryTextView.text =
                "No units added yet.\nGo to Tasks and add your first unit."
            return
        }

        val dashboardTasks =
            getDashboardTasks()

        val summary =
            latestUnits.take(3)
                .joinToString("\n\n") { unit ->

                    val unitTasks =
                        dashboardTasks.filter {
                            it.unitId == unit.id ||
                                    it.course.equals(
                                        unit.unitCode,
                                        ignoreCase = true
                                    )
                        }

                    val completedWeight =
                        unitTasks
                            .filter { it.completed }
                            .sumOf { it.weight }

                    val pendingWeight =
                        unitTasks
                            .filter { !it.completed }
                            .sumOf { it.weight }

                    val totalTrackedWeight =
                        completedWeight + pendingWeight

                    val status =
                        when {
                            completedWeight >= unit.passMark ->
                                "Passing"

                            totalTrackedWeight < unit.passMark ->
                                "At Risk"

                            else ->
                                "Need ${(unit.passMark - completedWeight).coerceAtLeast(0)}% more"
                        }

                    "${unit.unitCode} - $status\n" +
                            "Completed: $completedWeight% · Pending: $pendingWeight%\n" +
                            "Pass Mark: ${unit.passMark}%"
                }

        unitSummaryTextView.text =
            "Units: ${latestUnits.size}\n\n$summary"
    }

    /**
     * Enables automatic reminders for due and upcoming tasks.
     */
    private fun enableAutomaticReminders() {
        val dueAndUpcomingTasks =
            ScheduleGenerator.sortBySmartPriority(
                getDashboardTasks().filter {
                    ScheduleGenerator.calculateDaysRemaining(
                        it.deadline
                    ) <= 7
                }
            )

        val reminderMessage =
            if (dueAndUpcomingTasks.isEmpty()) {
                getString(R.string.no_due_assignments)
            } else {
                dueAndUpcomingTasks.take(5)
                    .joinToString("\n") { task ->

                        val days =
                            ScheduleGenerator.calculateDaysRemaining(
                                task.deadline
                            )

                        val dueText =
                            when {
                                days < 0 ->
                                    getString(R.string.overdue_status)

                                days == 0L ->
                                    getString(R.string.due_today)

                                days == 1L ->
                                    getString(R.string.due_tomorrow)

                                else ->
                                    getString(
                                        R.string.due_in_days,
                                        days.toInt()
                                    )
                            }

                        "${task.title} - $dueText"
                    }
            }

        ReminderScheduler.startReminder(
            requireContext(),
            reminderMessage
        )

        Toast.makeText(
            requireContext(),
            getString(R.string.auto_reminder_enabled),
            Toast.LENGTH_LONG
        ).show()
    }

    /**
     * Shows logout confirmation and clears the current session.
     */
    private fun confirmLogout() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.logout_title))
            .setMessage(getString(R.string.logout_message))
            .setPositiveButton(
                getString(R.string.logout_button)
            ) { _, _ ->

                requireActivity()
                    .getSharedPreferences(
                        "smartacademia_session",
                        Context.MODE_PRIVATE
                    )
                    .edit()
                    .clear()
                    .apply()

                val intent =
                    Intent(
                        requireContext(),
                        LoginActivity::class.java
                    )

                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)
            }
            .setNegativeButton(
                getString(R.string.cancel_button),
                null
            )
            .show()
    }
}