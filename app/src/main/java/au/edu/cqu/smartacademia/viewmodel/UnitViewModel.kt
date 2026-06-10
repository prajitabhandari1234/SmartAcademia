package au.edu.cqu.smartacademia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import au.edu.cqu.smartacademia.database.SmartAcademiaDatabase
import au.edu.cqu.smartacademia.database.Unit
import au.edu.cqu.smartacademia.database.UnitRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for managing university unit data.
 *
 * Connects the UI layer to UnitRepository and keeps
 * unit data lifecycle-aware.
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
     *
     * @param email Logged-in user email.
     * @return LiveData list of units.
     */
    fun getUnitsForUser(email: String): LiveData<List<Unit>> {
        return repository.getUnitsForUser(email)
    }

    /**
     * Inserts a new unit.
     *
     * @param unit Unit to insert.
     */
    fun insertUnit(unit: Unit) {
        viewModelScope.launch {
            repository.insertUnit(unit)
        }
    }

    /**
     * Updates an existing unit.
     *
     * @param unit Updated unit.
     */
    fun updateUnit(unit: Unit) {
        viewModelScope.launch {
            repository.updateUnit(unit)
        }
    }

    /**
     * Deletes a unit by ID.
     *
     * @param unitId Unit identifier.
     */
    fun deleteUnitById(unitId: String) {
        viewModelScope.launch {
            repository.deleteUnitById(unitId)
        }
    }
}