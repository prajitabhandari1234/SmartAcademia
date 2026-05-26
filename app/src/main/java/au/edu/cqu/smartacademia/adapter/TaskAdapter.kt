package au.edu.cqu.smartacademia.adapter

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import au.edu.cqu.smartacademia.R
import au.edu.cqu.smartacademia.activities.AddTaskActivity
import au.edu.cqu.smartacademia.database.Task
import au.edu.cqu.smartacademia.utils.ScheduleGenerator

class TaskAdapter(
    private var tasks: List<Task>,
    private val onDeleteClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskHolder>() {

    class TaskHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskTitleTextView: TextView = itemView.findViewById(R.id.taskTitleTextView)
        val taskCourseTextView: TextView = itemView.findViewById(R.id.taskCourseTextView)
        val taskDeadlineTextView: TextView = itemView.findViewById(R.id.taskDeadlineTextView)
        val editTaskButton: Button = itemView.findViewById(R.id.editTaskButton)
        val deleteTaskButton: Button = itemView.findViewById(R.id.deleteTaskButton)
        val emailTaskButton: Button = itemView.findViewById(R.id.emailTaskButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskHolder(view)
    }

    override fun onBindViewHolder(holder: TaskHolder, position: Int) {
        val task = tasks[position]

        holder.taskTitleTextView.text = task.title
        holder.taskCourseTextView.text = task.course
        holder.taskDeadlineTextView.text = task.deadline

        val daysRemaining = ScheduleGenerator.calculateDaysRemaining(task.deadline)
        holder.itemView.setBackgroundColor(
            when {
                daysRemaining < 0 -> 0xFFF7B6C2.toInt()
                daysRemaining == 0L -> 0xFFFFD7B5.toInt()
                daysRemaining in 1..7 -> 0xFFBDE7FF.toInt()
                else -> 0xFFA9CBE8.toInt()
            }
        )

        holder.editTaskButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, AddTaskActivity::class.java)
            intent.putExtra("task_id", task.id)
            holder.itemView.context.startActivity(intent)
        }

        holder.deleteTaskButton.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Delete") { _, _ -> onDeleteClick(task) }
                .setNegativeButton("Cancel", null)
                .show()
        }

        holder.emailTaskButton.setOnClickListener {
            sendTaskEmail(holder.itemView, task)
        }
    }

    private fun sendTaskEmail(view: View, task: Task) {
        val context = view.context

        if (task.title.isBlank() || task.course.isBlank() || task.deadline.isBlank()) {
            Toast.makeText(context, context.getString(R.string.email_missing_message), Toast.LENGTH_SHORT).show()
            return
        }

        val subject = context.getString(R.string.email_subject, task.title)

        val body = context.getString(
            R.string.email_body,
            task.title,
            task.course,
            task.deadline,
            task.weight,
            task.estimatedHours,
            task.notes.ifBlank { "No notes added" }
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            context.startActivity(
                Intent.createChooser(intent, context.getString(R.string.email_chooser_title))
            )
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.no_email_app_message), Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}