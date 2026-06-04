package au.edu.cqu.smartacademia.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import au.edu.cqu.smartacademia.R
import org.json.JSONArray
import org.json.JSONObject

/**
 * Activity responsible for creating, editing,
 * deleting and previewing study schedule plans.
 *
 * Supports SmartAcademia's schedule planning feature
 * by allowing users to manage daily study plans and
 * store them locally using SharedPreferences.
 */
class RegenerateScheduleActivity : AppCompatActivity() {

    private lateinit var planNameEditText: EditText
    private lateinit var studyHoursEditText: EditText
    private lateinit var startTimeEditText: EditText
    private lateinit var extraActivityEditText: EditText
    private lateinit var locationNameEditText: EditText
    private lateinit var latitudeEditText: EditText
    private lateinit var longitudeEditText: EditText
    private lateinit var generatedPlanTextView: TextView
    private var selectedDateKey: String = ""
    private var editingStartTime: String? = null

    /**
     * Creates and initialises the schedule planner screen.
     *
     * Loads existing plan data when editing and
     * configures button actions for save, delete
     * and navigation.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_regenerate_schedule)

        selectedDateKey =
            intent.getStringExtra("selected_date_key") ?: ""

        editingStartTime =
            intent.getStringExtra("editing_start_time")

        planNameEditText =
            findViewById(R.id.planNameEditText)

        studyHoursEditText =
            findViewById(R.id.studyHoursEditText)

        startTimeEditText =
            findViewById(R.id.startTimeEditText)

        extraActivityEditText =
            findViewById(R.id.extraActivityEditText)

        locationNameEditText =
            findViewById(R.id.locationNameEditText)

        latitudeEditText =
            findViewById(R.id.latitudeEditText)

        longitudeEditText =
            findViewById(R.id.longitudeEditText)

        generatedPlanTextView =
            findViewById(R.id.generatedPlanTextView)

        findViewById<Button>(R.id.generateButton).setOnClickListener {
            saveOrOverwritePlan()
        }

        findViewById<Button>(R.id.deletePlanButton).setOnClickListener {
            deleteCurrentPlan()
        }

        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish()
        }

        loadExistingPlanForEdit()
    }

    /**
     * Saves a new study plan or updates an existing one.
     *
     * Validates required fields before storing
     * plan information in SharedPreferences.
     */
    private fun saveOrOverwritePlan() {
        val planName =
            planNameEditText.text.toString().trim()

        val hours =
            studyHoursEditText.text.toString().trim()

        val startTime =
            startTimeEditText.text.toString().trim()

        val activity =
            extraActivityEditText.text.toString().trim()

        val locationName =
            locationNameEditText.text.toString().trim()

        val latitude =
            latitudeEditText.text.toString().trim()

        val longitude =
            longitudeEditText.text.toString().trim()

        if (
            planName.isEmpty() ||
            hours.isEmpty() ||
            startTime.isEmpty()
        ) {
            Toast.makeText(
                this,
                getString(R.string.complete_plan_required_fields),
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val newPlan = JSONObject().apply {
            put("planName", planName)
            put("hours", hours)
            put("startTime", startTime)

            put(
                "activity",
                if (activity.isEmpty()) {
                    getString(R.string.break_revision)
                } else {
                    activity
                }
            )

            put(
                "locationName",
                if (locationName.isEmpty()) {
                    getString(R.string.not_added)
                } else {
                    locationName
                }
            )

            put(
                "latitude",
                if (latitude.isEmpty()) {
                    getString(R.string.not_added)
                } else {
                    latitude
                }
            )

            put(
                "longitude",
                if (longitude.isEmpty()) {
                    getString(R.string.not_added)
                } else {
                    longitude
                }
            )
        }

        val oldPlans = loadPlans()
        val updatedPlans = JSONArray()

        for (i in 0 until oldPlans.length()) {
            val oldPlan = oldPlans.getJSONObject(i)
            val oldTime = oldPlan.getString("startTime")

            if (
                oldTime != startTime &&
                oldTime != editingStartTime
            ) {
                updatedPlans.put(oldPlan)
            }
        }

        updatedPlans.put(newPlan)

        getSharedPreferences(
            "smartacademia_schedule",
            MODE_PRIVATE
        )
            .edit()
            .putString(
                selectedDateKey,
                updatedPlans.toString()
            )
            .apply()

        generatedPlanTextView.text =
            formatPlan(newPlan)

        Toast.makeText(
            this,
            getString(R.string.plan_saved_successfully),
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Deletes the selected study plan after
     * user confirmation.
     */
    private fun deleteCurrentPlan() {
        val timeToDelete =
            editingStartTime
                ?: startTimeEditText.text.toString().trim()

        if (timeToDelete.isEmpty()) {
            Toast.makeText(
                this,
                getString(R.string.enter_start_time_delete),
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_plan))
            .setMessage(
                getString(
                    R.string.delete_plan_message,
                    timeToDelete
                )
            )
            .setPositiveButton(
                getString(R.string.delete_button)
            ) { _, _ ->
                val oldPlans = loadPlans()
                val updatedPlans = JSONArray()

                for (i in 0 until oldPlans.length()) {
                    val plan = oldPlans.getJSONObject(i)

                    if (plan.getString("startTime") != timeToDelete) {
                        updatedPlans.put(plan)
                    }
                }

                getSharedPreferences(
                    "smartacademia_schedule",
                    MODE_PRIVATE
                )
                    .edit()
                    .putString(
                        selectedDateKey,
                        updatedPlans.toString()
                    )
                    .apply()

                Toast.makeText(
                    this,
                    getString(R.string.plan_deleted_simple),
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }
            .setNegativeButton(
                getString(R.string.cancel_button),
                null
            )
            .show()
    }

    /**
     * Loads an existing study plan into the form
     * when the user chooses to edit a plan.
     */
    private fun loadExistingPlanForEdit() {
        val editTime = editingStartTime ?: return
        val plans = loadPlans()

        for (i in 0 until plans.length()) {
            val plan = plans.getJSONObject(i)

            if (plan.getString("startTime") == editTime) {
                planNameEditText.setText(
                    plan.getString("planName")
                )

                studyHoursEditText.setText(
                    plan.getString("hours")
                )

                startTimeEditText.setText(
                    plan.getString("startTime")
                )

                extraActivityEditText.setText(
                    plan.getString("activity")
                )

                locationNameEditText.setText(
                    plan.getString("locationName")
                )

                latitudeEditText.setText(
                    plan.getString("latitude")
                )

                longitudeEditText.setText(
                    plan.getString("longitude")
                )

                generatedPlanTextView.text =
                    formatPlan(plan)

                break
            }
        }
    }

    /**
     * Retrieves all saved plans for the selected date.
     *
     * @return JSONArray containing saved plans.
     */
    private fun loadPlans(): JSONArray {
        val text = getSharedPreferences(
            "smartacademia_schedule",
            MODE_PRIVATE
        )
            .getString(
                selectedDateKey,
                "[]"
            )

        return JSONArray(text)
    }

    /**
     * Converts plan JSON data into a readable
     * preview format for display.
     *
     * @param plan JSON object representing a study plan.
     * @return Formatted plan summary text.
     */
    private fun formatPlan(plan: JSONObject): String {
        return getString(
            R.string.plan_name_label,
            plan.getString("planName")
        ) + "\n\n" +
                getString(
                    R.string.start_time_label,
                    plan.getString("startTime")
                ) + "\n" +
                getString(
                    R.string.study_duration_label,
                    plan.getString("hours")
                ) + "\n\n" +
                getString(R.string.activity_label) + "\n" +
                plan.getString("activity") + "\n\n" +
                getString(
                    R.string.location_label,
                    plan.getString("locationName")
                ) + "\n" +
                getString(
                    R.string.latitude_label,
                    plan.getString("latitude")
                ) + "\n" +
                getString(
                    R.string.longitude_label,
                    plan.getString("longitude")
                )
    }
}