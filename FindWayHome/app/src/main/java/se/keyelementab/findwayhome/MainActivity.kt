package se.keyelementab.findwayhome

import android.Manifest
//import android.R
import android.annotation.SuppressLint
import android.app.Dialog
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
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


class MainActivity : AppCompatActivity(), LocationListener, SensorEventListener {
    private val TAG = "MainActivity"
    private val DEBUG = false

    private val LOCATION_UPDATE_TIME_MS = 5000L
    private val LOCATION_UPDATE_DISTANCE_METERS = 0f

    private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val fabAnimationHandler = FabAnimationHandler(this)

    /**
     * Flag indicating that the first received position from the location manager is to be stored
     * as the destination.
     */
    private var firstReceivedPositionIsDestination = false

    private val locationManager by lazy { getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    private val sensorManager by lazy { getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    // TODO: can these be static?
    private val sharedPrefManager = SharedPrefManager()
    private val directionManager by lazy { DirectionManager() }


    /**
     * Direction from current position to the destination position.
     * This is in degrees east of true north.
     */
    var bearingToDestination : Float? = null

    private val LOCATION_PERMISSION_CODE = 2

    private val COMPASS_UPDATE_DELAY_MS = 200
    private var latestCompassUpdateTimeMs : Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        val context = this

        val onItemClickInterface : FabAnimationHandler.ClickListener = object : FabAnimationHandler.ClickListener {
            override fun onAboutClicked() {
                Log.d(TAG, "onAboutClicked")
            }

            override fun onSetDestinationClicked() {
                Log.d(TAG, "onSetDestinationClicked")
                if (!sharedPrefManager.isDisclaimerAccepted(context)) {
                    presentDisclaimerDialog()
                } else {
                    firstReceivedPositionIsDestination = true
                }
            }
        }

        fabAnimationHandler.enableFab(onItemClickInterface)

        if (!sharedPrefManager.isDisclaimerAccepted(this)) {
            presentDisclaimerDialog()
        } else {
            startGetLocation()
        }
    }

    fun presentDisclaimerDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.disclaimer)
        val checkbox = dialog.findViewById(R.id.checkBoxAgree) as CheckBox
        val continueButton = dialog.findViewById(R.id.continueButton) as Button
        val cancelButton = dialog.findViewById(R.id.cancelButton) as Button

        checkbox.setOnClickListener {
            continueButton.isEnabled = checkbox.isChecked()
        }

        continueButton.setOnClickListener {
            dialog.dismiss()
            sharedPrefManager.setDisclaimerAccepted(this)
            startGetLocation()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
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
            ActivityCompat.requestPermissions(
                    this,
                    REQUIRED_PERMISSIONS,
                    LOCATION_PERMISSION_CODE
            )
        } else {
            Log.d(TAG, "startGetLocation requestLocationUpdates")
//            if (locationManager == null) {
//                locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//            }
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_UPDATE_TIME_MS,
                    LOCATION_UPDATE_DISTANCE_METERS,
                    this
            )

            sensorManager.registerListener(
                    this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                    SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    private fun stopGetLocation() {
        Log.d(TAG, "stopGetLocation")
        locationManager.removeUpdates(this);
        sensorManager.unregisterListener(this);
    }

    override fun onLocationChanged(location: Location) {
        Log.d(
                TAG,
                "onLocationChanged, Latitude: " + location.latitude + " , Longitude: " + location.longitude
        )

        if (DEBUG) {
            val latTextView = findViewById<TextView>(R.id.latTextView)
            val longTextView = findViewById<TextView>(R.id.longTextView)
            latTextView.visibility = View.VISIBLE
            longTextView.visibility = View.VISIBLE
            latTextView.text = "lat: " + location.latitude
            longTextView.text = "long: " + location.longitude
        }

        if (firstReceivedPositionIsDestination) {
            if (DEBUG) {
                // for debugging purposes, set the destination to Turning Torso in Malmö, Sweden
                val locationTurningTorso = Location(LocationManager.GPS_PROVIDER)
                locationTurningTorso.latitude = 55.607997568
                locationTurningTorso.longitude = 12.97249611
                sharedPrefManager.setLongLat(this, locationTurningTorso)
            } else {
                sharedPrefManager.setLongLat(this, location)
            }
            firstReceivedPositionIsDestination = false
            Toast.makeText(this, R.string.destination_set, Toast.LENGTH_LONG).show()
        }

        if (sharedPrefManager.isDestinationSet(this)) {
//            directionToDestination = directionManager.calculateDirectionToDestination(
//                sharedPrefManager.getLatitude(this),
//                sharedPrefManager.getLongitude(this),
//                location)
            val destination = Location(LocationManager.GPS_PROVIDER)
            destination.latitude = sharedPrefManager.getLatitude(this)
            destination.longitude = sharedPrefManager.getLongitude(this)
            bearingToDestination = location.bearingTo(destination)
            Log.d(TAG, "bearingToDestination: " + bearingToDestination)

            Log.d(TAG, "distance: " + location.distanceTo(destination))



//            val locationNewYork = Location(LocationManager.GPS_PROVIDER)
//            locationNewYork.latitude = 40.730610
//            locationNewYork.longitude = -73.935242
//            Log.d(TAG, "newyorkbearing: " + location.bearingTo(locationNewYork))


        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // these calls are too frequent and can not
        if (latestCompassUpdateTimeMs != null &&
            (System.currentTimeMillis() - latestCompassUpdateTimeMs!!) < COMPASS_UPDATE_DELAY_MS) {
            return
        }
        latestCompassUpdateTimeMs = System.currentTimeMillis()

        val degree = Math.round(event!!.values[0]).toFloat()
        Log.d(TAG, "onSensorChanged, bearing: " + degree)

        if (DEBUG) {
            val bearingTextView = findViewById<TextView>(R.id.bearingTextView)
            bearingTextView.visibility = View.VISIBLE
            bearingTextView.text = "Compass bearing: " + degree
        }

        if (bearingToDestination != null) {
            // TODO: degree is to the magnetic north
            Log.d(TAG, "turnImage: " + directionManager.degreesToTurnImage(bearingToDestination!!, degree))

            val arrowImageView = findViewById<ImageView>(R.id.arroyImageView)
            val turnDegrees = directionManager.degreesToTurnImage(bearingToDestination!!, degree)
            arrowImageView.setRotation(turnDegrees)
            Log.d(TAG, "rotate image: " + turnDegrees)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // no relevant
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionsResult")
        if (requestCode == LOCATION_PERMISSION_CODE) {
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
                    permission
            ) == PackageManager.PERMISSION_GRANTED
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