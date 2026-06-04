package au.edu.cqu.smartacademia.model

/**
 * Data model used to decode task information
 * received from the remote JSON API.
 *
 * This model supports the Assignment 3
 * HTTP Data Fetching requirement by mapping
 * JSON task objects into Kotlin objects.
 *
 * Properties:
 * @property title Task title.
 * @property course Course code associated with the task.
 * @property deadline Due date and time.
 * @property weight Assessment weight percentage.
 * @property estimatedHours Estimated study hours required.
 * @property notes Additional task notes.
 * @property lat Latitude used for Google Maps integration.
 * @property lon Longitude used for Google Maps integration.
 */
data class TaskResponse(

    /** Task title. */
    val title: String,

    /** Course code. */
    val course: String,

    /** Task deadline. */
    val deadline: String,

    /** Assessment weight percentage. */
    val weight: Int,

    /** Estimated study hours required. */
    val estimatedHours: Int,

    /** Additional notes for the task. */
    val notes: String,

    /** Latitude for map marker location. */
    val lat: Double,

    /** Longitude for map marker location. */
    val lon: Double
)