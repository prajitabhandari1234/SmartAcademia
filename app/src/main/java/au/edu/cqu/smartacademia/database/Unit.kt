package au.edu.cqu.smartacademia.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.UUID

/**
 * Unit entity used by the SmartAcademia Room database.
 *
 * Represents one university unit or subject, such as
 * COIT13234 Mobile Software Development.
 *
 * Each unit can contain multiple assessment tasks.
 */
@Entity(tableName = "units")
data class Unit(

    /**
     * Unique unit identifier.
     */
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    /**
     * Email address of the user who owns this unit.
     */
    var userEmail: String,

    /**
     * Unit code.
     *
     * Example:
     * COIT13234
     */
    var unitCode: String,

    /**
     * Full unit name.
     *
     * Example:
     * Mobile Software Development
     */
    var unitName: String,

    /**
     * Minimum percentage required to pass the unit.
     */
    var passMark: Int = 50

) : Serializable