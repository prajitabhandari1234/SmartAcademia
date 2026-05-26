package au.edu.cqu.smartacademia.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import au.edu.cqu.smartacademia.R
import au.edu.cqu.smartacademia.database.Task
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var selectedTask: Task? = null
    private var googleMap: GoogleMap? = null
    private var taskLatLng: LatLng? = null

    private val locationPermissionRequestCode = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        findViewById<View>(R.id.topBar).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.backButton).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.myLocationButton).setOnClickListener {
            moveToCurrentLocation()
        }

        findViewById<Button>(R.id.taskLocationButton).setOnClickListener {
            moveToTaskLocation()
        }

        selectedTask = intent.getSerializableExtra("task") as? Task

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        val task = selectedTask
        if (task != null) {
            taskLatLng = LatLng(task.lat, task.lon)

            googleMap?.addMarker(
                MarkerOptions()
                    .position(taskLatLng!!)
                    .title(task.title)
                    .snippet("${task.course} - ${task.deadline}")
            )

            googleMap?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(taskLatLng!!, 12f)
            )
        }

        enableMyLocationIfAllowed()
    }

    private fun moveToTaskLocation() {
        if (taskLatLng == null) {
            Toast.makeText(this, "Task location not available.", Toast.LENGTH_SHORT).show()
            return
        }

        googleMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(taskLatLng!!, 15f)
        )
    }

    private fun enableMyLocationIfAllowed() {
        val fineLocationGranted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineLocationGranted || coarseLocationGranted) {
            googleMap?.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                locationPermissionRequestCode
            )
        }
    }

    private fun moveToCurrentLocation() {
        val fineLocationGranted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineLocationGranted && !coarseLocationGranted) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                locationPermissionRequestCode
            )
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)

                    googleMap?.addMarker(
                        MarkerOptions()
                            .position(currentLatLng)
                            .title("My Current Location")
                    )

                    googleMap?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f)
                    )
                } else {
                    Toast.makeText(this, "Current location not available. Try again.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Unable to get current location.", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == locationPermissionRequestCode) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                enableMyLocationIfAllowed()
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}