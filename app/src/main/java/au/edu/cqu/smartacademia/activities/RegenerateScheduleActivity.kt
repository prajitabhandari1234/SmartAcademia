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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_regenerate_schedule)

        selectedDateKey = intent.getStringExtra("selected_date_key") ?: ""
        editingStartTime = intent.getStringExtra("editing_start_time")

        planNameEditText = findViewById(R.id.planNameEditText)
        studyHoursEditText = findViewById(R.id.studyHoursEditText)
        startTimeEditText = findViewById(R.id.startTimeEditText)
        extraActivityEditText = findViewById(R.id.extraActivityEditText)
        locationNameEditText = findViewById(R.id.locationNameEditText)
        latitudeEditText = findViewById(R.id.latitudeEditText)
        longitudeEditText = findViewById(R.id.longitudeEditText)
        generatedPlanTextView = findViewById(R.id.generatedPlanTextView)

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

    private fun saveOrOverwritePlan() {
        val planName = planNameEditText.text.toString().trim()
        val hours = studyHoursEditText.text.toString().trim()
        val startTime = startTimeEditText.text.toString().trim()
        val activity = extraActivityEditText.text.toString().trim()
        val locationName = locationNameEditText.text.toString().trim()
        val latitude = latitudeEditText.text.toString().trim()
        val longitude = longitudeEditText.text.toString().trim()

        if (planName.isEmpty() || hours.isEmpty() || startTime.isEmpty()) {
            Toast.makeText(this, "Please complete plan name, study hours and start time", Toast.LENGTH_SHORT).show()
            return
        }

        val newPlan = JSONObject().apply {
            put("planName", planName)
            put("hours", hours)
            put("startTime", startTime)
            put("activity", if (activity.isEmpty()) "Break / Revision" else activity)
            put("locationName", if (locationName.isEmpty()) "Not Added" else locationName)
            put("latitude", if (latitude.isEmpty()) "Not Added" else latitude)
            put("longitude", if (longitude.isEmpty()) "Not Added" else longitude)
        }

        val oldPlans = loadPlans()
        val updatedPlans = JSONArray()

        for (i in 0 until oldPlans.length()) {
            val oldPlan = oldPlans.getJSONObject(i)
            val oldTime = oldPlan.getString("startTime")

            if (oldTime != startTime && oldTime != editingStartTime) {
                updatedPlans.put(oldPlan)
            }
        }

        updatedPlans.put(newPlan)

        getSharedPreferences("smartacademia_schedule", MODE_PRIVATE)
            .edit()
            .putString(selectedDateKey, updatedPlans.toString())
            .apply()

        generatedPlanTextView.text = formatPlan(newPlan)
        Toast.makeText(this, "Plan saved successfully", Toast.LENGTH_SHORT).show()
    }

    private fun deleteCurrentPlan() {
        val timeToDelete = editingStartTime ?: startTimeEditText.text.toString().trim()

        if (timeToDelete.isEmpty()) {
            Toast.makeText(this, "Enter start time to delete a plan", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Delete Plan")
            .setMessage("Delete the plan at $timeToDelete?")
            .setPositiveButton("Delete") { _, _ ->
                val oldPlans = loadPlans()
                val updatedPlans = JSONArray()

                for (i in 0 until oldPlans.length()) {
                    val plan = oldPlans.getJSONObject(i)
                    if (plan.getString("startTime") != timeToDelete) {
                        updatedPlans.put(plan)
                    }
                }

                getSharedPreferences("smartacademia_schedule", MODE_PRIVATE)
                    .edit()
                    .putString(selectedDateKey, updatedPlans.toString())
                    .apply()

                Toast.makeText(this, "Plan deleted", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadExistingPlanForEdit() {
        val editTime = editingStartTime ?: return
        val plans = loadPlans()

        for (i in 0 until plans.length()) {
            val plan = plans.getJSONObject(i)

            if (plan.getString("startTime") == editTime) {
                planNameEditText.setText(plan.getString("planName"))
                studyHoursEditText.setText(plan.getString("hours"))
                startTimeEditText.setText(plan.getString("startTime"))
                extraActivityEditText.setText(plan.getString("activity"))
                locationNameEditText.setText(plan.getString("locationName"))
                latitudeEditText.setText(plan.getString("latitude"))
                longitudeEditText.setText(plan.getString("longitude"))
                generatedPlanTextView.text = formatPlan(plan)
                break
            }
        }
    }

    private fun loadPlans(): JSONArray {
        val text = getSharedPreferences("smartacademia_schedule", MODE_PRIVATE)
            .getString(selectedDateKey, "[]")

        return JSONArray(text)
    }

    private fun formatPlan(plan: JSONObject): String {
        return "Plan Name: ${plan.getString("planName")}\n\n" +
                "Start Time: ${plan.getString("startTime")}\n" +
                "Study Duration: ${plan.getString("hours")} hour(s)\n\n" +
                "Activity:\n${plan.getString("activity")}\n\n" +
                "Location: ${plan.getString("locationName")}\n" +
                "Latitude: ${plan.getString("latitude")}\n" +
                "Longitude: ${plan.getString("longitude")}"
    }
}