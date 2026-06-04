package au.edu.cqu.smartacademia.adapter

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import au.edu.cqu.smartacademia.R
import au.edu.cqu.smartacademia.activities.AddTaskActivity
import au.edu.cqu.smartacademia.activities.MapsActivity
import au.edu.cqu.smartacademia.database.Task
import au.edu.cqu.smartacademia.utils.ScheduleGenerator

/**
 * RecyclerView adapter responsible for displaying
 * SmartAcademia task items in the task list.
 *
 * Supports Assignment 3 requirements:
 * - RecyclerView list display.
 * - ViewHolder pattern.
 * - Email sharing.
 * - Google Maps navigation.
 * - Task editing and deletion.
 * - Task completion workflow.
 */
class TaskAdapter(
    private var tasks: List<Task>,
    private val onDeleteClick: (Task) -> Unit,
    private val onCompleteClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskHolder>() {

    /**
     * ViewHolder that stores references to
     * task item UI components.
     *
     * @param itemView Root view of a task item.
     */
    class TaskHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskTitleTextView: TextView =
            itemView.findViewById(R.id.taskTitleTextView)

        val taskCourseTextView: TextView =
            itemView.findViewById(R.id.taskCourseTextView)

        val taskDeadlineTextView: TextView =
            itemView.findViewById(R.id.taskDeadlineTextView)

        val editTaskButton: Button =
            itemView.findViewById(R.id.editTaskButton)

        val deleteTaskButton: Button =
            itemView.findViewById(R.id.deleteTaskButton)

        val emailTaskButton: Button =
            itemView.findViewById(R.id.emailTaskButton)

        val mapTaskButton: Button =
            itemView.findViewById(R.id.mapTaskButton)

        val completeTaskButton: Button =
            itemView.findViewById(R.id.completeTaskButton)
    }

    /**
     * Creates a new ViewHolder by inflating
     * the task item layout.
     *
     * @param parent Parent ViewGroup.
     * @param viewType RecyclerView item type.
     * @return New TaskHolder instance.
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TaskHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_task,
                parent,
                false
            )

        return TaskHolder(view)
    }

    /**
     * Binds task data to the ViewHolder.
     *
     * Also configures:
     * - Task status colour.
     * - Completion button.
     * - Edit button.
     * - Delete button.
     * - Email button.
     * - Map button.
     * - Task card click for map navigation.
     *
     * @param holder TaskHolder to bind.
     * @param position Task position in the list.
     */
    override fun onBindViewHolder(
        holder: TaskHolder,
        position: Int
    ) {
        val task = tasks[position]

        holder.taskTitleTextView.text = task.title
        holder.taskCourseTextView.text = task.course
        holder.taskDeadlineTextView.text = task.deadline

        val daysRemaining =
            ScheduleGenerator.calculateDaysRemaining(task.deadline)

        holder.itemView.setBackgroundColor(
            when {
                task.completed ->
                    Color.parseColor("#C8E6C9")

                daysRemaining < 0 ->
                    Color.parseColor("#F7B6C2")

                daysRemaining == 0L ->
                    Color.parseColor("#FFD7B5")

                daysRemaining in 1..7 ->
                    Color.parseColor("#BDE7FF")

                else ->
                    Color.parseColor("#A9CBE8")
            }
        )

        if (task.completed) {
            holder.completeTaskButton.text =
                holder.itemView.context.getString(R.string.completed_button)

            holder.completeTaskButton.isEnabled = false

            holder.completeTaskButton.setBackgroundColor(
                Color.parseColor("#4CAF50")
            )

            holder.completeTaskButton.setOnClickListener(null)
        } else {
            holder.completeTaskButton.text =
                holder.itemView.context.getString(R.string.mark_as_completed)

            holder.completeTaskButton.isEnabled = true

            holder.completeTaskButton.setBackgroundColor(
                Color.parseColor("#6C63FF")
            )

            holder.completeTaskButton.setOnClickListener {
                showCompleteTaskDialog(holder, task)
            }
        }

        holder.itemView.setOnClickListener {
            openTaskLocation(holder, task)
        }

        holder.editTaskButton.setOnClickListener {
            openEditTaskScreen(holder, task)
        }

        holder.deleteTaskButton.setOnClickListener {
            showDeleteTaskDialog(holder, task)
        }

        holder.emailTaskButton.setOnClickListener {
            sendTaskEmail(holder.itemView, task)
        }

        holder.mapTaskButton.setOnClickListener {
            openTaskLocation(holder, task)
        }
    }

    /**
     * Shows confirmation dialog before marking
     * a task as completed.
     *
     * @param holder Current task ViewHolder.
     * @param task Selected task.
     */
    private fun showCompleteTaskDialog(
        holder: TaskHolder,
        task: Task
    ) {
        AlertDialog.Builder(holder.itemView.context)
            .setTitle(
                holder.itemView.context.getString(
                    R.string.complete_task_title
                )
            )
            .setMessage(
                holder.itemView.context.getString(
                    R.string.complete_task_message
                )
            )
            .setPositiveButton(
                holder.itemView.context.getString(
                    R.string.yes_button
                )
            ) { _, _ ->
                onCompleteClick(task)
            }
            .setNegativeButton(
                holder.itemView.context.getString(
                    R.string.cancel_button
                ),
                null
            )
            .show()
    }

    /**
     * Opens AddTaskActivity in edit mode.
     *
     * @param holder Current task ViewHolder.
     * @param task Task selected for editing.
     */
    private fun openEditTaskScreen(
        holder: TaskHolder,
        task: Task
    ) {
        val intent =
            Intent(
                holder.itemView.context,
                AddTaskActivity::class.java
            )

        intent.putExtra(
            "task_id",
            task.id
        )

        holder.itemView.context.startActivity(intent)
    }

    /**
     * Shows confirmation dialog before deleting a task.
     *
     * @param holder Current task ViewHolder.
     * @param task Task selected for deletion.
     */
    private fun showDeleteTaskDialog(
        holder: TaskHolder,
        task: Task
    ) {
        AlertDialog.Builder(holder.itemView.context)
            .setTitle(
                holder.itemView.context.getString(
                    R.string.delete_task
                )
            )
            .setMessage(
                holder.itemView.context.getString(
                    R.string.delete_task_confirm_message
                )
            )
            .setPositiveButton(
                holder.itemView.context.getString(
                    R.string.delete_button
                )
            ) { _, _ ->
                onDeleteClick(task)
            }
            .setNegativeButton(
                holder.itemView.context.getString(
                    R.string.cancel_button
                ),
                null
            )
            .show()
    }

    /**
     * Opens an email chooser with selected task details.
     *
     * The email will not open if key task details
     * are missing.
     *
     * @param view Current task item view.
     * @param task Task details to share.
     */
    private fun sendTaskEmail(
        view: View,
        task: Task
    ) {
        val context = view.context

        if (
            task.title.isBlank() ||
            task.course.isBlank() ||
            task.deadline.isBlank()
        ) {
            Toast.makeText(
                context,
                context.getString(R.string.email_missing_message),
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val subject =
            context.getString(
                R.string.email_subject,
                task.title
            )

        val body =
            context.getString(
                R.string.email_body,
                task.title,
                task.course,
                task.deadline,
                task.weight,
                task.estimatedHours,
                task.notes.ifBlank {
                    context.getString(R.string.no_notes_added)
                }
            )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(
                Intent.EXTRA_SUBJECT,
                subject
            )
            putExtra(
                Intent.EXTRA_TEXT,
                body
            )
        }

        try {
            context.startActivity(
                Intent.createChooser(
                    intent,
                    context.getString(R.string.email_chooser_title)
                )
            )
        } catch (e: Exception) {
            Toast.makeText(
                context,
                context.getString(R.string.no_email_app_message),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Opens the map screen for the selected task.
     *
     * The map does not open if the task does
     * not contain valid coordinates.
     *
     * @param holder Current task ViewHolder.
     * @param task Selected task.
     */
    private fun openTaskLocation(
        holder: TaskHolder,
        task: Task
    ) {
        val context = holder.itemView.context

        if (!isValidLocation(task.lat, task.lon)) {
            Toast.makeText(
                context,
                context.getString(R.string.invalid_task_location),
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val intent =
            Intent(
                context,
                MapsActivity::class.java
            )

        intent.putExtra(
            "task",
            task
        )

        context.startActivity(intent)
    }

    /**
     * Validates map coordinates before opening MapsActivity.
     *
     * @param lat Latitude value.
     * @param lon Longitude value.
     * @return true if coordinates are valid.
     */
    private fun isValidLocation(
        lat: Double,
        lon: Double
    ): Boolean {
        return lat != 0.0 &&
                lon != 0.0 &&
                lat in -90.0..90.0 &&
                lon in -180.0..180.0
    }

    /**
     * Returns the total number of tasks displayed.
     *
     * @return Task count.
     */
    override fun getItemCount(): Int = tasks.size

    /**
     * Updates RecyclerView task data.
     *
     * Called when LiveData, filtering or sorting changes.
     *
     * @param newTasks Updated task list.
     */
    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}