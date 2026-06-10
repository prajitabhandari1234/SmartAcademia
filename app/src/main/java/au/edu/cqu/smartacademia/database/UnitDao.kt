package au.edu.cqu.smartacademia.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * Data Access Object for managing Unit records.
 */
@Dao
interface UnitDao {

    /**
     * Inserts a unit into the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: Unit)

    /**
     * Updates an existing unit.
     */
    @Update
    suspend fun updateUnit(unit: Unit)

    /**
     * Returns all units for the logged-in user.
     *
     * @param email Logged-in user email.
     * @return LiveData list of units.
     */
    @Query(
        "SELECT * FROM units " +
                "WHERE userEmail = :email " +
                "ORDER BY unitCode ASC"
    )
    fun getUnitsForUser(email: String): LiveData<List<Unit>>

    /**
     * Finds a unit by its unique ID.
     *
     * @param unitId Unit identifier.
     * @return Matching unit or null.
     */
    @Query("SELECT * FROM units WHERE id = :unitId LIMIT 1")
    suspend fun getUnitById(unitId: String): Unit?

    /**
     * Finds a unit using its unit code.
     *
     * @param email Logged-in user email.
     * @param unitCode Unit code.
     * @return Matching unit or null.
     */
    @Query(
        "SELECT * FROM units " +
                "WHERE userEmail = :email " +
                "AND unitCode = :unitCode " +
                "LIMIT 1"
    )
    suspend fun getUnitByCode(
        email: String,
        unitCode: String
    ): Unit?

    /**
     * Deletes a unit by ID.
     *
     * @param unitId Unit identifier.
     */
    @Query("DELETE FROM units WHERE id = :unitId")
    suspend fun deleteUnitById(unitId: String)
}