package au.edu.cqu.smartacademia.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import au.edu.cqu.smartacademia.R
import au.edu.cqu.smartacademia.activities.LoginActivity
import au.edu.cqu.smartacademia.database.Task
import au.edu.cqu.smartacademia.utils.ScheduleGenerator
import au.edu.cqu.smartacademia.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DashboardFragment : Fragment() {

    private lateinit var taskViewModel: TaskViewModel
    private var userEmail: String = ""

    private lateinit var greetingTextView: TextView
    private lateinit var dateTimeTextView: TextView
    private lateinit var overdueCardTextView: TextView
    private lateinit var dueTodayCardTextView: TextView
    private lateinit var thisWeekCardTextView: TextView
    private lateinit var completedCardTextView: TextView
    private lateinit var studyPlanTextView: TextView
    private lateinit var upcomingDeadlinesTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        greetingTextView = view.findViewById(R.id.greetingTextView)
        dateTimeTextView = view.findViewById(R.id.dateTimeTextView)
        overdueCardTextView = view.findViewById(R.id.overdueCardTextView)
        dueTodayCardTextView = view.findViewById(R.id.dueTodayCardTextView)
        thisWeekCardTextView = view.findViewById(R.id.thisWeekCardTextView)
        completedCardTextView = view.findViewById(R.id.completedCardTextView)
        studyPlanTextView = view.findViewById(R.id.studyPlanTextView)
        upcomingDeadlinesTextView = view.findViewById(R.id.upcomingDeadlinesTextView)

        val logoutButton = view.findViewById<Button>(R.id.logoutButton)

        val sharedPreferences = requireActivity()
            .getSharedPreferences("smartacademia_session", Context.MODE_PRIVATE)

        userEmail = sharedPreferences.getString("email", "") ?: ""

        updateGreetingAndDate()

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        taskViewModel.getTasksForUser(userEmail).observe(viewLifecycleOwner) { tasks ->
            updateDashboard(tasks)
        }

        logoutButton.setOnClickListener {
            confirmLogout()
        }

        return view
    }

    private fun updateGreetingAndDate() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val greeting = when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }

        greetingTextView.text = "$greeting,👋"

        val dateFormat = SimpleDateFormat(
            "EEEE, d MMMM yyyy\nhh:mm a",
            Locale.getDefault()
        )

        dateTimeTextView.text = dateFormat.format(calendar.time)
    }

    private fun updateDashboard(tasks: List<Task>) {
        val overdue = ScheduleGenerator.countOverdue(tasks)
        val dueToday = ScheduleGenerator.countDueToday(tasks)
        val thisWeek = ScheduleGenerator.countThisWeek(tasks)
        val completed = tasks.count { it.completed }

        overdueCardTextView.text = "$overdue\nOverdue"
        dueTodayCardTextView.text = "$dueToday\nDue Today"
        thisWeekCardTextView.text = "$thisWeek\nThis Week"
        completedCardTextView.text = "$completed\nCompleted"

        val activeTasks = tasks
            .filter { !it.completed }
            .sortedByDescending {
                ScheduleGenerator.calculatePriorityScore(it)
            }
            .take(3)

        studyPlanTextView.text =
            if (activeTasks.isEmpty()) {
                "No active tasks today.\nTap Add Task to create a new study plan."
            } else {
                activeTasks.mapIndexed { index, task ->
                    "${index + 1}. ${task.title} (${task.estimatedHours} hrs)"
                }.joinToString("\n")
            }

        val upcomingTasks = tasks
            .filter {
                !it.completed &&
                        ScheduleGenerator.calculateDaysRemaining(it.deadline) >= 0
            }
            .sortedBy {
                ScheduleGenerator.calculateDaysRemaining(it.deadline)
            }
            .take(3)

        upcomingDeadlinesTextView.text =
            if (upcomingTasks.isEmpty()) {
                "No upcoming deadlines."
            } else {
                upcomingTasks.joinToString("\n\n") { task ->
                    val days = ScheduleGenerator.calculateDaysRemaining(task.deadline)

                    val dueText = when (days) {
                        0L -> "Due today"
                        1L -> "Due tomorrow"
                        else -> "Due in $days days"
                    }

                    "• ${task.title}\n  ${task.course} - $dueText"
                }
            }
    }

    private fun confirmLogout() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                requireActivity()
                    .getSharedPreferences("smartacademia_session", Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply()

                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}