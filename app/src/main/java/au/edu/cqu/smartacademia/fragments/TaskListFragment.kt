package au.edu.cqu.smartacademia.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.cqu.smartacademia.R
import au.edu.cqu.smartacademia.activities.AddTaskActivity
import au.edu.cqu.smartacademia.adapter.TaskAdapter
import au.edu.cqu.smartacademia.viewmodel.TaskViewModel

class TaskListFragment : Fragment() {

    private lateinit var taskViewModel: TaskViewModel
    private lateinit var taskAdapter: TaskAdapter
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

        val taskRecyclerView = view.findViewById<RecyclerView>(R.id.taskRecyclerView)
        taskAdapter = TaskAdapter(emptyList())

        taskRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        taskRecyclerView.adapter = taskAdapter

        taskViewModel.loadSeedData(userEmail)

        taskViewModel.getTasksForUser(userEmail).observe(viewLifecycleOwner) { tasks ->
            taskAdapter.updateTasks(tasks)
        }
        val addTaskButton = view.findViewById<Button>(R.id.addTaskButton)
        addTaskButton.setOnClickListener {
            startActivity(Intent(requireContext(), AddTaskActivity::class.java))
        }

        return view
    }
}