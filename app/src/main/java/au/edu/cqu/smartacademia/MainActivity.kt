package au.edu.cqu.smartacademia

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import au.edu.cqu.smartacademia.fragments.AnalyticsFragment
import au.edu.cqu.smartacademia.fragments.DashboardFragment
import au.edu.cqu.smartacademia.fragments.ScheduleFragment
import au.edu.cqu.smartacademia.fragments.TaskListFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, DashboardFragment())
                .commit()
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_dashboard -> DashboardFragment()
                R.id.nav_tasks -> TaskListFragment()
                R.id.nav_schedule -> ScheduleFragment()
                R.id.nav_analytics -> AnalyticsFragment()
                else -> DashboardFragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()

            true
        }
    }
}