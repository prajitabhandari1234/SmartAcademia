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

/**
 * Activity responsible for displaying task locations on Google Maps.
 *
 * Supports Assignment 3 map requirements:
 * - Opens Google Maps Activity.
 * - Receives selected task as a serialised object.
 * - Centres map on the selected task location.
 * - Displays a marker at the task location.
 * - Shows task details on marker click.
 * - Uses zoom level 12 for selected task location.
 */
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var selectedTask: Task? = null
    private var googleMap: GoogleMap? = null
    private var taskLatLng: LatLng? = null

    private val locationPermissionRequestCode = 1001

    /**
     * Creates and initialises the map screen.
     *
     * Sets up:
     * - Back navigation.
     * - Current location button.
     * - Task location button.
     * - Google Map fragment.
     * - Selected task from Intent.
     */
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

        selectedTask =
            intent.getSerializableExtra("task") as? Task

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    /**
     * Called when the Google Map is ready.
     *
     * Displays the selected task marker and moves the map camera to the task location.
     *
     * @param map Ready GoogleMap instance.
     */
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        val task = selectedTask

        if (task != null && isValidLocation(task.lat, task.lon)) {
            taskLatLng = LatLng(task.lat, task.lon)

            val marker = googleMap?.addMarker(
                MarkerOptions()
                    .position(taskLatLng!!)
                    .title(task.title)
                    .snippet("${task.course} - ${task.deadline}")
            )

            marker?.showInfoWindow()

            googleMap?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    taskLatLng!!,
                    12f
                )
            )
        } else {
            Toast.makeText(
                this,
                getString(R.string.task_location_not_available),
                Toast.LENGTH_SHORT
            ).show()
        }

        enableMyLocationIfAllowed()
    }

    /**
     * Moves the map camera back to the selected task location.
     */
    private fun moveToTaskLocation() {
        if (taskLatLng == null) {
            Toast.makeText(
                this,
                getString(R.string.task_location_not_available),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        googleMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                taskLatLng!!,
                15f
            )
        )
    }

    /**
     * Enables the Google Maps current location layer
     * if location permission has been granted.
     */
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

    /**
     * Moves the map camera to the user's current GPS location.
     */
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
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                locationPermissionRequestCode
            )
            return
        }

        val fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng =
                        LatLng(location.latitude, location.longitude)

                    googleMap?.addMarker(
                        MarkerOptions()
                            .position(currentLatLng)
                            .title(getString(R.string.my_current_location))
                    )

                    googleMap?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            currentLatLng,
                            15f
                        )
                    )
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.current_location_not_available),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    getString(R.string.unable_get_location),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    /**
     * Handles the user's response to the location permission request.
     *
     * @param requestCode Permission request code.
     * @param permissions Requested permissions.
     * @param grantResults Permission result values.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == locationPermissionRequestCode) {
            if (
                grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                enableMyLocationIfAllowed()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.location_permission_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Validates map coordinates before displaying a marker.
     *
     * @param lat Latitude value.
     * @param lon Longitude value.
     * @return true if coordinates are valid, otherwise false.
     */
    private fun isValidLocation(
        lat: Double,
        lon: Double
    ): Boolean {
        return lat != 0.0 &&
                lon != 0.0 &&
                lat in -90.0..90.0 &&
                lon in -180.0..180.0
    }
}