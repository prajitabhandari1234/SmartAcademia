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
 * - University units.
 * - Academic tasks and assignment information.
 */
@Database(
    entities = [User::class, Unit::class, Task::class],
    version = 3,
    exportSchema = false
)
abstract class SmartAcademiaDatabase : RoomDatabase() {

    /**
     * Provides access to User database operations.
     */
    abstract fun userDao(): UserDao

    /**
     * Provides access to Unit database operations.
     */
    abstract fun unitDao(): UnitDao

    /**
     * Provides access to Task database operations.
     */
    abstract fun taskDao(): TaskDao

    companion object {

        /**
         * Singleton instance of the Room database.
         */
        @Volatile
        private var INSTANCE: SmartAcademiaDatabase? = null

        /**
         * Returns the existing database instance.
         *
         * Creates the database if it does not already exist.
         *
         * fallbackToDestructiveMigration is used during active
         * development so schema changes do not crash the app.
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