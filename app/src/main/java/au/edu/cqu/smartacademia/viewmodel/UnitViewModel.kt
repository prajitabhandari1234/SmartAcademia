package au.edu.cqu.smartacademia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import au.edu.cqu.smartacademia.database.CourseUnit
import au.edu.cqu.smartacademia.database.SmartAcademiaDatabase
import au.edu.cqu.smartacademia.database.UnitRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for managing university units.
 */
class UnitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UnitRepository

    init {
        val unitDao = SmartAcademiaDatabase
            .getDatabase(application)
            .unitDao()

        repository = UnitRepository(unitDao)
    }

    /**
     * Returns all units for the logged-in user.
     */
    fun getUnitsForUser(email: String): LiveData<List<CourseUnit>> {
        return repository.getUnitsForUser(email)
    }

    /**
     * Inserts a new unit.
     */
    fun insertUnit(unit: CourseUnit) {
        viewModelScope.launch {
            repository.insertUnit(unit)
        }
    }

    /**
     * Updates an existing unit.
     */
    fun updateUnit(unit: CourseUnit) {
        viewModelScope.launch {
            repository.updateUnit(unit)
        }
    }

    /**
     * Loads one unit by ID.
     */
    fun getUnitById(
        unitId: String,
        result: (CourseUnit?) -> kotlin.Unit
    ) {
        viewModelScope.launch {
            result(repository.getUnitById(unitId))
        }
    }

    /**
     * Deletes a unit by ID.
     */
    fun deleteUnitById(unitId: String) {
        viewModelScope.launch {
            repository.deleteUnitById(unitId)
        }
    }
}