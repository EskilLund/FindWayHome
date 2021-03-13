package se.keyelementab.findwayhome

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import android.widget.TextView

class MainActivity : AppCompatActivity(), LocationListener {
    private val TAG = "MainActivity"

    private val LOCATION_UPDATE_TIME_MS = 5000L
    private val LOCATION_UPDATE_DISTANCE_METERS = 0f

    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                                               Manifest.permission.ACCESS_COARSE_LOCATION)

    private val fabAnimationHandler = FabAnimationHandler(this)

    /**
     * Flag indicating that the first received position from the location manager is to be stored
     * as the destination.
     */
    private var firstReceivedPositionIsDestination = false

    //private lateinit var locationManager: LocationManager = null
    val locationManager by lazy { getSystemService(Context.LOCATION_SERVICE) as LocationManager }

    // TODO: can this be static?
    val sharedPrefManager = SharedPrefManager()

    var currentLocation : Location? = null

    private val locationPermissionCode = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        val onItemClickInterface : FabAnimationHandler.ClickListener = object : FabAnimationHandler.ClickListener {
            override fun onAboutClicked() {
                Log.d(TAG, "onAboutClicked")
            }

            override fun onSetDestinationClicked() {
                Log.d(TAG, "onSetDestinationClicked")
                firstReceivedPositionIsDestination = true
                getLocation()
            }
        }

        fabAnimationHandler.enableFab(onItemClickInterface)
    }

    override fun onPause() {
        super.onPause()
        fabAnimationHandler.disableFab()
    }

    @SuppressLint("MissingPermission") // the permission is checked using checkPermissions
    private fun getLocation() {
        Log.d(TAG, "getLocation")

        if (!checkPermissions()) {
            Log.d(TAG, "getLocation requestPermissions")
            ActivityCompat.requestPermissions(this,
                    REQUIRED_PERMISSIONS,
                    locationPermissionCode)
        } else {
            Log.d(TAG, "getLocation requestLocationUpdates")
//            if (locationManager == null) {
//                locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    LOCATION_UPDATE_TIME_MS,
                    LOCATION_UPDATE_DISTANCE_METERS,
                    this)
        }
    }

    override fun onLocationChanged(location: Location) {
        Log.d(TAG, "Latitude: " + location.latitude + " , Longitude: " + location.longitude)
        val latTextView = findViewById<TextView>(R.id.latTextView)
        val longTextView = findViewById<TextView>(R.id.longTextView)
        latTextView.text = "lat " + location.latitude
        longTextView.text = "long " + location.longitude

        if (firstReceivedPositionIsDestination) {
            sharedPrefManager.setLongLat(this, location)
            firstReceivedPositionIsDestination = false
        }

        currentLocation = location
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.d(TAG, "onRequestPermissionsResult")
        if (requestCode == locationPermissionCode) {
            var allGranted = true
            if (grantResults.isEmpty()) {
                allGranted = false
            }

            grantResults.forEach {
                if (it != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false
                }
            }

            if (allGranted) {
                // call getLocation again, as this is where the permission request was instigated
                getLocation()
            } else {
                presentPermissionsAreMandatoryDialog()
            }
        }
    }

    fun presentPermissionsAreMandatoryDialog() {
        // TODO: use AlertDialog, and better text
        Toast.makeText(this, R.string.permissions_are_mandatory, Toast.LENGTH_SHORT).show()
    }

    fun checkPermissions() : Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            val granted = ActivityCompat.checkSelfPermission(
                    this,
                    permission) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                //TODO: simplify, granted ? "" : "not"
                Log.d(TAG, "Permission " + permission + " granted")
            } else {
                Log.d(TAG, "Permission " + permission + " not granted")
            }

            if (!granted) {
                return false
            }
        }
        return true
    }
}