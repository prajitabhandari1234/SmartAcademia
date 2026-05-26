package au.edu.cqu.smartacademia.activities

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import au.edu.cqu.smartacademia.R
import au.edu.cqu.smartacademia.database.Task
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var selectedTask: Task? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val topBar = findViewById<View>(R.id.topBar)
        val backButton = findViewById<TextView>(R.id.backButton)

        topBar.setOnClickListener {
            finish()
        }

        backButton.setOnClickListener {
            finish()
        }

        selectedTask = intent.getSerializableExtra("task") as? Task

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val task = selectedTask ?: return
        val location = LatLng(task.lat, task.lon)

        googleMap.addMarker(
            MarkerOptions()
                .position(location)
                .title(task.title)
                .snippet("${task.course} - ${task.deadline}")
        )

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12f))
    }
}