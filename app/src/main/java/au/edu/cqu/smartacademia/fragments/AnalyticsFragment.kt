package au.edu.cqu.smartacademia.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import au.edu.cqu.smartacademia.R
import au.edu.cqu.smartacademia.database.Task
import au.edu.cqu.smartacademia.utils.ScheduleGenerator
import au.edu.cqu.smartacademia.viewmodel.TaskViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Analytics screen for SmartAcademia.
 *
 * Displays task statistics, progress charts,
 * study workload, productivity score and upcoming deadlines.
 *
 * This fragment uses ViewModel and LiveData so that analytics
 * update automatically when task data changes.
 */
class AnalyticsFragment : Fragment() {

    private lateinit var taskViewModel: TaskViewModel
    private var userEmail: String = ""

    private val chartWeekStart = Calendar.getInstance()
    private var currentTasks: List<Task> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_analytics, container, false)

        val analyticsDateTextView = view.findViewById<TextView>(R.id.analyticsDateTextView)
        val onTimeRateTextView = view.findViewById<TextView>(R.id.onTimeRateTextView)
        val taskDoneTextView = view.findViewById<TextView>(R.id.taskDoneTextView)
        val overdueTextView = view.findViewById<TextView>(R.id.overdueTextView)
        val upcomingTextView = view.findViewById<TextView>(R.id.upcomingTextView)
        val productivityTextView = view.findViewById<TextView>(R.id.productivityTextView)
        val studyHoursTextView = view.findViewById<TextView>(R.id.studyHoursTextView)
        val upcomingDeadlineTextView = view.findViewById<TextView>(R.id.upcomingDeadlineTextView)
        val analyticsWeekRangeTextView = view.findViewById<TextView>(R.id.analyticsWeekRangeTextView)

        val previousAnalyticsWeekButton =
            view.findViewById<Button>(R.id.previousAnalyticsWeekButton)

        val nextAnalyticsWeekButton =
            view.findViewById<Button>(R.id.nextAnalyticsWeekButton)

        val pieChart = view.findViewById<PieChart>(R.id.pieChart)
        val barChart = view.findViewById<BarChart>(R.id.barChart)

        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        analyticsDateTextView.text = dateFormat.format(Calendar.getInstance().time)

        setStartOfWeek()

        userEmail = requireActivity()
            .getSharedPreferences("smartacademia_session", Context.MODE_PRIVATE)
            .getString("email", "") ?: ""

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        taskViewModel.getTasksForUser(userEmail).observe(viewLifecycleOwner) { tasks ->
            currentTasks = tasks

            updateAnalytics(
                tasks,
                onTimeRateTextView,
                taskDoneTextView,
                overdueTextView,
                upcomingTextView,
                productivityTextView,
                studyHoursTextView,
                upcomingDeadlineTextView,
                pieChart,
                barChart,
                analyticsWeekRangeTextView
            )
        }

        previousAnalyticsWeekButton.setOnClickListener {
            chartWeekStart.add(Calendar.DAY_OF_MONTH, -7)
            setupBarChart(barChart, analyticsWeekRangeTextView)
        }

        nextAnalyticsWeekButton.setOnClickListener {
            chartWeekStart.add(Calendar.DAY_OF_MONTH, 7)
            setupBarChart(barChart, analyticsWeekRangeTextView)
        }

        return view
    }

    /**
     * Calculates and displays task analytics.
     *
     * Updates statistic cards, progress chart,
     * weekly chart and upcoming deadline summary.
     *
     * @param tasks Current list of user tasks.
     */
    private fun updateAnalytics(
        tasks: List<Task>,
        onTimeRateTextView: TextView,
        taskDoneTextView: TextView,
        overdueTextView: TextView,
        upcomingTextView: TextView,
        productivityTextView: TextView,
        studyHoursTextView: TextView,
        upcomingDeadlineTextView: TextView,
        pieChart: PieChart,
        barChart: BarChart,
        analyticsWeekRangeTextView: TextView
    ) {
        val totalTasks = tasks.size
        val completedTasks = tasks.count { it.completed }
        val overdueTasks = ScheduleGenerator.countOverdue(tasks)

        val upcomingTasks = tasks.count {
            !it.completed && ScheduleGenerator.calculateDaysRemaining(it.deadline) >= 0
        }

        val pendingTasks = tasks.count { !it.completed }

        val onTimeRate =
            if (totalTasks == 0) 0
            else ((totalTasks - overdueTasks) * 100) / totalTasks

        val productivityScore =
            if (totalTasks == 0) 0
            else (completedTasks * 100) / totalTasks

        val completedPercent =
            if (totalTasks == 0) 0
            else (completedTasks * 100) / totalTasks

        val inProgressPercent =
            if (totalTasks == 0) 0
            else (pendingTasks * 100) / totalTasks

        val notStartedPercent =
            (100 - completedPercent - inProgressPercent).coerceAtLeast(0)

        val totalStudyHours = tasks
            .filter { !it.completed }
            .sumOf { it.estimatedHours }

        onTimeRateTextView.text =
            "$onTimeRate%\n${getString(R.string.on_time_rate_label)}"

        taskDoneTextView.text =
            "$completedTasks\n${getString(R.string.task_done_label)}"

        overdueTextView.text =
            "$overdueTasks\n${getString(R.string.overdue_label)}"

        upcomingTextView.text =
            "$upcomingTasks\n${getString(R.string.upcoming_label)}"

        productivityTextView.text =
            "$productivityScore%\n${getString(R.string.productivity_label)}"

        studyHoursTextView.text =
            "$totalStudyHours hrs\n${getString(R.string.study_load_label)}"

        setupPieChart(
            pieChart,
            completedPercent,
            inProgressPercent,
            notStartedPercent
        )

        setupBarChart(barChart, analyticsWeekRangeTextView)

        upcomingDeadlineTextView.text = generateUpcomingDeadlineText(tasks)
    }

    /**
     * Configures the progress pie chart.
     *
     * @param completedPercent Percentage of completed tasks.
     * @param inProgressPercent Percentage of active tasks.
     * @param notStartedPercent Percentage of not-started tasks.
     */
    private fun setupPieChart(
        pieChart: PieChart,
        completedPercent: Int,
        inProgressPercent: Int,
        notStartedPercent: Int
    ) {
        val entries = ArrayList<PieEntry>()

        entries.add(
            PieEntry(
                completedPercent.toFloat(),
                getString(R.string.completed)
            )
        )

        entries.add(
            PieEntry(
                inProgressPercent.toFloat(),
                "In Progress"
            )
        )

        entries.add(
            PieEntry(
                notStartedPercent.toFloat(),
                "Not Started"
            )
        )

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(
            Color.rgb(47, 128, 199),
            Color.rgb(155, 196, 230),
            Color.rgb(220, 230, 240)
        )

        dataSet.valueTextColor = Color.rgb(11, 95, 165)
        dataSet.valueTextSize = 12f

        pieChart.data = PieData(dataSet)
        pieChart.description.isEnabled = false
        pieChart.centerText = getString(R.string.progress_chart_title)
        pieChart.setCenterTextSize(16f)
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.legend.isEnabled = true
        pieChart.invalidate()
    }

    /**
     * Sets the analytics calendar to the beginning
     * of the current week.
     */
    private fun setStartOfWeek() {
        while (chartWeekStart.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            chartWeekStart.add(Calendar.DAY_OF_MONTH, -1)
        }
    }

    /**
     * Generates the weekly bar chart for the selected week.
     *
     * @param barChart Bar chart view.
     * @param analyticsWeekRangeTextView TextView that displays the selected week range.
     */
    private fun setupBarChart(
        barChart: BarChart,
        analyticsWeekRangeTextView: TextView
    ) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val rangeFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

        val weekEnd = chartWeekStart.clone() as Calendar
        weekEnd.add(Calendar.DAY_OF_MONTH, 6)

        analyticsWeekRangeTextView.text =
            "${rangeFormat.format(chartWeekStart.time)} - ${rangeFormat.format(weekEnd.time)}"

        for (i in 0..6) {
            val day = chartWeekStart.clone() as Calendar
            day.add(Calendar.DAY_OF_MONTH, i)

            labels.add(dateFormat.format(day.time))

            val value = getDemoWeeklyValue(i)
            entries.add(BarEntry(i.toFloat(), value.toFloat()))
        }

        val dataSet = BarDataSet(
            entries,
            getString(R.string.tasks_completed_chart)
        )

        dataSet.color = Color.rgb(47, 128, 199)
        dataSet.valueTextColor = Color.rgb(11, 95, 165)
        dataSet.valueTextSize = 11f

        val data = BarData(dataSet)
        data.barWidth = 0.55f

        barChart.data = data
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.granularity = 1f
        xAxis.labelCount = 7
        xAxis.setDrawGridLines(false)

        barChart.axisRight.isEnabled = false
        barChart.axisLeft.axisMinimum = 0f

        barChart.invalidate()
    }

    /**
     * Returns weekly chart values.
     *
     * These values are currently sample values used
     * for visualising the weekly analytics chart.
     *
     * @param index Day index of the week.
     * @return Number of completed tasks.
     */
    private fun getDemoWeeklyValue(index: Int): Int {
        return when (index) {
            0 -> 2
            1 -> 3
            2 -> 1
            3 -> 4
            4 -> 1
            else -> 0
        }
    }

    /**
     * Generates a summary of the nearest upcoming deadlines.
     *
     * @param tasks Current list of user tasks.
     * @return Formatted upcoming deadline text.
     */
    private fun generateUpcomingDeadlineText(tasks: List<Task>): String {
        val upcoming = tasks
            .filter {
                !it.completed &&
                        ScheduleGenerator.calculateDaysRemaining(it.deadline) >= 0
            }
            .sortedBy {
                ScheduleGenerator.calculateDaysRemaining(it.deadline)
            }
            .take(3)

        if (upcoming.isEmpty()) {
            return getString(R.string.upcoming_deadlines_title) +
                    "\n\n" +
                    getString(R.string.no_upcoming_deadlines)
        }

        val text = StringBuilder()
        text.append(getString(R.string.upcoming_deadlines_title))
        text.append("\n\n")

        upcoming.forEach { task ->
            val days = ScheduleGenerator.calculateDaysRemaining(task.deadline)

            val dueText = when (days) {
                0L -> getString(R.string.due_today)
                1L -> getString(R.string.due_tomorrow)
                else -> getString(R.string.due_in_days, days.toInt())
            }

            text.append("• ${task.title}\n")
            text.append("  ${task.course} - $dueText\n\n")
        }

        return text.toString().trim()
    }
}