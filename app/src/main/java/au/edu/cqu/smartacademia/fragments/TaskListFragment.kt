package au.edu.cqu.smartacademia.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.cqu.smartacademia.R
import au.edu.cqu.smartacademia.activities.AddTaskActivity
import au.edu.cqu.smartacademia.adapter.TaskAdapter
import au.edu.cqu.smartacademia.database.Task
import au.edu.cqu.smartacademia.utils.ScheduleGenerator
import au.edu.cqu.smartacademia.viewmodel.TaskViewModel

class TaskListFragment : Fragment() {

    private lateinit var taskViewModel: TaskViewModel
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var sortSpinner: Spinner
    private var allTasks: List<Task> = emptyList()
    private var userEmail: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_task_list, container, false)

        val sharedPreferences = requireActivity()
            .getSharedPreferences("smartacademia_session", android.content.Context.MODE_PRIVATE)

        userEmail = sharedPreferences.getString("email", "") ?: ""

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        val addTaskButton = view.findViewById<Button>(R.id.addTaskButton)
        val taskRecyclerView = view.findViewById<RecyclerView>(R.id.taskRecyclerView)
        sortSpinner = view.findViewById(R.id.sortSpinner)

        setupSortSpinner()

        taskAdapter = TaskAdapter(emptyList()) { task ->
            taskViewModel.deleteTaskById(task.id)
        }

        taskRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        taskRecyclerView.adapter = taskAdapter

        addTaskButton.setOnClickListener {
            startActivity(Intent(requireContext(), AddTaskActivity::class.java))
        }

        taskViewModel.loadSeedData(userEmail)

        taskViewModel.getTasksForUser(userEmail).observe(viewLifecycleOwner) { tasks ->
            allTasks = tasks
            applySorting()
        }

        return view
    }

    private fun setupSortSpinner() {
        val sortOptions = listOf("Priority", "Deadline", "Course", "Weight")

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            sortOptions
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = adapter

        sortSpinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                applySorting()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })
    }

    private fun applySorting() {
        val selectedSort = sortSpinner.selectedItem?.toString() ?: "Priority"

        val sortedTasks = when (selectedSort) {
            "Deadline" -> allTasks.sortedBy { it.deadline }
            "Course" -> allTasks.sortedBy { it.course }
            "Weight" -> allTasks.sortedByDescending { it.weight }
            else -> allTasks.sortedByDescending {
                ScheduleGenerator.calculatePriorityScore(it)
            }
        }

        taskAdapter.updateTasks(sortedTasks)
    }
}