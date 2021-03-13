package se.keyelementab.findwayhome

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


class MainActivity : AppCompatActivity(), LocationListener, SensorEventListener {
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

    private val locationManager by lazy { getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    private val sensorManager by lazy { getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    // TODO: can this be static?
    val sharedPrefManager = SharedPrefManager()

    var currentLocation : Location? = null

    var destinationLat : Double? = null
    var destinationLong : Double? = null


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
            }
        }

        fabAnimationHandler.enableFab(onItemClickInterface)

        startGetLocation()
    }

    override fun onPause() {
        super.onPause()
        fabAnimationHandler.disableFab()

        stopGetLocation()
    }

    @SuppressLint("MissingPermission") // the permission is checked using checkPermissions
    private fun startGetLocation() {
        Log.d(TAG, "startGetLocation")

        if (!checkPermissions()) {
            Log.d(TAG, "startGetLocation requestPermissions")
            ActivityCompat.requestPermissions(this,
                    REQUIRED_PERMISSIONS,
                    locationPermissionCode)
        } else {
            Log.d(TAG, "startGetLocation requestLocationUpdates")
//            if (locationManager == null) {
//                locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    LOCATION_UPDATE_TIME_MS,
                    LOCATION_UPDATE_DISTANCE_METERS,
                    this)

            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                    SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun stopGetLocation() {
        Log.d(TAG, "stopGetLocation")
        locationManager.removeUpdates(this);
        sensorManager.unregisterListener(this);
    }

    override fun onLocationChanged(location: Location) {
        Log.d(TAG, "onLocationChanged, Latitude: " + location.latitude + " , Longitude: " + location.longitude)
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

    override fun onSensorChanged(event: SensorEvent?) {
        val degree = Math.round(event!!.values[0]).toFloat()
        Log.d(TAG, "onSensorChanged, direction: " + degree)
        //Log.d(TAG, "onSensorChanged, event: " + event)


        val directionTextView = findViewById<TextView>(R.id.directionTextView)
        directionTextView.text = "direction " + degree
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // no relevant
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
                startGetLocation()
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