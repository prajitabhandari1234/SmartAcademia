package au.edu.cqu.smartacademia.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
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
    private lateinit var sortedByTextView: TextView

    private var allTasks: List<Task> = emptyList()
    private var userEmail: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_task_list, container, false)

        userEmail = requireActivity()
            .getSharedPreferences("smartacademia_session", android.content.Context.MODE_PRIVATE)
            .getString("email", "") ?: ""

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        val addTaskButton = view.findViewById<Button>(R.id.addTaskButton)
        val fetchTasksButton = view.findViewById<Button>(R.id.fetchTasksButton)
        val taskRecyclerView = view.findViewById<RecyclerView>(R.id.taskRecyclerView)

        sortSpinner = view.findViewById(R.id.sortSpinner)
        sortedByTextView = view.findViewById(R.id.sortedByTextView)

        setupSortSpinner()

        taskAdapter = TaskAdapter(
            emptyList(),
            onDeleteClick = { task ->
                taskViewModel.deleteTaskById(task.id)
            },
            onCompleteClick = { task ->
                task.completed = true
                taskViewModel.updateTask(task)
                Toast.makeText(requireContext(), "Task marked as completed", Toast.LENGTH_SHORT).show()
            }
        )

        taskRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        taskRecyclerView.adapter = taskAdapter

        addTaskButton.setOnClickListener {
            startActivity(Intent(requireContext(), AddTaskActivity::class.java))
        }

        fetchTasksButton.setOnClickListener {
            taskViewModel.fetchTasksFromApi(userEmail)
            Toast.makeText(requireContext(), "Fetching remote tasks...", Toast.LENGTH_SHORT).show()
        }

        taskViewModel.loadSeedData(userEmail)

        taskViewModel.getTasksForUser(userEmail).observe(viewLifecycleOwner) { tasks ->
            allTasks = tasks
            applySorting()
        }

        return view
    }

    private fun setupSortSpinner() {
        val sortOptions = listOf(
            "Active Tasks",
            "Completed Tasks",
            "All Tasks",
            "Priority",
            "Deadline",
            "Course",
            "Weight"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            sortOptions
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = adapter

        sortSpinner.setOnItemSelectedListener(
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    applySorting()
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }
        )
    }

    private fun applySorting() {
        val selectedSort = sortSpinner.selectedItem?.toString() ?: "Active Tasks"

        sortedByTextView.text = when (selectedSort) {
            "Active Tasks" -> "Showing Active Tasks"
            "Completed Tasks" -> "Showing Completed Tasks"
            "All Tasks" -> "Showing All Tasks"
            "Priority" -> "Sorted by Priority"
            "Deadline" -> "Sorted by Deadline"
            "Course" -> "Sorted by Course"
            "Weight" -> "Sorted by Weight"
            else -> "Showing Active Tasks"
        }

        val filteredTasks = when (selectedSort) {
            "Completed Tasks" -> allTasks.filter { it.completed }
            "All Tasks" -> allTasks
            else -> allTasks.filter { !it.completed }
        }

        val sortedTasks = when (selectedSort) {
            "Deadline" -> filteredTasks.sortedBy { it.deadline }
            "Course" -> filteredTasks.sortedBy { it.course }
            "Weight" -> filteredTasks.sortedByDescending { it.weight }
            "Priority" -> filteredTasks.sortedByDescending {
                ScheduleGenerator.calculatePriorityScore(it)
            }
            else -> filteredTasks.sortedByDescending {
                ScheduleGenerator.calculatePriorityScore(it)
            }
        }

        taskAdapter.updateTasks(sortedTasks)
    }
}