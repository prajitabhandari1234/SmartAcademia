package au.edu.cqu.smartacademia.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Main Room database for SmartAcademia.
 *
 * Stores:
 * - User accounts and authentication data.
 * - Academic tasks and assignment information.
 */
@Database(
    entities = [User::class, Task::class],
    version = 2,
    exportSchema = false
)
abstract class SmartAcademiaDatabase : RoomDatabase() {

    /**
     * Provides access to User database operations.
     */
    abstract fun userDao(): UserDao

    /**
     * Provides access to Task database operations.
     */
    abstract fun taskDao(): TaskDao

    companion object {

        /**
         * Singleton instance of the Room database.
         *
         * Volatile ensures changes made by one thread
         * are visible to all other threads immediately.
         */
        @Volatile
        private var INSTANCE: SmartAcademiaDatabase? = null

        /**
         * Returns the existing database instance.
         * Creates the database if it does not already exist.
         *
         * This prevents multiple database instances from
         * being created during application execution.
         */
        fun getDatabase(context: Context): SmartAcademiaDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartAcademiaDatabase::class.java,
                    "smartacademia_database"
                )
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}