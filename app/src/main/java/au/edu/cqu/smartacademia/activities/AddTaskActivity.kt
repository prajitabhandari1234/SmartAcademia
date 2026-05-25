package au.edu.cqu.smartacademia.activities

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import au.edu.cqu.smartacademia.R
import au.edu.cqu.smartacademia.database.Task
import au.edu.cqu.smartacademia.utils.ScheduleGenerator
import au.edu.cqu.smartacademia.viewmodel.TaskViewModel

class AddTaskActivity : AppCompatActivity() {

    private lateinit var taskViewModel: TaskViewModel
    private var userEmail: String = ""
    private var editingTaskId: String? = null

    private lateinit var titleEditText: EditText
    private lateinit var courseEditText: EditText
    private lateinit var deadlineEditText: EditText
    private lateinit var weightEditText: EditText
    private lateinit var hoursEditText: EditText
    private lateinit var notesEditText: EditText
    private lateinit var latEditText: EditText
    private lateinit var lonEditText: EditText
    private lateinit var saveTaskButton: Button
    private lateinit var deleteTaskButton: Button
    private lateinit var cancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        userEmail = getSharedPreferences("smartacademia_session", MODE_PRIVATE)
            .getString("email", "") ?: ""

        editingTaskId = intent.getStringExtra("task_id")

        titleEditText = findViewById(R.id.taskTitleEditText)
        courseEditText = findViewById(R.id.courseEditText)
        deadlineEditText = findViewById(R.id.deadlineEditText)
        weightEditText = findViewById(R.id.weightEditText)
        hoursEditText = findViewById(R.id.hoursEditText)
        notesEditText = findViewById(R.id.notesEditText)
        latEditText = findViewById(R.id.latEditText)
        lonEditText = findViewById(R.id.lonEditText)
        saveTaskButton = findViewById(R.id.saveTaskButton)
        deleteTaskButton = findViewById(R.id.deleteTaskButton)
        cancelButton = findViewById(R.id.cancelButton)

        if (editingTaskId == null) {
            deleteTaskButton.visibility = View.GONE
        } else {
            saveTaskButton.text = getString(R.string.save_changes_button)
            loadTaskForEditing(editingTaskId!!)
        }

        saveTaskButton.setOnClickListener {
            saveOrUpdateTask()
        }

        deleteTaskButton.setOnClickListener {
            editingTaskId?.let {
                taskViewModel.deleteTaskById(it)
                Toast.makeText(this, getString(R.string.task_deleted_message), Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        cancelButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Discard Changes?")
                .setMessage("Any unsaved changes will be lost.")
                .setPositiveButton("Discard") { _, _ ->
                    finish()
                }
                .setNegativeButton("Continue Editing", null)
                .show()
        }
    }

    private fun loadTaskForEditing(taskId: String) {
        taskViewModel.getTaskById(taskId) { task ->
            if (task != null) {
                titleEditText.setText(task.title)
                courseEditText.setText(task.course)
                deadlineEditText.setText(task.deadline)
                weightEditText.setText(task.weight.toString())
                hoursEditText.setText(task.estimatedHours.toString())
                notesEditText.setText(task.notes)
                latEditText.setText(task.lat.toString())
                lonEditText.setText(task.lon.toString())
            }
        }
    }

    private fun saveOrUpdateTask() {
        val title = titleEditText.text.toString().trim()
        val course = courseEditText.text.toString().trim()
        val deadline = deadlineEditText.text.toString().trim()
        val weightText = weightEditText.text.toString().trim()
        val hoursText = hoursEditText.text.toString().trim()

        if (title.isEmpty() || course.isEmpty() || deadline.isEmpty()
            || weightText.isEmpty() || hoursText.isEmpty()
        ) {
            Toast.makeText(this, getString(R.string.empty_fields_message), Toast.LENGTH_SHORT).show()
            return
        }

        val task = Task(
            id = editingTaskId ?: java.util.UUID.randomUUID().toString(),
            userEmail = userEmail,
            title = title,
            course = course,
            deadline = deadline,
            weight = weightText.toIntOrNull() ?: 0,
            estimatedHours = hoursText.toIntOrNull() ?: 0,
            notes = notesEditText.text.toString().trim(),
            lat = latEditText.text.toString().toDoubleOrNull() ?: -33.8688,
            lon = lonEditText.text.toString().toDoubleOrNull() ?: 151.2093
        )

        task.priorityScore = ScheduleGenerator.calculatePriorityScore(task)
        taskViewModel.insertTask(task)

        val message = if (editingTaskId == null) {
            R.string.task_saved_message
        } else {
            R.string.task_updated_message
        }

        Toast.makeText(this, getString(message), Toast.LENGTH_SHORT).show()
        finish()
    }
}