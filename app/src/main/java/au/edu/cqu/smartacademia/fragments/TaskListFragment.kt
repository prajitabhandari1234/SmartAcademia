package au.edu.cqu.smartacademia.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
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

/**
 * Fragment responsible for displaying and managing academic tasks.
 *
 * Features:
 * - Displays tasks using RecyclerView.
 * - Supports sorting and filtering.
 * - Allows task creation, editing and deletion.
 * - Supports task completion tracking.
 * - Retrieves remote tasks through HTTP.
 * - Uses ViewModel and LiveData to keep the UI updated.
 * - Applies urgency-first smart priority sorting.
 */
class TaskListFragment : Fragment() {

    private lateinit var taskViewModel: TaskViewModel
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var sortSpinner: Spinner
    private lateinit var sortedByTextView: TextView

    private var allTasks: List<Task> = emptyList()
    private var userEmail: String = ""

    /**
     * Creates and initialises the task list screen.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            inflater.inflate(R.layout.fragment_task_list, container, false)

        userEmail = requireActivity()
            .getSharedPreferences("smartacademia_session", Context.MODE_PRIVATE)
            .getString("email", "") ?: ""

        taskViewModel =
            ViewModelProvider(this)[TaskViewModel::class.java]

        val addTaskButton =
            view.findViewById<Button>(R.id.addTaskButton)

        val fetchTasksButton =
            view.findViewById<Button>(R.id.fetchTasksButton)

        val taskRecyclerView =
            view.findViewById<RecyclerView>(R.id.taskRecyclerView)

        sortSpinner =
            view.findViewById(R.id.sortSpinner)

        sortedByTextView =
            view.findViewById(R.id.sortedByTextView)

        setupSortSpinner()

        taskAdapter = TaskAdapter(
            emptyList(),
            onDeleteClick = { task ->
                taskViewModel.deleteTaskById(task.id)
            },
            onCompleteClick = { task ->
                task.completed = true
                taskViewModel.updateTask(task)

                Toast.makeText(
                    requireContext(),
                    getString(R.string.task_completed_message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        taskRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        taskRecyclerView.adapter =
            taskAdapter

        addTaskButton.setOnClickListener {
            startActivity(
                Intent(requireContext(), AddTaskActivity::class.java)
            )
        }

        fetchTasksButton.setOnClickListener {
            taskViewModel.fetchTasksFromApi(userEmail)

            Toast.makeText(
                requireContext(),
                getString(R.string.fetching_tasks_message),
                Toast.LENGTH_SHORT
            ).show()
        }

        taskViewModel.loadSeedData(userEmail)
        taskViewModel.fetchTasksFromApi(userEmail)

        taskViewModel.getTasksForUser(userEmail)
            .observe(viewLifecycleOwner) { tasks ->
                allTasks = tasks
                applySorting()
            }

        return view
    }

    /**
     * Configures sorting and filtering options.
     */
    private fun setupSortSpinner() {
        val sortOptions = listOf(
            getString(R.string.active_tasks),
            getString(R.string.completed_tasks),
            getString(R.string.all_tasks),
            getString(R.string.priority),
            getString(R.string.deadline),
            getString(R.string.course),
            getString(R.string.weight)
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            sortOptions
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        sortSpinner.adapter =
            adapter

        sortSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    applySorting()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // No action required.
                }
            }
    }

    /**
     * Applies filtering and sorting based on the selected dropdown option.
     */
    private fun applySorting() {
        val selectedSort =
            sortSpinner.selectedItem?.toString()
                ?: getString(R.string.active_tasks)

        sortedByTextView.text =
            when (selectedSort) {
                getString(R.string.active_tasks) ->
                    getString(R.string.showing_active_tasks)

                getString(R.string.completed_tasks) ->
                    getString(R.string.showing_completed_tasks)

                getString(R.string.all_tasks) ->
                    getString(R.string.showing_all_tasks)

                getString(R.string.priority) ->
                    getString(R.string.sorted_by_priority)

                getString(R.string.deadline) ->
                    getString(R.string.sorted_by_deadline)

                getString(R.string.course) ->
                    getString(R.string.sorted_by_course)

                getString(R.string.weight) ->
                    getString(R.string.sorted_by_weight)

                else ->
                    getString(R.string.showing_active_tasks)
            }

        val sortedTasks =
            when (selectedSort) {

                getString(R.string.active_tasks) -> {
                    ScheduleGenerator.sortBySmartPriority(
                        allTasks.filter { !it.completed }
                    )
                }

                getString(R.string.completed_tasks) -> {
                    allTasks
                        .filter { it.completed }
                        .sortedBy {
                            ScheduleGenerator.calculateDaysRemaining(
                                it.deadline
                            )
                        }
                }

                getString(R.string.all_tasks) -> {
                    allTasks.sortedWith(
                        compareBy<Task> { it.completed }
                            .thenBy {
                                ScheduleGenerator.getUrgencyTier(it)
                            }
                            .thenByDescending {
                                ScheduleGenerator.calculatePriorityScore(it)
                            }
                    )
                }

                getString(R.string.priority) -> {
                    ScheduleGenerator.sortBySmartPriority(
                        allTasks.filter { !it.completed }
                    )
                }

                getString(R.string.deadline) -> {
                    allTasks
                        .filter { !it.completed }
                        .sortedBy {
                            ScheduleGenerator.calculateDaysRemaining(
                                it.deadline
                            )
                        }
                }

                getString(R.string.course) -> {
                    allTasks
                        .filter { !it.completed }
                        .sortedBy {
                            it.course
                        }
                }

                getString(R.string.weight) -> {
                    allTasks
                        .filter { !it.completed }
                        .sortedByDescending {
                            it.weight
                        }
                }

                else -> {
                    ScheduleGenerator.sortBySmartPriority(
                        allTasks.filter { !it.completed }
                    )
                }
            }

        taskAdapter.updateTasks(sortedTasks)
    }
}