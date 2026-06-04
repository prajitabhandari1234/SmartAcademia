package au.edu.cqu.smartacademia.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.UUID

/**
 * Task entity used by the SmartAcademia Room database.
 *
 * Stores assignment, study, and academic planning information
 * for each registered user.
 *
 * Implements Serializable so task objects can be passed
 * between Activities using Intent extras.
 */
@Entity(tableName = "tasks")
data class Task(

    /**
     * Unique task identifier.
     */
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    /**
     * Email of the user who owns the task.
     */
    var userEmail: String,

    /**
     * Task or assignment title.
     */
    var title: String,

    /**
     * Course code or subject name.
     */
    var course: String,

    /**
     * Due date and time.
     *
     * Example:
     * 2026-06-05 23:45
     */
    var deadline: String,

    /**
     * Assessment weight percentage.
     */
    var weight: Int,

    /**
     * Estimated study hours required.
     */
    var estimatedHours: Int,

    /**
     * Additional notes entered by the user.
     */
    var notes: String = "",

    /**
     * Calculated priority score used for
     * scheduling and task sorting.
     */
    var priorityScore: Int = 0,

    /**
     * Indicates whether the task
     * has been completed.
     */
    var completed: Boolean = false,

    /**
     * Latitude coordinate used by Google Maps.
     */
    var lat: Double = 0.0,

    /**
     * Longitude coordinate used by Google Maps.
     */
    var lon: Double = 0.0

) : Serializable