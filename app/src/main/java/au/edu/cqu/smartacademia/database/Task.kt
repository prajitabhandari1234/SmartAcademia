package au.edu.cqu.smartacademia.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.UUID

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    var userEmail: String,
    var title: String,
    var course: String,
    var deadline: String,
    var weight: Int,
    var estimatedHours: Int,
    var notes: String = "",
    var priorityScore: Int = 0,
    var completed: Boolean = false,
    var lat: Double = 0.0,
    var lon: Double = 0.0
) : Serializable