package au.edu.cqu.smartacademia.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import au.edu.cqu.smartacademia.R
import au.edu.cqu.smartacademia.activities.RegenerateScheduleActivity
import au.edu.cqu.smartacademia.database.Task
import au.edu.cqu.smartacademia.utils.ScheduleGenerator
import au.edu.cqu.smartacademia.viewmodel.TaskViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class ScheduleItem(
    val time: String,
    val title: String,
    val course: String,
    val detail: String,
    val color: String,
    val isCustom: Boolean = false
)

class ScheduleFragment : Fragment() {

    private lateinit var taskViewModel: TaskViewModel
    private lateinit var timelineLayout: LinearLayout
    private lateinit var dateTextView: TextView
    private lateinit var weekRangeTextView: TextView

    private lateinit var sunTextView: TextView
    private lateinit var monTextView: TextView
    private lateinit var tueTextView: TextView
    private lateinit var wedTextView: TextView
    private lateinit var thuTextView: TextView
    private lateinit var friTextView: TextView
    private lateinit var satTextView: TextView

    private var userEmail = ""
    private var latestTasks: List<Task> = emptyList()

    private val selectedDate = Calendar.getInstance()
    private val weekStartDate = Calendar.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_schedule, container, false)

        timelineLayout = view.findViewById(R.id.timelineLayout)
        dateTextView = view.findViewById(R.id.dateTextView)
        weekRangeTextView = view.findViewById(R.id.weekRangeTextView)

        sunTextView = view.findViewById(R.id.sunTextView)
        monTextView = view.findViewById(R.id.monTextView)
        tueTextView = view.findViewById(R.id.tueTextView)
        wedTextView = view.findViewById(R.id.wedTextView)
        thuTextView = view.findViewById(R.id.thuTextView)
        friTextView = view.findViewById(R.id.friTextView)
        satTextView = view.findViewById(R.id.satTextView)

        val regenerateButton = view.findViewById<Button>(R.id.regenerateButton)
        val dayButton = view.findViewById<Button>(R.id.dayButton)
        val weekButton = view.findViewById<Button>(R.id.weekButton)
        val previousWeekButton = view.findViewById<Button>(R.id.previousWeekButton)
        val nextWeekButton = view.findViewById<Button>(R.id.nextWeekButton)

        userEmail = requireActivity()
            .getSharedPreferences("smartacademia_session", Context.MODE_PRIVATE)
            .getString("email", "") ?: ""

        setStartOfWeek()
        updateDateHeaders()

        setupDayClicks()

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]
        taskViewModel.getTasksForUser(userEmail).observe(viewLifecycleOwner) { tasks ->
            latestTasks = tasks.filter { !it.completed }
            generateDayPlan()
        }

        regenerateButton.setOnClickListener {
            openRegeneratePage()
        }

        dayButton.setOnClickListener {
            generateDayPlan()
        }

        weekButton.setOnClickListener {
            generateWeekPlan()
        }

        previousWeekButton.setOnClickListener {
            weekStartDate.add(Calendar.DAY_OF_MONTH, -7)
            selectedDate.time = weekStartDate.time
            updateDateHeaders()
            generateDayPlan()
        }

        nextWeekButton.setOnClickListener {
            weekStartDate.add(Calendar.DAY_OF_MONTH, 7)
            selectedDate.time = weekStartDate.time
            updateDateHeaders()
            generateDayPlan()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        if (::timelineLayout.isInitialized) {
            generateDayPlan()
        }
    }

    private fun setStartOfWeek() {
        weekStartDate.time = selectedDate.time
        while (weekStartDate.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            weekStartDate.add(Calendar.DAY_OF_MONTH, -1)
        }
    }

    private fun updateDateHeaders() {
        val fullFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        val shortFormat = SimpleDateFormat("EEE\nd MMM", Locale.getDefault())

        dateTextView.text = fullFormat.format(selectedDate.time)

        val endWeek = weekStartDate.clone() as Calendar
        endWeek.add(Calendar.DAY_OF_MONTH, 6)

        val rangeFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        weekRangeTextView.text =
            "${rangeFormat.format(weekStartDate.time)} - ${rangeFormat.format(endWeek.time)}"

        val dayViews = listOf(sunTextView, monTextView, tueTextView, wedTextView, thuTextView, friTextView, satTextView)

        for (i in dayViews.indices) {
            val day = weekStartDate.clone() as Calendar
            day.add(Calendar.DAY_OF_MONTH, i)

            dayViews[i].text = shortFormat.format(day.time)
            dayViews[i].setBackgroundColor(Color.WHITE)
            dayViews[i].setTextColor(Color.BLACK)

            if (isSameDate(day, selectedDate)) {
                dayViews[i].setBackgroundColor(Color.parseColor("#4A90D6"))
                dayViews[i].setTextColor(Color.WHITE)
            }
        }
    }

    private fun setupDayClicks() {
        val dayViews = listOf(sunTextView, monTextView, tueTextView, wedTextView, thuTextView, friTextView, satTextView)

        dayViews.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                selectedDate.time = weekStartDate.time
                selectedDate.add(Calendar.DAY_OF_MONTH, index)
                updateDateHeaders()
                generateDayPlan()
            }
        }
    }

    private fun openRegeneratePage(editingStartTime: String? = null) {
        val intent = Intent(requireContext(), RegenerateScheduleActivity::class.java)
        intent.putExtra("selected_date_key", getSelectedDateKey())
        editingStartTime?.let {
            intent.putExtra("editing_start_time", it)
        }
        startActivity(intent)
    }

    private fun getSelectedDateKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time)
    }

    private fun loadPlansForSelectedDate(): JSONArray {
        val text = requireActivity()
            .getSharedPreferences("smartacademia_schedule", Context.MODE_PRIVATE)
            .getString(getSelectedDateKey(), "[]")

        return JSONArray(text)
    }

    private fun savePlansForSelectedDate(plans: JSONArray) {
        requireActivity()
            .getSharedPreferences("smartacademia_schedule", Context.MODE_PRIVATE)
            .edit()
            .putString(getSelectedDateKey(), plans.toString())
            .apply()
    }

    private fun generateDayPlan() {
        timelineLayout.removeAllViews()

        val scheduleItems = mutableListOf<ScheduleItem>()

        val customPlans = loadPlansForSelectedDate()
        for (i in 0 until customPlans.length()) {
            val plan = customPlans.getJSONObject(i)
            scheduleItems.add(
                ScheduleItem(
                    time = plan.getString("startTime"),
                    title = plan.getString("planName"),
                    course = "Custom Schedule",
                    detail = formatPlanDetails(plan),
                    color = "#BDE7FF",
                    isCustom = true
                )
            )
        }

        val sortedTasks = latestTasks.sortedByDescending {
            ScheduleGenerator.calculatePriorityScore(it)
        }

        val firstTask = sortedTasks.getOrNull(0)
        val secondTask = sortedTasks.getOrNull(1)

        scheduleItems.add(
            ScheduleItem(
                time = "9:00 AM",
                title = firstTask?.title ?: "Morning Study Session",
                course = firstTask?.course ?: "Personal Study",
                detail = "${firstTask?.estimatedHours ?: 2} hrs • Priority 1",
                color = "#F4A6B8"
            )
        )

        scheduleItems.add(
            ScheduleItem(
                time = "11:00 AM",
                title = "Break / Free Time",
                course = "Personal Schedule",
                detail = "No fixed task",
                color = "#CFCFD6"
            )
        )

        scheduleItems.add(
            ScheduleItem(
                time = "12:00 PM",
                title = secondTask?.title ?: "Revision",
                course = secondTask?.course ?: "General Study",
                detail = "${secondTask?.estimatedHours ?: 2} hrs • Priority 2",
                color = "#F4CDBB"
            )
        )

        scheduleItems.add(
            ScheduleItem(
                time = "3:00 PM",
                title = "Lecture / External Study",
                course = "Custom Activity",
                detail = "Zoom / Library / Practice",
                color = "#F4CDBB"
            )
        )

        val finalItems = scheduleItems
            .groupBy { normalizeTime(it.time) }
            .map { entry ->
                val custom = entry.value.find { it.isCustom }
                custom ?: entry.value.first()
            }
            .sortedBy { convertTimeToMinutes(it.time) }

        if (finalItems.isEmpty()) {
            addEmptyState()
            return
        }

        finalItems.forEach { item ->
            addScheduleBlock(item)
        }
    }

    private fun generateWeekPlan() {
        timelineLayout.removeAllViews()

        for (i in 0..6) {
            val day = weekStartDate.clone() as Calendar
            day.add(Calendar.DAY_OF_MONTH, i)

            val key = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(day.time)
            val label = SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(day.time)

            val plansText = requireActivity()
                .getSharedPreferences("smartacademia_schedule", Context.MODE_PRIVATE)
                .getString(key, "[]")

            val plans = JSONArray(plansText)

            if (plans.length() == 0) {
                addScheduleBlock(
                    ScheduleItem(
                        time = label,
                        title = "No Custom Plans",
                        course = "Weekly View",
                        detail = "Tap a day and create a plan",
                        color = "#CFCFD6"
                    )
                )
            } else {
                for (j in 0 until plans.length()) {
                    val plan = plans.getJSONObject(j)
                    addScheduleBlock(
                        ScheduleItem(
                            time = label,
                            title = "${plan.getString("startTime")} - ${plan.getString("planName")}",
                            course = "Custom Schedule",
                            detail = formatPlanDetails(plan),
                            color = "#BDE7FF",
                            isCustom = true
                        )
                    )
                }
            }
        }
    }

    private fun addScheduleBlock(item: ScheduleItem) {
        val row = LinearLayout(requireContext())
        row.orientation = LinearLayout.HORIZONTAL
        row.setPadding(0, 8, 0, 8)

        val timeText = TextView(requireContext())
        timeText.text = item.time.replace(" ", "\n")
        timeText.setTextColor(Color.parseColor("#0B5FA5"))
        timeText.textSize = 16f
        timeText.layoutParams = LinearLayout.LayoutParams(180, 240)

        val block = TextView(requireContext())
        block.text = "${item.title}\n${item.course}\n${item.detail}"
        block.setTextColor(Color.parseColor("#0B5FA5"))
        block.textSize = 15f
        block.setPadding(18, 10, 18, 10)
        block.setBackgroundColor(Color.parseColor(item.color))
        block.layoutParams = LinearLayout.LayoutParams(0, 240, 1f)

        if (item.isCustom) {
            block.setOnClickListener {
                showPlanActionsDialog(item)
            }

            block.setOnLongClickListener {
                showPlanActionsDialog(item)
                true
            }
        } else {
            block.setOnClickListener {
                showDetailsDialog(item)
            }
        }

        row.addView(timeText)
        row.addView(block)
        timelineLayout.addView(row)
    }

    private fun showDetailsDialog(item: ScheduleItem) {
        AlertDialog.Builder(requireContext())
            .setTitle(item.title)
            .setMessage(
                "Time: ${item.time}\n\n" +
                        "Type: ${item.course}\n\n" +
                        item.detail
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showPlanActionsDialog(item: ScheduleItem) {
        val options = arrayOf("View Details", "Edit Plan", "Delete Plan")

        AlertDialog.Builder(requireContext())
            .setTitle(item.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showDetailsDialog(item)
                    1 -> openRegeneratePage(item.time)
                    2 -> deletePlan(item.time)
                }
            }
            .show()
    }

    private fun deletePlan(startTime: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Plan")
            .setMessage("Delete the plan at $startTime?")
            .setPositiveButton("Delete") { _, _ ->
                val oldPlans = loadPlansForSelectedDate()
                val updatedPlans = JSONArray()

                for (i in 0 until oldPlans.length()) {
                    val plan = oldPlans.getJSONObject(i)
                    if (normalizeTime(plan.getString("startTime")) != normalizeTime(startTime)) {
                        updatedPlans.put(plan)
                    }
                }

                savePlansForSelectedDate(updatedPlans)
                generateDayPlan()
                Toast.makeText(requireContext(), "Plan deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addEmptyState() {
        addScheduleBlock(
            ScheduleItem(
                time = "",
                title = "No plans for this day",
                course = "Smart Schedule",
                detail = "Tap Regenerate Today's Plan to create your first schedule.",
                color = "#CFCFD6"
            )
        )
    }

    private fun formatPlanDetails(plan: JSONObject): String {
        return "Duration: ${plan.getString("hours")} hr(s)\n" +
                "Activity: ${plan.getString("activity")}\n" +
                "Location: ${plan.getString("locationName")}\n" +
                "Lat/Lon: ${plan.getString("latitude")}, ${plan.getString("longitude")}"
    }

    private fun isSameDate(a: Calendar, b: Calendar): Boolean {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    }

    private fun normalizeTime(time: String): String {
        return time.trim().uppercase(Locale.getDefault())
            .replace("  ", " ")
    }

    private fun convertTimeToMinutes(time: String): Int {
        return try {
            val parts = normalizeTime(time).split(" ")
            val hourMinute = parts[0].split(":")
            var hour = hourMinute[0].toInt()
            val minute = hourMinute.getOrNull(1)?.toIntOrNull() ?: 0
            val amPm = parts.getOrNull(1) ?: "AM"

            if (amPm == "PM" && hour != 12) hour += 12
            if (amPm == "AM" && hour == 12) hour = 0

            hour * 60 + minute
        } catch (e: Exception) {
            9999
        }
    }
}