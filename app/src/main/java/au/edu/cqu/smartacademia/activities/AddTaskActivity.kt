package au.edu.cqu.smartacademia.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import au.edu.cqu.smartacademia.R
import au.edu.cqu.smartacademia.database.Task
import au.edu.cqu.smartacademia.viewmodel.TaskViewModel

class AddTaskActivity : AppCompatActivity() {

    private lateinit var taskViewModel: TaskViewModel
    private var userEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        userEmail = getSharedPreferences("smartacademia_session", MODE_PRIVATE)
            .getString("email", "") ?: ""

        val titleEditText = findViewById<EditText>(R.id.taskTitleEditText)
        val courseEditText = findViewById<EditText>(R.id.courseEditText)
        val deadlineEditText = findViewById<EditText>(R.id.deadlineEditText)
        val weightEditText = findViewById<EditText>(R.id.weightEditText)
        val hoursEditText = findViewById<EditText>(R.id.hoursEditText)
        val notesEditText = findViewById<EditText>(R.id.notesEditText)
        val latEditText = findViewById<EditText>(R.id.latEditText)
        val lonEditText = findViewById<EditText>(R.id.lonEditText)
        val saveTaskButton = findViewById<Button>(R.id.saveTaskButton)

        saveTaskButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val course = courseEditText.text.toString().trim()
            val deadline = deadlineEditText.text.toString().trim()
            val weightText = weightEditText.text.toString().trim()
            val hoursText = hoursEditText.text.toString().trim()

            if (title.isEmpty() || course.isEmpty() || deadline.isEmpty()
                || weightText.isEmpty() || hoursText.isEmpty()
            ) {
                Toast.makeText(this, getString(R.string.empty_fields_message), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val weight = weightText.toIntOrNull() ?: 0
            val hours = hoursText.toIntOrNull() ?: 0
            val notes = notesEditText.text.toString().trim()
            val lat = latEditText.text.toString().toDoubleOrNull() ?: -33.8688
            val lon = lonEditText.text.toString().toDoubleOrNull() ?: 151.2093

            val tempTask = Task(
                userEmail = userEmail,
                title = title,
                course = course,
                deadline = deadline,
                weight = weight,
                estimatedHours = hours,
                notes = notes,
                lat = lat,
                lon = lon
            )

            val priorityScore = au.edu.cqu.smartacademia.utils.ScheduleGenerator.calculatePriorityScore(tempTask)

            val task = tempTask
            task.priorityScore = priorityScore

            taskViewModel.insertTask(task)

            Toast.makeText(this, getString(R.string.task_saved_message), Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}