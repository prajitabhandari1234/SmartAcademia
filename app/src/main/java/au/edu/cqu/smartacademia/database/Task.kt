package au.edu.cqu.smartacademia.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.UUID

/**
 * Task entity used by the SmartAcademia Room database.
 *
 * Stores assignment, study planning, scheduling,
 * completion status and location information
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
     * Email address of the user who owns the task.
     */
    var userEmail: String,

    /**
     * Task or assessment title.
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
     * Assessment weighting percentage.
     */
    var weight: Int,

    /**
     * Estimated study hours required to complete the task.
     */
    var estimatedHours: Int,

    /**
     * Additional notes entered by the user.
     */
    var notes: String = "",

    /**
     * Calculated priority score used by the
     * Smart Scheduling feature.
     */
    var priorityScore: Int = 0,

    /**
     * Indicates whether the task has been completed.
     */
    var completed: Boolean = false,

    /**
     * Human-readable location entered by the user.
     *
     * Example:
     * CQU Sydney
     * University of Sydney
     */
    var locationName: String = "",

    /**
     * Latitude coordinate generated from the location name.
     *
     * Used by Google Maps when displaying task locations.
     */
    var lat: Double = 0.0,

    /**
     * Longitude coordinate generated from the location name.
     *
     * Used by Google Maps when displaying task locations.
     */
    var lon: Double = 0.0

) : Serializable