package au.edu.cqu.smartacademia.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [User::class, Task::class],
    version = 1,
    exportSchema = false
)
abstract class SmartAcademiaDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: SmartAcademiaDatabase? = null

        fun getDatabase(context: Context): SmartAcademiaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartAcademiaDatabase::class.java,
                    "smartacademia_database"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}