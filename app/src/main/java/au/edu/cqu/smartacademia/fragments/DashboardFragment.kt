package au.edu.cqu.smartacademia.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import au.edu.cqu.smartacademia.R
import au.edu.cqu.smartacademia.activities.AddTaskActivity
import au.edu.cqu.smartacademia.viewmodel.TaskViewModel

class DashboardFragment : Fragment() {

    private lateinit var taskViewModel: TaskViewModel
    private var userEmail: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        val todayPlanTextView = view.findViewById<TextView>(R.id.todayPlanTextView)
        val addTaskButton = view.findViewById<Button>(R.id.addTaskButton)

        val sharedPreferences = requireActivity()
            .getSharedPreferences("smartacademia_session", Context.MODE_PRIVATE)

        userEmail = sharedPreferences.getString("email", "") ?: ""

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]
        taskViewModel.loadSeedData(userEmail)

        taskViewModel.getTasksForUser(userEmail).observe(viewLifecycleOwner) { tasks ->
            if (tasks.isEmpty()) {
                todayPlanTextView.text = getString(R.string.no_tasks)
            } else {
                val topTasks = tasks.take(3).joinToString("\n") {
                    "${it.title} - ${it.course}"
                }
                todayPlanTextView.text = topTasks
            }
        }

        addTaskButton.setOnClickListener {
            startActivity(Intent(requireContext(), AddTaskActivity::class.java))
        }

        return view
    }
}