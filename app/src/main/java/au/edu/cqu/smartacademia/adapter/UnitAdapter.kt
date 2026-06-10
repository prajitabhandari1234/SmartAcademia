package au.edu.cqu.smartacademia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import au.edu.cqu.smartacademia.R
import au.edu.cqu.smartacademia.database.CourseUnit
import au.edu.cqu.smartacademia.database.Task

/**
 * Adapter used to display user-created university unit cards.
 */
class UnitAdapter(
    private var units: List<CourseUnit>,
    private var tasks: List<Task>,
    private val onUnitClick: (CourseUnit) -> kotlin.Unit,
    private val onEditClick: (CourseUnit) -> kotlin.Unit
) : RecyclerView.Adapter<UnitAdapter.UnitViewHolder>() {

    /**
     * ViewHolder for one unit card.
     */
    class UnitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val unitCodeTextView: TextView =
            view.findViewById(R.id.unitCodeTextView)

        val unitNameTextView: TextView =
            view.findViewById(R.id.unitNameTextView)

        val unitAssignmentCountTextView: TextView =
            view.findViewById(R.id.unitAssignmentCountTextView)

        val unitGradeSummaryTextView: TextView =
            view.findViewById(R.id.unitGradeSummaryTextView)

        val unitStatusTextView: TextView =
            view.findViewById(R.id.unitStatusTextView)

        val editUnitButton: Button =
            view.findViewById(R.id.editUnitButton)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UnitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_unit, parent, false)

        return UnitViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: UnitViewHolder,
        position: Int
    ) {
        val unit = units[position]

        val unitTasks = tasks.filter {
            it.unitId == unit.id ||
                    it.course.equals(unit.unitCode, ignoreCase = true)
        }

        val completedWeight = unitTasks
            .filter { it.completed }
            .sumOf { it.weight }

        val pendingWeight = unitTasks
            .filter { !it.completed }
            .sumOf { it.weight }

        val needToPass =
            (unit.passMark - completedWeight).coerceAtLeast(0)

        holder.unitCodeTextView.text = unit.unitCode
        holder.unitNameTextView.text = unit.unitName
        holder.unitAssignmentCountTextView.text =
            "${unitTasks.size} assignments"

        holder.unitGradeSummaryTextView.text =
            "Secured: $completedWeight% · Pending: $pendingWeight%"

        holder.unitStatusTextView.text =
            when {
                completedWeight >= unit.passMark ->
                    "Status: Passing"

                completedWeight + pendingWeight < unit.passMark ->
                    "Status: Cannot reach pass mark"

                else ->
                    "Status: Need $needToPass% more to pass"
            }

        holder.itemView.setOnClickListener {
            onUnitClick(unit)
        }

        holder.editUnitButton.setOnClickListener {
            onEditClick(unit)
        }
    }

    override fun getItemCount(): Int {
        return units.size
    }

    /**
     * Updates the adapter data.
     */
    fun updateData(
        updatedUnits: List<CourseUnit>,
        updatedTasks: List<Task>
    ) {
        units = updatedUnits
        tasks = updatedTasks
        notifyDataSetChanged()
    }
}