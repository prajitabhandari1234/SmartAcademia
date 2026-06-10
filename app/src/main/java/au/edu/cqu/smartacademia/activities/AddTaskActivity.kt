package au.edu.cqu.smartacademia.activities

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

/**
 * Activity used to add, edit and delete academic tasks.
 *
 * Users can create or update assessment tasks by entering task
 * details, selecting a deadline, entering assessment weight,
 * estimated study hours, notes and a readable location name.
 *
 * The deadline is selected using a DatePickerDialog followed by
 * a TimePickerDialog. This prevents invalid deadline formats.
 *
 * The location name is converted into latitude and longitude
 * using Android Geocoder so users do not need to manually enter
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
     * - Initialises the TaskViewModel.
     * - Retrieves the logged-in user email from SharedPreferences.
     * - Connects XML views to Kotlin variables.
     * - Configures the deadline picker.
     * - Loads an existing task when editing.
     * - Sets up Save, Delete and Cancel button actions.
     *
     * @param savedInstanceState Previous activity state.
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

        setupDeadlinePicker()

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
     * Configures the deadline input field.
     *
     * Users cannot manually type the deadline. When the field
     * is selected, a DatePickerDialog is displayed first and a
     * TimePickerDialog is displayed immediately after the date
     * is selected.
     */
    private fun setupDeadlinePicker() {
        deadlineEditText.keyListener = null
        deadlineEditText.isFocusable = false
        deadlineEditText.isClickable = true

        deadlineEditText.setOnClickListener {
            showDateTimePicker()
        }
    }

    /**
     * Displays a DatePickerDialog for selecting the task date.
     *
     * After the user selects a date, the selected value is stored
     * in a Calendar object and passed to the time picker.
     */
    private fun showDateTimePicker() {
        val calendar =
            Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                showTimePicker(calendar)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    /**
     * Displays a TimePickerDialog after the date has been selected.
     *
     * The selected date and time are combined and formatted as
     * yyyy-MM-dd HH:mm, which is the format used by ScheduleGenerator.
     *
     * @param calendar Calendar object containing the selected date.
     */
    private fun showTimePicker(calendar: Calendar) {
        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)

                val formatter =
                    SimpleDateFormat(
                        "yyyy-MM-dd HH:mm",
                        Locale.getDefault()
                    )

                deadlineEditText.setText(
                    formatter.format(calendar.time)
                )
            },
            23,
            45,
            true
        ).show()
    }

    /**
     * Loads an existing task into the form for editing.
     *
     * The selected task is retrieved using its ID and all task
     * fields are populated into the corresponding input controls.
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
     * This method validates:
     * - Required fields.
     * - Assessment weight range.
     * - Estimated study hours range.
     * - Deadline format.
     * - Location name conversion.
     *
     * After validation, the task priority score is calculated
     * and the task is saved to the Room database through the ViewModel.
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

        val weight =
            weightText.toIntOrNull()

        val estimatedHours =
            hoursText.toIntOrNull()

        if (weight == null || weight !in 1..100) {
            Toast.makeText(
                this,
                getString(R.string.invalid_weight_message),
                Toast.LENGTH_LONG
            ).show()

            return
        }

        if (estimatedHours == null || estimatedHours !in 1..200) {
            Toast.makeText(
                this,
                getString(R.string.invalid_hours_message),
                Toast.LENGTH_LONG
            ).show()

            return
        }

        if (!isValidDeadlineFormat(deadline)) {
            Toast.makeText(
                this,
                getString(R.string.invalid_deadline_message),
                Toast.LENGTH_LONG
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
                "Location could not be found. Task will be saved without map coordinates.",
                Toast.LENGTH_LONG
            ).show()
        }

        val task = Task(
            id = editingTaskId ?: UUID.randomUUID().toString(),
            userEmail = userEmail,
            title = title,
            course = course,
            deadline = deadline,
            weight = weight,
            estimatedHours = estimatedHours,
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
     * Validates the deadline format.
     *
     * Accepted format:
     * yyyy-MM-dd HH:mm
     *
     * @param deadline Deadline value from the input field.
     * @return True if the deadline matches the expected format.
     */
    private fun isValidDeadlineFormat(
        deadline: String
    ): Boolean {
        return try {
            val formatter =
                SimpleDateFormat(
                    "yyyy-MM-dd HH:mm",
                    Locale.getDefault()
                )

            formatter.isLenient = false
            formatter.parse(deadline) != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Converts a human-readable location name into latitude and longitude.
     *
     * The method uses Android Geocoder to search for the supplied
     * location name and retrieve geographic coordinates that can
     * later be displayed on Google Maps.
     *
     * To improve search accuracy, Australia is automatically appended
     * to the search query. This helps resolve common educational
     * locations such as CQU Sydney, Parramatta Library and other
     * Australian venues.
     *
     * If the location cannot be resolved, the method returns
     * (0.0, 0.0).
     *
     * @param locationName Human-readable location entered by the user.
     * @return Pair containing latitude and longitude coordinates.
     */
    private fun getCoordinatesFromLocation(
        locationName: String
    ): Pair<Double, Double> {

        return try {

            val geocoder =
                Geocoder(this, Locale.ENGLISH)

            /**
             * Append Australia to improve geocoding accuracy.
             */
            val searchQuery =
                "$locationName, Australia"

            val addresses =
                geocoder.getFromLocationName(
                    searchQuery,
                    1
                )

            if (!addresses.isNullOrEmpty()) {

                Pair(
                    addresses[0].latitude,
                    addresses[0].longitude
                )

            } else {

                Pair(0.0, 0.0)
            }

        } catch (e: Exception) {

            e.printStackTrace()

            Pair(0.0, 0.0)
        }
    }

    /**
     * Deletes the task currently being edited.
     *
     * This method removes the selected task from the Room database
     * using the task's unique identifier.
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
     * This prevents users from accidentally losing unsaved task
     * information when the Cancel button is selected.
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