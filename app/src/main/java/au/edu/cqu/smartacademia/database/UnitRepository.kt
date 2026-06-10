package au.edu.cqu.smartacademia.database

import androidx.lifecycle.LiveData

/**
 * Repository for managing university units.
 */
class UnitRepository(
    private val unitDao: UnitDao
) {

    /**
     * Returns all units for a user.
     */
    fun getUnitsForUser(email: String): LiveData<List<CourseUnit>> {
        return unitDao.getUnitsForUser(email)
    }

    /**
     * Inserts a unit.
     */
    suspend fun insertUnit(unit: CourseUnit) {
        unitDao.insertUnit(unit)
    }

    /**
     * Updates a unit.
     */
    suspend fun updateUnit(unit: CourseUnit) {
        unitDao.updateUnit(unit)
    }

    /**
     * Returns one unit by ID.
     */
    suspend fun getUnitById(unitId: String): CourseUnit? {
        return unitDao.getUnitById(unitId)
    }

    /**
     * Deletes one unit by ID.
     */
    suspend fun deleteUnitById(unitId: String) {
        unitDao.deleteUnitById(unitId)
    }
}