package au.edu.cqu.smartacademia.database

import androidx.lifecycle.LiveData

/**
 * Repository responsible for managing Unit data.
 *
 * Acts as a bridge between UnitViewModel and UnitDao.
 */
class UnitRepository(
    private val unitDao: UnitDao
) {

    /**
     * Returns all units belonging to a user.
     *
     * @param email Logged-in user email.
     * @return LiveData list of units.
     */
    fun getUnitsForUser(email: String): LiveData<List<Unit>> {
        return unitDao.getUnitsForUser(email)
    }

    /**
     * Inserts a new unit.
     *
     * @param unit Unit to insert.
     */
    suspend fun insertUnit(unit: Unit) {
        unitDao.insertUnit(unit)
    }

    /**
     * Updates an existing unit.
     *
     * @param unit Updated unit.
     */
    suspend fun updateUnit(unit: Unit) {
        unitDao.updateUnit(unit)
    }

    /**
     * Retrieves a unit by ID.
     *
     * @param unitId Unit identifier.
     * @return Matching unit or null.
     */
    suspend fun getUnitById(unitId: String): Unit? {
        return unitDao.getUnitById(unitId)
    }

    /**
     * Retrieves a unit by unit code.
     *
     * @param email Logged-in user email.
     * @param unitCode Unit code.
     * @return Matching unit or null.
     */
    suspend fun getUnitByCode(
        email: String,
        unitCode: String
    ): Unit? {
        return unitDao.getUnitByCode(email, unitCode)
    }

    /**
     * Deletes a unit by ID.
     *
     * @param unitId Unit identifier.
     */
    suspend fun deleteUnitById(unitId: String) {
        unitDao.deleteUnitById(unitId)
    }
}