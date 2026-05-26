package au.edu.cqu.smartacademia.model

data class TaskResponse(
    val title: String,
    val course: String,
    val deadline: String,
    val weight: Int,
    val estimatedHours: Int,
    val notes: String,
    val lat: Double,
    val lon: Double
)