package au.edu.cqu.smartacademia.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.cqu.smartacademia.R
import au.edu.cqu.smartacademia.activities.AddUnitActivity
import au.edu.cqu.smartacademia.activities.UnitDetailActivity
import au.edu.cqu.smartacademia.adapter.UnitAdapter
import au.edu.cqu.smartacademia.database.CourseUnit
import au.edu.cqu.smartacademia.database.Task
import au.edu.cqu.smartacademia.viewmodel.TaskViewModel
import au.edu.cqu.smartacademia.viewmodel.UnitViewModel

/**
 * Fragment showing the student's university units.
 */
class UnitDashboardFragment : Fragment() {

    private lateinit var unitViewModel: UnitViewModel
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var unitAdapter: UnitAdapter

    private var userEmail: String = ""
    private var latestUnits: List<CourseUnit> = emptyList()
    private var latestTasks: List<Task> = emptyList()

    /**
     * Creates the unit dashboard.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.fragment_unit_dashboard,
            container,
            false
        )

        userEmail = requireActivity()
            .getSharedPreferences(
                "smartacademia_session",
                Context.MODE_PRIVATE
            )
            .getString("email", "") ?: ""

        val addUnitButton =
            view.findViewById<View>(R.id.addUnitButton)

        val unitRecyclerView =
            view.findViewById<RecyclerView>(R.id.unitRecyclerView)

        unitAdapter = UnitAdapter(
            units = emptyList(),
            tasks = emptyList(),
            onUnitClick = { unit: CourseUnit ->
                val intent =
                    Intent(requireContext(), UnitDetailActivity::class.java)

                intent.putExtra("unit_id", unit.id)
                intent.putExtra("unit_code", unit.unitCode)
                intent.putExtra("unit_name", unit.unitName)
                intent.putExtra("pass_mark", unit.passMark)

                startActivity(intent)
            },
            onEditClick = { unit: CourseUnit ->
                val intent =
                    Intent(requireContext(), AddUnitActivity::class.java)

                intent.putExtra("unit_id", unit.id)
                startActivity(intent)
            }
        )

        unitRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        unitRecyclerView.adapter =
            unitAdapter

        unitViewModel =
            ViewModelProvider(this)[UnitViewModel::class.java]

        taskViewModel =
            ViewModelProvider(this)[TaskViewModel::class.java]

        addUnitButton.setOnClickListener {
            startActivity(
                Intent(requireContext(), AddUnitActivity::class.java)
            )
        }

        observeUnitsAndTasks()

        return view
    }

    /**
     * Observes units and tasks so cards update automatically.
     */
    private fun observeUnitsAndTasks() {
        unitViewModel.getUnitsForUser(userEmail)
            .observe(viewLifecycleOwner) { units ->
                latestUnits = units
                updateAdapter()
            }

        taskViewModel.getTasksForUser(userEmail)
            .observe(viewLifecycleOwner) { tasks ->
                latestTasks = tasks
                updateAdapter()
            }
    }

    /**
     * Updates the adapter with latest data.
     */
    private fun updateAdapter() {
        unitAdapter.updateData(
            latestUnits,
            latestTasks
        )
    }
}