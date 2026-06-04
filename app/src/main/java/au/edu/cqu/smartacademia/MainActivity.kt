package au.edu.cqu.smartacademia

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import au.edu.cqu.smartacademia.fragments.AnalyticsFragment
import au.edu.cqu.smartacademia.fragments.DashboardFragment
import au.edu.cqu.smartacademia.fragments.ScheduleFragment
import au.edu.cqu.smartacademia.fragments.TaskListFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Main activity of the SmartAcademia application.
 *
 * This activity acts as the container for all primary
 * application fragments and provides navigation using
 * BottomNavigationView.
 *
 * Features:
 * - Dashboard screen
 * - Task management screen
 * - Schedule planner screen
 * - Analytics screen
 *
 * Supports Assignment 3 requirements by providing
 * a multi-screen application with intuitive navigation.
 */
class MainActivity : AppCompatActivity() {

    /**
     * Bottom navigation component used to switch
     * between application screens.
     */
    private lateinit var bottomNavigationView: BottomNavigationView

    /**
     * Creates and initialises the main application screen.
     *
     * Sets up:
     * - Bottom navigation
     * - Default dashboard screen
     * - Fragment navigation handling
     *
     * @param savedInstanceState Previously saved state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView =
            findViewById(R.id.bottomNavigationView)

        // Load Dashboard as the default screen.
        if (savedInstanceState == null) {

            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    DashboardFragment()
                )
                .commit()
        }

        // Handle navigation item selection.
        bottomNavigationView.setOnItemSelectedListener { item ->

            val fragment = when (item.itemId) {

                R.id.nav_dashboard ->
                    DashboardFragment()

                R.id.nav_tasks ->
                    TaskListFragment()

                R.id.nav_schedule ->
                    ScheduleFragment()

                R.id.nav_analytics ->
                    AnalyticsFragment()

                else ->
                    DashboardFragment()
            }

            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    fragment
                )
                .commit()

            true
        }
    }
}