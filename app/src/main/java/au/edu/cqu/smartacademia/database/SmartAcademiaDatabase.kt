package au.edu.cqu.smartacademia.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Main Room database for SmartAcademia.
 *
 * Stores users, units and academic tasks.
 */
@Database(
    entities = [User::class, CourseUnit::class, Task::class],
    version = 4,
    exportSchema = false
)
abstract class SmartAcademiaDatabase : RoomDatabase() {

    /**
     * Provides access to user operations.
     */
    abstract fun userDao(): UserDao

    /**
     * Provides access to unit operations.
     */
    abstract fun unitDao(): UnitDao

    /**
     * Provides access to task operations.
     */
    abstract fun taskDao(): TaskDao

    companion object {

        @Volatile
        private var INSTANCE: SmartAcademiaDatabase? = null

        /**
         * Returns the singleton database instance.
         */
        fun getDatabase(context: Context): SmartAcademiaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartAcademiaDatabase::class.java,
                    "smartacademia_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}