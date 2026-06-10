package au.edu.cqu.smartacademia.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.UUID

/**
 * Entity representing one university unit or course.
 *
 * Example:
 * COIT13234 - Mobile Software Development
 */
@Entity(tableName = "units")
data class CourseUnit(

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
     * Unit name.
     *
     * Example:
     * Mobile Software Development
     */
    var unitName: String,

    /**
     * Minimum percentage required to pass this unit.
     */
    var passMark: Int = 50

) : Serializable