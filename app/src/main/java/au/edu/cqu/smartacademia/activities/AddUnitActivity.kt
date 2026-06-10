package au.edu.cqu.smartacademia.activities

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import au.edu.cqu.smartacademia.R
import au.edu.cqu.smartacademia.database.CourseUnit
import au.edu.cqu.smartacademia.viewmodel.TaskViewModel
import au.edu.cqu.smartacademia.viewmodel.UnitViewModel
import java.util.UUID

/**
 * Activity used to add, edit and delete university units.
 */
class AddUnitActivity : AppCompatActivity() {

    private lateinit var unitViewModel: UnitViewModel
    private lateinit var taskViewModel: TaskViewModel

    private lateinit var unitCodeEditText: EditText
    private lateinit var unitNameEditText: EditText
    private lateinit var passMarkEditText: EditText
    private lateinit var saveUnitButton: Button
    private lateinit var deleteUnitButton: Button
    private lateinit var cancelUnitButton: Button

    private var userEmail: String = ""
    private var editingUnitId: String? = null

    /**
     * Creates the Add/Edit Unit screen.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_unit)

        unitViewModel = ViewModelProvider(this)[UnitViewModel::class.java]
        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        userEmail = getSharedPreferences("smartacademia_session", MODE_PRIVATE)
            .getString("email", "") ?: ""

        editingUnitId = intent.getStringExtra("unit_id")

        unitCodeEditText = findViewById(R.id.unitCodeEditText)
        unitNameEditText = findViewById(R.id.unitNameEditText)
        passMarkEditText = findViewById(R.id.passMarkEditText)
        saveUnitButton = findViewById(R.id.saveUnitButton)
        deleteUnitButton = findViewById(R.id.deleteUnitButton)
        cancelUnitButton = findViewById(R.id.cancelUnitButton)

        if (editingUnitId == null) {
            deleteUnitButton.visibility = View.GONE
            passMarkEditText.setText("50")
        } else {
            saveUnitButton.text = getString(R.string.save_changes_button)
            loadUnitForEditing(editingUnitId!!)
        }

        saveUnitButton.setOnClickListener {
            saveOrUpdateUnit()
        }

        deleteUnitButton.setOnClickListener {
            confirmDeleteUnit()
        }

        cancelUnitButton.setOnClickListener {
            finish()
        }
    }

    /**
     * Loads an existing unit into the form.
     */
    private fun loadUnitForEditing(unitId: String) {
        unitViewModel.getUnitById(unitId) { unit ->
            if (unit != null) {
                unitCodeEditText.setText(unit.unitCode)
                unitNameEditText.setText(unit.unitName)
                passMarkEditText.setText(unit.passMark.toString())
            }
        }
    }

    /**
     * Saves a new unit or updates an existing unit.
     */
    private fun saveOrUpdateUnit() {
        val unitCode = unitCodeEditText.text.toString().trim().uppercase()
        val unitName = unitNameEditText.text.toString().trim()
        val passMarkText = passMarkEditText.text.toString().trim()

        if (
            unitCode.isEmpty() ||
            unitName.isEmpty() ||
            passMarkText.isEmpty()
        ) {
            Toast.makeText(
                this,
                getString(R.string.empty_fields_message),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val passMark = passMarkText.toIntOrNull()

        if (passMark == null || passMark !in 1..100) {
            Toast.makeText(
                this,
                getString(R.string.invalid_pass_mark_message),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val unit = CourseUnit(
            id = editingUnitId ?: UUID.randomUUID().toString(),
            userEmail = userEmail,
            unitCode = unitCode,
            unitName = unitName,
            passMark = passMark
        )

        if (editingUnitId == null) {
            unitViewModel.insertUnit(unit)
            Toast.makeText(
                this,
                getString(R.string.unit_saved_message),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            unitViewModel.updateUnit(unit)
            Toast.makeText(
                this,
                getString(R.string.unit_updated_message),
                Toast.LENGTH_SHORT
            ).show()
        }

        finish()
    }

    /**
     * Confirms deletion before deleting a unit.
     */
    private fun confirmDeleteUnit() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_unit_title))
            .setMessage(getString(R.string.delete_unit_message))
            .setPositiveButton(getString(R.string.delete_button)) { _, _ ->
                editingUnitId?.let { unitId ->
                    taskViewModel.deleteTasksForUnit(unitId)
                    unitViewModel.deleteUnitById(unitId)

                    Toast.makeText(
                        this,
                        getString(R.string.unit_deleted_message),
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()
                }
            }
            .setNegativeButton(getString(R.string.cancel_button), null)
            .show()
    }
}