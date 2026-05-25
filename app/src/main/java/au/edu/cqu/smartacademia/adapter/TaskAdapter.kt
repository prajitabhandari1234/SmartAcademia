package au.edu.cqu.smartacademia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import au.edu.cqu.smartacademia.R
import au.edu.cqu.smartacademia.database.Task

class TaskAdapter(
    private var tasks: List<Task>
) : RecyclerView.Adapter<TaskAdapter.TaskHolder>() {

    class TaskHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskTitleTextView: TextView = itemView.findViewById(R.id.taskTitleTextView)
        val taskCourseTextView: TextView = itemView.findViewById(R.id.taskCourseTextView)
        val taskDeadlineTextView: TextView = itemView.findViewById(R.id.taskDeadlineTextView)
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

        holder.emailTaskButton.setOnClickListener {
            // Email feature will be added in Phase 7
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