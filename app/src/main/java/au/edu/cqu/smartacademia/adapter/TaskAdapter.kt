package au.edu.cqu.smartacademia.adapter

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
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
        val backgroundColor = when {
            daysRemaining < 0 -> 0xFFF7B6C2.toInt()
            daysRemaining == 0L -> 0xFFFFD7B5.toInt()
            daysRemaining in 1..7 -> 0xFFBDE7FF.toInt()
            else -> 0xFFA9CBE8.toInt()
        }
        holder.itemView.setBackgroundColor(backgroundColor)

        holder.editTaskButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, AddTaskActivity::class.java)
            intent.putExtra("task_id", task.id)
            holder.itemView.context.startActivity(intent)
        }

        holder.deleteTaskButton.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Delete") { _, _ ->
                    onDeleteClick(task)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        holder.emailTaskButton.setOnClickListener {
            // Phase 7 email feature
        }
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}