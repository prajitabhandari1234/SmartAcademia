package au.edu.cqu.smartacademia.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
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
 * - Opens a Google Maps Activity.
 * - Receives the selected task as a serialised object.
 * - Centres the map on the selected task location.
 * - Displays a marker at the task location.
 * - Shows task details above the marker.
 * - Uses zoom level 12 for the selected task location.
 *
 * The purple top bar acts as a large back-navigation target.
 * Tapping anywhere on the bar returns the user to the previous screen.
 */
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var selectedTask: Task? = null
    private var googleMap: GoogleMap? = null
    private var taskLatLng: LatLng? = null

    private val locationPermissionRequestCode = 1001

    /**
     * Creates and initialises the map screen.
     *
     * This method:
     * - Sets the layout.
     * - Makes the full top bar clickable for back navigation.
     * - Configures My Location and Task Location buttons.
     * - Retrieves the selected task from the Intent.
     * - Loads the Google Map fragment asynchronously.
     *
     * @param savedInstanceState Previous activity state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        setupTopBarNavigation()
        setupMapButtons()
        loadSelectedTask()
        initialiseMapFragment()
    }

    /**
     * Makes the entire purple top bar act as a back button.
     *
     * This improves usability because users can tap the arrow,
     * title, or empty purple area to return to the previous screen.
     */
    private fun setupTopBarNavigation() {
        findViewById<View>(R.id.topBar).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    /**
     * Configures the floating map action buttons.
     *
     * My Location moves the map to the user's current GPS location.
     * Task Location moves the map back to the selected task marker.
     */
    private fun setupMapButtons() {
        findViewById<Button>(R.id.myLocationButton).setOnClickListener {
            moveToCurrentLocation()
        }

        findViewById<Button>(R.id.taskLocationButton).setOnClickListener {
            moveToTaskLocation()
        }
    }

    /**
     * Retrieves the selected task passed from the task list screen.
     *
     * The task is passed as a serialised object through the Intent.
     */
    private fun loadSelectedTask() {
        selectedTask =
            intent.getSerializableExtra("task") as? Task
    }

    /**
     * Finds and initialises the SupportMapFragment.
     *
     * The map is loaded asynchronously and returned through onMapReady().
     */
    private fun initialiseMapFragment() {
        val mapFragment =
            supportFragmentManager.findFragmentById(
                R.id.map
            ) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    /**
     * Called when the Google Map is ready.
     *
     * Displays the selected task marker and moves the map camera
     * to the task location when valid coordinates are available.
     *
     * @param map Ready GoogleMap instance.
     */
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        displaySelectedTaskLocation()
        enableMyLocationIfAllowed()
    }

    /**
     * Displays the selected task location on the map.
     *
     * A marker is added at the task coordinates. The marker title
     * displays the task title, and the snippet displays course and
     * deadline details.
     */
    private fun displaySelectedTaskLocation() {
        val task =
            selectedTask

        if (task != null && isValidLocation(task.lat, task.lon)) {
            taskLatLng =
                LatLng(task.lat, task.lon)

            val marker =
                googleMap?.addMarker(
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
     * Enables the Google Maps current-location layer
     * if fine or coarse location permission has been granted.
     *
     * If permission has not been granted, the user is prompted
     * using Android runtime permission handling.
     */
    private fun enableMyLocationIfAllowed() {
        val fineLocationGranted =
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted =
            ActivityCompat.checkSelfPermission(
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
     *
     * If location permission is missing, permission is requested first.
     * If the current location is unavailable, a message is shown.
     */
    private fun moveToCurrentLocation() {
        val fineLocationGranted =
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted =
            ActivityCompat.checkSelfPermission(
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
                        LatLng(
                            location.latitude,
                            location.longitude
                        )

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
     * @param requestCode Permission request identifier.
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
     * Coordinates are valid only when:
     * - Latitude is not 0.0.
     * - Longitude is not 0.0.
     * - Latitude is between -90 and 90.
     * - Longitude is between -180 and 180.
     *
     * @param lat Latitude value.
     * @param lon Longitude value.
     * @return True if the coordinates are valid.
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