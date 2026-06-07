package au.edu.cqu.smartacademia.activities

import android.app.AlertDialog
import android.location.Geocoder
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
import java.util.Locale
import java.util.UUID

/**
 * Activity used to add, edit and delete academic tasks.
 *
 * This screen supports the main task management requirement of SmartAcademia.
 * Users can enter academic task details such as title, course, deadline,
 * assessment weight, estimated study hours, notes and a readable location name.
 *
 * The location name is converted into latitude and longitude using Android
 * Geocoder before the task is saved, so users do not need to manually enter
 * GPS coordinates.
 */
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
    private lateinit var locationNameEditText: EditText
    private lateinit var saveTaskButton: Button
    private lateinit var deleteTaskButton: Button
    private lateinit var cancelButton: Button

    /**
     * Creates and initialises the Add/Edit Task screen.
     *
     * This method:
     * - Connects XML views to Kotlin variables.
     * - Retrieves the logged-in user's email from SharedPreferences.
     * - Checks whether the screen is adding a new task or editing an existing task.
     * - Sets up Save, Delete and Cancel button actions.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        taskViewModel =
            ViewModelProvider(this)[TaskViewModel::class.java]

        userEmail =
            getSharedPreferences("smartacademia_session", MODE_PRIVATE)
                .getString("email", "") ?: ""

        editingTaskId =
            intent.getStringExtra("task_id")

        titleEditText =
            findViewById(R.id.taskTitleEditText)

        courseEditText =
            findViewById(R.id.courseEditText)

        deadlineEditText =
            findViewById(R.id.deadlineEditText)

        weightEditText =
            findViewById(R.id.weightEditText)

        hoursEditText =
            findViewById(R.id.hoursEditText)

        notesEditText =
            findViewById(R.id.notesEditText)

        locationNameEditText =
            findViewById(R.id.locationNameEditText)

        saveTaskButton =
            findViewById(R.id.saveTaskButton)

        deleteTaskButton =
            findViewById(R.id.deleteTaskButton)

        cancelButton =
            findViewById(R.id.cancelButton)

        if (editingTaskId == null) {
            deleteTaskButton.visibility = View.GONE
        } else {
            saveTaskButton.text =
                getString(R.string.save_changes_button)

            loadTaskForEditing(editingTaskId!!)
        }

        saveTaskButton.setOnClickListener {
            saveOrUpdateTask()
        }

        deleteTaskButton.setOnClickListener {
            deleteCurrentTask()
        }

        cancelButton.setOnClickListener {
            showDiscardChangesDialog()
        }
    }

    /**
     * Loads an existing task into the form when the user selects Edit.
     *
     * @param taskId Unique identifier of the task being edited.
     */
    private fun loadTaskForEditing(taskId: String) {
        taskViewModel.getTaskById(taskId) { task ->
            if (task != null) {
                titleEditText.setText(task.title)
                courseEditText.setText(task.course)
                deadlineEditText.setText(task.deadline)
                weightEditText.setText(task.weight.toString())
                hoursEditText.setText(task.estimatedHours.toString())
                notesEditText.setText(task.notes)
                locationNameEditText.setText(task.locationName)
            }
        }
    }

    /**
     * Saves a new task or updates an existing task.
     *
     * The method validates required fields, converts the readable location
     * name into map coordinates, calculates task priority and stores the
     * task in the Room database through the TaskViewModel.
     */
    private fun saveOrUpdateTask() {
        val title =
            titleEditText.text.toString().trim()

        val course =
            courseEditText.text.toString().trim()

        val deadline =
            deadlineEditText.text.toString().trim()

        val weightText =
            weightEditText.text.toString().trim()

        val hoursText =
            hoursEditText.text.toString().trim()

        val notes =
            notesEditText.text.toString().trim()

        val locationName =
            locationNameEditText.text.toString().trim()

        if (
            title.isEmpty() ||
            course.isEmpty() ||
            deadline.isEmpty() ||
            weightText.isEmpty() ||
            hoursText.isEmpty()
        ) {
            Toast.makeText(
                this,
                getString(R.string.empty_fields_message),
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val coordinates =
            if (locationName.isNotEmpty()) {
                getCoordinatesFromLocation(locationName)
            } else {
                Pair(0.0, 0.0)
            }

        if (
            locationName.isNotEmpty() &&
            coordinates.first == 0.0 &&
            coordinates.second == 0.0
        ) {
            Toast.makeText(
                this,
                getString(R.string.location_not_found_message),
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val task = Task(
            id = editingTaskId ?: UUID.randomUUID().toString(),
            userEmail = userEmail,
            title = title,
            course = course,
            deadline = deadline,
            weight = weightText.toIntOrNull() ?: 0,
            estimatedHours = hoursText.toIntOrNull() ?: 0,
            notes = notes,
            locationName = locationName,
            lat = coordinates.first,
            lon = coordinates.second
        )

        task.priorityScore =
            ScheduleGenerator.calculatePriorityScore(task)

        taskViewModel.insertTask(task)

        val message =
            if (editingTaskId == null) {
                R.string.task_saved_message
            } else {
                R.string.task_updated_message
            }

        Toast.makeText(
            this,
            getString(message),
            Toast.LENGTH_SHORT
        ).show()

        finish()
    }

    /**
     * Converts a human-readable location name into latitude and longitude.
     *
     * Android Geocoder is used so users can enter normal place names such as
     * "CQU Sydney" instead of manually entering decimal GPS coordinates.
     *
     * @param locationName Location name typed by the user.
     * @return A Pair containing latitude and longitude. If no location is found,
     * 0.0 and 0.0 are returned.
     */
    private fun getCoordinatesFromLocation(
        locationName: String
    ): Pair<Double, Double> {
        return try {
            val geocoder =
                Geocoder(this, Locale.getDefault())

            val addresses =
                geocoder.getFromLocationName(locationName, 1)

            if (!addresses.isNullOrEmpty()) {
                Pair(
                    addresses[0].latitude,
                    addresses[0].longitude
                )
            } else {
                Pair(0.0, 0.0)
            }
        } catch (e: Exception) {
            Pair(0.0, 0.0)
        }
    }

    /**
     * Deletes the task currently being edited.
     *
     * This method is only available when an existing task is opened
     * in edit mode.
     */
    private fun deleteCurrentTask() {
        editingTaskId?.let { taskId ->
            taskViewModel.deleteTaskById(taskId)

            Toast.makeText(
                this,
                getString(R.string.task_deleted_message),
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }

    /**
     * Displays a confirmation dialog before leaving the form.
     *
     * This helps prevent users from accidentally losing unsaved changes.
     */
    private fun showDiscardChangesDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.discard_changes_title))
            .setMessage(getString(R.string.discard_changes_message))
            .setPositiveButton(
                getString(R.string.discard_button)
            ) { _, _ ->
                finish()
            }
            .setNegativeButton(
                getString(R.string.continue_editing_button),
                null
            )
            .show()
    }
}