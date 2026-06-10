package au.edu.cqu.smartacademia.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.cqu.smartacademia.R
import au.edu.cqu.smartacademia.adapter.TaskAdapter
import au.edu.cqu.smartacademia.database.Task
import au.edu.cqu.smartacademia.utils.ScheduleGenerator
import au.edu.cqu.smartacademia.viewmodel.TaskViewModel

/**
 * Activity that displays details for one selected university unit.
 *
 * Shows all assignments linked to the unit, grade progress,
 * pass/fail status and allows the student to add new assignments
 * directly inside the selected unit.
 */
class UnitDetailActivity : AppCompatActivity() {

    private lateinit var taskViewModel: TaskViewModel
    private lateinit var taskAdapter: TaskAdapter

    private lateinit var unitTitleTextView: TextView
    private lateinit var unitProgressTextView: TextView
    private lateinit var unitStatusTextView: TextView
    private lateinit var addAssignmentButton: Button
    private lateinit var backButton: Button
    private lateinit var assignmentRecyclerView: RecyclerView

    private var userEmail: String = ""
    private var unitId: String = ""
    private var unitCode: String = ""
    private var unitName: String = ""
    private var passMark: Int = 50

    /**
     * Creates the unit detail screen.
     *
     * Receives unit information from UnitDashboardFragment,
     * loads assignments linked to this unit and updates
     * grade progress automatically through LiveData.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unit_detail)

        userEmail =
            getSharedPreferences("smartacademia_session", Context.MODE_PRIVATE)
                .getString("email", "") ?: ""

        unitId =
            intent.getStringExtra("unit_id") ?: ""

        unitCode =
            intent.getStringExtra("unit_code") ?: ""

        unitName =
            intent.getStringExtra("unit_name") ?: ""

        passMark =
            intent.getIntExtra("pass_mark", 50)

        unitTitleTextView =
            findViewById(R.id.unitDetailTitleTextView)

        unitProgressTextView =
            findViewById(R.id.unitProgressTextView)

        unitStatusTextView =
            findViewById(R.id.unitPassStatusTextView)

        addAssignmentButton =
            findViewById(R.id.addAssignmentButton)

        backButton =
            findViewById(R.id.backFromUnitButton)

        assignmentRecyclerView =
            findViewById(R.id.unitAssignmentRecyclerView)

        unitTitleTextView.text =
            "$unitCode\n$unitName"

        taskViewModel =
            ViewModelProvider(this)[TaskViewModel::class.java]

        taskAdapter =
            TaskAdapter(
                emptyList(),
                onDeleteClick = { task ->
                    taskViewModel.deleteTaskById(task.id)
                },
                onCompleteClick = { task ->
                    task.completed = true
                    taskViewModel.updateTask(task)
                }
            )

        assignmentRecyclerView.layoutManager =
            LinearLayoutManager(this)

        assignmentRecyclerView.adapter =
            taskAdapter

        addAssignmentButton.setOnClickListener {
            val intent =
                Intent(this, AddTaskActivity::class.java)

            intent.putExtra("unit_id", unitId)
            intent.putExtra("unit_code", unitCode)

            startActivity(intent)
        }

        backButton.setOnClickListener {
            finish()
        }

        observeUnitTasks()
    }

    /**
     * Observes assignments linked to this unit.
     *
     * The task list and grade progress update automatically
     * when assignments are added, edited or completed.
     */
    private fun observeUnitTasks() {
        taskViewModel.getTasksForUnit(
            userEmail,
            unitId,
            unitCode
        )
            .observe(this) { tasks ->
                val sortedTasks =
                    ScheduleGenerator.sortBySmartPriority(tasks)

                taskAdapter.updateTasks(sortedTasks)
                updateGradeProgress(tasks)
            }
    }

    /**
     * Calculates secured weight, pending weight and pass status.
     *
     * @param tasks Assignments linked to the selected unit.
     */
    private fun updateGradeProgress(tasks: List<Task>) {
        val securedWeight =
            tasks.filter { it.completed }
                .sumOf { it.weight }

        val pendingWeight =
            tasks.filter { !it.completed }
                .sumOf { it.weight }

        val needed =
            (passMark - securedWeight).coerceAtLeast(0)

        unitProgressTextView.text =
            "Secured: $securedWeight% · Pending: $pendingWeight% · Pass Mark: $passMark%"

        unitStatusTextView.text =
            when {
                securedWeight >= passMark ->
                    "Status: Passing"

                securedWeight + pendingWeight < passMark ->
                    "Status: Cannot reach pass mark"

                else ->
                    "Status: Need $needed% more to pass"
            }
    }
}