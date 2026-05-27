package au.edu.cqu.smartacademia.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import au.edu.cqu.smartacademia.R
import au.edu.cqu.smartacademia.activities.AddTaskActivity
import au.edu.cqu.smartacademia.database.Task
import au.edu.cqu.smartacademia.utils.NotificationHelper
import au.edu.cqu.smartacademia.utils.ScheduleGenerator
import au.edu.cqu.smartacademia.viewmodel.TaskViewModel

class DashboardFragment : Fragment() {

    private lateinit var taskViewModel: TaskViewModel
    private var userEmail: String = ""
    private var currentTasks: List<Task> = emptyList()

    private val notificationPermissionRequestCode = 2001

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        val overdueCountTextView = view.findViewById<TextView>(R.id.overdueCountTextView)
        val dueTodayCountTextView = view.findViewById<TextView>(R.id.dueTodayCountTextView)
        val weekCountTextView = view.findViewById<TextView>(R.id.weekCountTextView)
        val todayPlanTextView = view.findViewById<TextView>(R.id.todayPlanTextView)
        val addTaskButton = view.findViewById<Button>(R.id.addTaskButton)
        val checkRemindersButton = view.findViewById<Button>(R.id.checkRemindersButton)

        NotificationHelper.createNotificationChannel(requireContext())
        requestNotificationPermissionIfNeeded()

        val sharedPreferences = requireActivity()
            .getSharedPreferences("smartacademia_session", Context.MODE_PRIVATE)

        userEmail = sharedPreferences.getString("email", "") ?: ""

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]
        taskViewModel.loadSeedData(userEmail)

        taskViewModel.getTasksForUser(userEmail).observe(viewLifecycleOwner) { tasks ->
            currentTasks = tasks

            overdueCountTextView.text = "${ScheduleGenerator.countOverdue(tasks)}\nOverdue"
            dueTodayCountTextView.text = "${ScheduleGenerator.countDueToday(tasks)}\nDue Today"
            weekCountTextView.text = "${ScheduleGenerator.countThisWeek(tasks)}\nThis Week"

            val sortedTasks = tasks.sortedByDescending {
                ScheduleGenerator.calculatePriorityScore(it)
            }

            todayPlanTextView.text = ScheduleGenerator.generateTodayPlan(sortedTasks)
        }

        addTaskButton.setOnClickListener {
            startActivity(Intent(requireContext(), AddTaskActivity::class.java))
        }

        checkRemindersButton.setOnClickListener {
            checkTaskReminders()
        }

        return view
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    notificationPermissionRequestCode
                )
            }
        }
    }

    private fun checkTaskReminders() {
        if (currentTasks.isEmpty()) {
            Toast.makeText(requireContext(), "No tasks available", Toast.LENGTH_SHORT).show()
            return
        }

        currentTasks.forEachIndexed { index, task ->
            if (task.completed) return@forEachIndexed

            val daysRemaining = ScheduleGenerator.calculateDaysRemaining(task.deadline)

            when {
                daysRemaining < 0 -> {
                    NotificationHelper.showTaskNotification(
                        requireContext(),
                        "Overdue Task",
                        "${task.title} is overdue. Deadline was ${task.deadline}.",
                        200 + index
                    )
                }

                daysRemaining == 0L -> {
                    NotificationHelper.showTaskNotification(
                        requireContext(),
                        "Task Due Today",
                        "${task.title} is due today at ${task.deadline}.",
                        300 + index
                    )
                }

                daysRemaining == 1L -> {
                    NotificationHelper.showTaskNotification(
                        requireContext(),
                        "Task Due Tomorrow",
                        "${task.title} is due tomorrow at ${task.deadline}.",
                        400 + index
                    )
                }
            }
        }

        Toast.makeText(requireContext(), "Task reminders checked", Toast.LENGTH_SHORT).show()
    }
}