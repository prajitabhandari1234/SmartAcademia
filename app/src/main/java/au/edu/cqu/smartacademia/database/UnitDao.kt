package au.edu.cqu.smartacademia.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * Data Access Object for managing university units.
 */
@Dao
interface UnitDao {

    /**
     * Inserts a new unit.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: CourseUnit)

    /**
     * Updates an existing unit.
     */
    @Update
    suspend fun updateUnit(unit: CourseUnit)

    /**
     * Returns all units for the logged-in user.
     */
    @Query(
        "SELECT * FROM units " +
                "WHERE userEmail = :email " +
                "ORDER BY unitCode ASC"
    )
    fun getUnitsForUser(email: String): LiveData<List<CourseUnit>>

    /**
     * Returns one unit by ID.
     */
    @Query("SELECT * FROM units WHERE id = :unitId LIMIT 1")
    suspend fun getUnitById(unitId: String): CourseUnit?

    /**
     * Deletes one unit by ID.
     */
    @Query("DELETE FROM units WHERE id = :unitId")
    suspend fun deleteUnitById(unitId: String)
}